package com.fivucsas.shared.platform.biometric

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.fivucsas.shared.domain.biometric.BiometricAuthenticator
import com.fivucsas.shared.domain.model.BiometricCapability
import com.fivucsas.shared.domain.model.BiometricError
import com.fivucsas.shared.domain.model.BiometricStepUpException
import com.fivucsas.shared.domain.model.PublicKeyJwk
import com.fivucsas.shared.platform.AndroidBiometricActivityHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidBiometricAuthenticator(
    private val biometricManager: BiometricManager
) : BiometricAuthenticator {

    override suspend fun canAuthenticate(): BiometricCapability = withContext(Dispatchers.Default) {
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.Supported
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapability.NoHardware
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricCapability.Unsupported
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapability.UnknownError
            else -> BiometricCapability.UnknownError
        }
    }

    override suspend fun ensureKeyPair(keyId: String): PublicKeyJwk = withContext(Dispatchers.Default) {
        val alias = aliasFor(keyId)
        val publicKey = loadPublicKey(alias) ?: createKeyPair(alias)
        toJwk(publicKey, keyId)
    }

    override suspend fun signNonceWithBiometric(
        keyId: String,
        nonce: ByteArray,
        reason: String
    ): ByteArray = withContext(Dispatchers.Main) {
        val alias = aliasFor(keyId)
        val privateKey = loadPrivateKey(alias) ?: run {
            ensureKeyPair(keyId)
            loadPrivateKey(alias)
        } ?: throw BiometricStepUpException(BiometricError.Failed, "Private key not available.")

        val signature = try {
            Signature.getInstance(SIGNATURE_ALGORITHM).apply { initSign(privateKey) }
        } catch (e: KeyPermanentlyInvalidatedException) {
            deleteAlias(alias)
            ensureKeyPair(keyId)
            throw BiometricStepUpException(BiometricError.KeyInvalidated, "Biometric key invalidated.", e)
        } catch (e: InvalidKeyException) {
            deleteAlias(alias)
            ensureKeyPair(keyId)
            throw BiometricStepUpException(BiometricError.KeyInvalidated, "Biometric key invalidated.", e)
        }

        val unlockedSignature = authenticateWithBiometric(signature, reason)
        try {
            unlockedSignature.update(nonce)
            unlockedSignature.sign()
        } catch (e: KeyPermanentlyInvalidatedException) {
            deleteAlias(alias)
            ensureKeyPair(keyId)
            throw BiometricStepUpException(BiometricError.KeyInvalidated, "Biometric key invalidated.", e)
        } catch (e: InvalidKeyException) {
            deleteAlias(alias)
            ensureKeyPair(keyId)
            throw BiometricStepUpException(BiometricError.KeyInvalidated, "Biometric key invalidated.", e)
        }
    }

    private suspend fun authenticateWithBiometric(signature: Signature, reason: String): Signature {
        val activity = AndroidBiometricActivityHolder.getCurrentActivity()
            ?: throw BiometricStepUpException(
                BiometricError.Unknown("No active activity."),
                "No active activity."
            )
        val executor = ContextCompat.getMainExecutor(activity)

        return suspendCancellableCoroutine { continuation ->
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            BiometricStepUpException(
                                mapAuthError(errorCode, errString.toString()),
                                errString.toString()
                            )
                        )
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val signed = result.cryptoObject?.signature
                    if (signed != null && continuation.isActive) {
                        continuation.resume(signed)
                    } else if (continuation.isActive) {
                        continuation.resumeWithException(
                            BiometricStepUpException(BiometricError.Failed, "Missing crypto result.")
                        )
                    }
                }
            }

            val prompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Confirmation")
                .setSubtitle(reason)
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
        }
    }

    private fun mapAuthError(errorCode: Int, message: String): BiometricError {
        return when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricError.Canceled
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricError.Lockout
            BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricError.NotEnrolled
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometricError.NoHardware
            else -> BiometricError.Unknown(message)
        }
    }

    private fun createKeyPair(alias: String): PublicKey {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val specBuilder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(CURVE_NAME))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            specBuilder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else {
            @Suppress("DEPRECATION")
            specBuilder.setUserAuthenticationValidityDurationSeconds(0)
        }

        keyPairGenerator.initialize(specBuilder.build())
        return keyPairGenerator.generateKeyPair().public
    }

    private fun loadPublicKey(alias: String): PublicKey? {
        val keyStore = keyStore()
        val cert = keyStore.getCertificate(alias)
        return cert?.publicKey
    }

    private fun loadPrivateKey(alias: String): PrivateKey? {
        val keyStore = keyStore()
        return keyStore.getKey(alias, null) as? PrivateKey
    }

    private fun deleteAlias(alias: String) {
        val keyStore = keyStore()
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun aliasFor(keyId: String): String = "$ALIAS_PREFIX$keyId"

    private fun toJwk(publicKey: PublicKey, keyId: String): PublicKeyJwk {
        val ecPublicKey = publicKey as ECPublicKey
        val x = toUnsignedFixed(ecPublicKey.w.affineX, 32)
        val y = toUnsignedFixed(ecPublicKey.w.affineY, 32)
        return PublicKeyJwk(
            kty = "EC",
            crv = "P-256",
            x = Base64.encodeToString(x, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
            y = Base64.encodeToString(y, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
            kid = keyId
        )
    }

    private fun toUnsignedFixed(value: BigInteger, size: Int): ByteArray {
        val input = value.toByteArray()
        if (input.size == size) return input
        if (input.size > size) return input.copyOfRange(input.size - size, input.size)
        return ByteArray(size - input.size) + input
    }

    private companion object {
        const val ALIAS_PREFIX = "fivucsas_bio_"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        const val CURVE_NAME = "secp256r1"
    }
}
