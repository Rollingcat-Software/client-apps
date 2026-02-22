package com.fivucsas.shared.platform

import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import org.koin.core.context.GlobalContext
import java.lang.ref.WeakReference
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import java.util.UUID
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

actual fun providePlatformFingerprintAuthenticator(): FingerprintAuthenticator {
    val app = GlobalContext.get().get<android.content.Context>().applicationContext as Application
    return AndroidFingerprintAuthenticator(app)
}

actual fun isFingerprintFlowAvailable(): Boolean = true

private class AndroidFingerprintAuthenticator(
    private val application: Application
) : FingerprintAuthenticator {

    private val prefs by lazy {
        application.getSharedPreferences("fivucsas_fingerprint", Activity.MODE_PRIVATE)
    }
    private val executor: Executor by lazy {
        Executor { command -> Handler(Looper.getMainLooper()).post(command) }
    }

    init {
        ActivityTracker.registerIfNeeded(application)
    }

    override suspend fun isSupported(): Boolean {
        val manager = BiometricManager.from(application)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    override suspend fun getOrCreateKeyId(): String {
        val existing = prefs.getString(KEY_ID, null)
        if (!existing.isNullOrBlank()) {
            ensureKeyExists(existing)
            return existing
        }
        val keyId = "fp-${UUID.randomUUID()}"
        createKeyPair(keyId)
        prefs.edit().putString(KEY_ID, keyId).apply()
        return keyId
    }

    override suspend fun getPublicKeyJwk(): String {
        val keyId = getOrCreateKeyId()
        val publicKey = loadPublicKey(keyId)
        return buildEcJwk(keyId, publicKey)
    }

    override suspend fun signNonce(nonce: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            throw FingerprintAuthException(
                message = "Biometric prompt requires Android 9+ on this build.",
                recoverable = false
            )
        }
        val activity = ActivityTracker.currentActivity()
            ?: throw FingerprintAuthException(
                message = "Biometric prompt host activity is unavailable.",
                recoverable = true
            )
        val keyId = getOrCreateKeyId()
        val signature = authenticateAndSign(activity, keyId, nonce)
        return Base64.encodeToString(signature, Base64.NO_WRAP)
    }

    private fun ensureKeyExists(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(alias)) {
            createKeyPair(alias)
        }
    }

    private fun createKeyPair(alias: String) {
        val generator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(false)
        generator.initialize(builder.build())
        generator.generateKeyPair()
    }

    private fun loadPublicKey(alias: String): ECPublicKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val certificate = keyStore.getCertificate(alias)
            ?: throw FingerprintAuthException("Missing public key certificate.", false)
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(certificate.publicKey.encoded))
        return publicKey as ECPublicKey
    }

    private fun buildEcJwk(keyId: String, publicKey: ECPublicKey): String {
        val x = publicKey.w.affineX.toByteArray().toUnsigned32()
        val y = publicKey.w.affineY.toByteArray().toUnsigned32()
        val xB64 = Base64.encodeToString(x, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val yB64 = Base64.encodeToString(y, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        return """{"kty":"EC","crv":"P-256","kid":"$keyId","x":"$xB64","y":"$yB64"}"""
    }

    @TargetApi(Build.VERSION_CODES.P)
    private suspend fun authenticateAndSign(
        activity: Activity,
        alias: String,
        nonce: String
    ): ByteArray = suspendCancellableCoroutine { continuation ->
        val prompt = BiometricPrompt.Builder(activity)
            .setTitle("Fingerprint verification")
            .setSubtitle("Verify your fingerprint to continue")
            .setNegativeButton("Cancel", executor) { _, _ ->
                continuation.resumeWithException(
                    FingerprintAuthException("Fingerprint scan was cancelled.", true)
                )
            }
            .build()

        val cancellationSignal = CancellationSignal()
        continuation.invokeOnCancellation { cancellationSignal.cancel() }

        prompt.authenticate(
            cancellationSignal,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    val isLockout = errorCode == BiometricPrompt.BIOMETRIC_ERROR_LOCKOUT || errorCode == 9
                    continuation.resumeWithException(
                        FingerprintAuthException(
                            message = errString?.toString()
                                ?: if (isLockout) "Too many attempts." else "Authentication error.",
                            recoverable = !isLockout
                        )
                    )
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    try {
                        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                        val privateKey = keyStore.getKey(alias, null)
                            ?: throw FingerprintAuthException("Private key not found.", false)
                        val signer = Signature.getInstance("SHA256withECDSA")
                        signer.initSign(privateKey as java.security.PrivateKey)
                        signer.update(nonce.encodeToByteArray())
                        continuation.resume(signer.sign())
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onAuthenticationFailed() {
                    continuation.resumeWithException(
                        FingerprintAuthException("Fingerprint not recognized.", true)
                    )
                }
            }
        )
    }

    private fun ByteArray.toUnsigned32(): ByteArray {
        if (size == 32) return this
        return if (size > 32) copyOfRange(size - 32, size) else ByteArray(32 - size) + this
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ID = "fingerprint_key_id"
    }
}

private object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var activityRef: WeakReference<Activity>? = null
    private var isRegistered = false

    fun registerIfNeeded(application: Application) {
        if (!isRegistered) {
            application.registerActivityLifecycleCallbacks(this)
            isRegistered = true
        }
    }

    fun currentActivity(): Activity? = activityRef?.get()

    override fun onActivityResumed(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
