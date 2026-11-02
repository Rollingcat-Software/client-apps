package com.fivucsas.shared.attestation

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.UUID

/**
 * Android hardware-attested signing key for the attested puzzle binding
 * (identity-core-api `docs/ATTESTED_PUZZLE_BINDING.md` §4.2).
 *
 * For each puzzle session: generate an EC P-256 SIGN key in AndroidKeyStore with
 * `setAttestationChallenge(serverNonce)`, StrongBox-backed when the device has
 * StrongBox (fallback TEE on [StrongBoxUnavailableException]), no user
 * authentication (the liveness puzzle is the user-presence proof); sign the
 * canonical payload with SHA256withECDSA; export the attestation chain; delete
 * the key. The chain's KeyDescription, written by the secure hardware, names this
 * app's package and signing certificate, so the server can tell the signature came
 * from the genuine, unmodified app on a genuine device.
 *
 * Key attestation needs API 24+ (minSdk). StrongBox needs API 28+.
 */
class AttestedKeyManager(private val context: Context) : AttestedKeyProvider {

    /**
     * Key attestation exists on every API 24+ device with a hardware keystore. A
     * device without one produces a SOFTWARE-level attestation, which the server
     * rejects (`SOFTWARE_SECURITY_LEVEL`); the server is the authority on the level.
     */
    override fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    private fun hasStrongBox(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)

    override suspend fun signWithAttestedKey(challenge: ByteArray, payload: ByteArray): AttestedSignature =
        withContext(Dispatchers.Default) {
            if (!isSupported()) {
                throw AttestationUnsupportedException("Hardware key attestation is not available on this device")
            }
            require(challenge.isNotEmpty() && challenge.size <= 128) { "attestation challenge must be 1..128 bytes" }
            val alias = "fivucsas-puzzle-" + UUID.randomUUID()
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            try {
                val strongBox = generateKey(alias, challenge)
                val privateKey = keyStore.getKey(alias, null) as PrivateKey
                val signature = Signature.getInstance(SIGNATURE_ALGORITHM).run {
                    initSign(privateKey)
                    update(payload)
                    sign()
                }
                val chain = keyStore.getCertificateChain(alias)
                    ?: throw AttestationUnsupportedException("Keystore returned no attestation chain")
                AttestedSignature(chain.map { it.encoded }, signature, strongBox)
            } finally {
                runCatching { keyStore.deleteEntry(alias) }
            }
        }

    /** Generates the attested key; returns true when it is StrongBox-backed. */
    private fun generateKey(alias: String, challenge: ByteArray): Boolean {
        if (hasStrongBox()) {
            try {
                generate(alias, challenge, strongBox = true)
                return true
            } catch (e: StrongBoxUnavailableException) {
                // fall through to TEE
            }
        }
        generate(alias, challenge, strongBox = false)
        return false
    }

    private fun generate(alias: String, challenge: ByteArray, strongBox: Boolean) {
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(false)
            .setAttestationChallenge(challenge)
            .apply {
                if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    setIsStrongBoxBacked(true)
                }
            }
            .build()
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE).run {
            initialize(spec)
            generateKeyPair()
        }
    }

    companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
    }
}

actual fun provideAttestedKeyProvider(): AttestedKeyProvider =
    AttestedKeyManager(GlobalContext.get().get<Context>().applicationContext)
