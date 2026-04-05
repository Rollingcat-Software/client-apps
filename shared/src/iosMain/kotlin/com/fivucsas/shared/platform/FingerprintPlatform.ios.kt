package com.fivucsas.shared.platform

import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreFoundation.CFErrorCopyDescription
import platform.Foundation.*
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Security.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS actual implementation for fingerprint / biometric authentication.
 *
 * Uses LocalAuthentication framework (LAContext) for Face ID / Touch ID,
 * and Security framework (Keychain + SecKey) for ECDSA P-256 key management.
 *
 * Flow:
 * 1. getOrCreateKeyId() — creates an EC P-256 key pair in the Keychain if not present
 * 2. getPublicKeyJwk() — exports the public key as JWK
 * 3. signNonce(nonce) — prompts Face ID / Touch ID, then signs the nonce with the private key
 */

actual fun providePlatformFingerprintAuthenticator(): FingerprintAuthenticator {
    return IosFingerprintAuthenticator()
}

actual fun isFingerprintFlowAvailable(): Boolean = true

@OptIn(ExperimentalForeignApi::class)
private class IosFingerprintAuthenticator : FingerprintAuthenticator {

    companion object {
        private const val KEY_TAG = "com.fivucsas.fingerprint.key"
        private const val KEY_ID_PREF = "fingerprint_key_id"
    }

    override suspend fun isSupported(): Boolean {
        val context = LAContext()
        return memScoped {
            val error = alloc<CPointerVar<__CFError>>()
            context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                error = error.ptr
            )
        }
    }

    override suspend fun getOrCreateKeyId(): String {
        // Check if we already have a key ID stored
        val defaults = NSUserDefaults.standardUserDefaults
        val existingId = defaults.stringForKey(KEY_ID_PREF)
        if (existingId != null && keychainKeyExists()) {
            return existingId
        }

        // Generate new key pair
        val keyId = "fp-${NSUUID().UUIDString}"
        createKeyPair()
        defaults.setObject(keyId, forKey = KEY_ID_PREF)
        defaults.synchronize()
        return keyId
    }

    override suspend fun getPublicKeyJwk(): String {
        val keyId = getOrCreateKeyId()
        val publicKey = loadPublicKey()
            ?: throw FingerprintAuthException("Public key not found in Keychain.", false)

        return buildEcJwk(keyId, publicKey)
    }

    override suspend fun signNonce(nonce: String): String {
        // First, authenticate with biometrics
        authenticateWithBiometrics()

        // Then sign the nonce
        val privateKey = loadPrivateKey()
            ?: throw FingerprintAuthException("Private key not found in Keychain.", false)

        return signData(privateKey, nonce.encodeToByteArray())
    }

    /**
     * Prompt Face ID / Touch ID via LAContext.
     */
    private suspend fun authenticateWithBiometrics() = suspendCancellableCoroutine<Unit> { cont ->
        val context = LAContext()

        memScoped {
            val canEvalError = alloc<ObjCObjectVar<NSError?>>()
            val canEvaluate = context.canEvaluatePolicy(
                LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                error = canEvalError.ptr
            )

            if (!canEvaluate) {
                val errMsg = canEvalError.value?.localizedDescription ?: "Biometric not available"
                cont.resumeWithException(FingerprintAuthException(errMsg, false))
                return@suspendCancellableCoroutine
            }
        }

        context.evaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            localizedReason = "Verify your identity"
        ) { success, error ->
            if (success) {
                cont.resume(Unit)
            } else {
                val msg = error?.localizedDescription ?: "Biometric authentication failed"
                val recoverable = error?.code != -2L // -2 = user cancel (LAErrorUserCancel)
                cont.resumeWithException(FingerprintAuthException(msg, recoverable))
            }
        }
    }

    /**
     * Check whether the ECDSA key pair already exists in the Keychain.
     */
    private fun keychainKeyExists(): Boolean = memScoped {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassKey,
            kSecAttrApplicationTag to KEY_TAG.encodeToByteArray().toNSData(),
            kSecAttrKeyType to kSecAttrKeyTypeECSECPrimeRandom,
            kSecReturnRef to kCFBooleanFalse
        )

        val status = SecItemCopyMatching(query as CFDictionaryRef, null)
        status == errSecSuccess
    }

    /**
     * Create an ECDSA P-256 key pair in the Keychain.
     */
    private fun createKeyPair() = memScoped {
        // Delete existing key first
        val deleteQuery = mapOf<Any?, Any?>(
            kSecClass to kSecClassKey,
            kSecAttrApplicationTag to KEY_TAG.encodeToByteArray().toNSData()
        )
        SecItemDelete(deleteQuery as CFDictionaryRef)

        val tagData = KEY_TAG.encodeToByteArray().toNSData()

        val privateKeyAttrs = mapOf<Any?, Any?>(
            kSecAttrIsPermanent to kCFBooleanTrue,
            kSecAttrApplicationTag to tagData
        )

        val attributes = mapOf<Any?, Any?>(
            kSecAttrKeyType to kSecAttrKeyTypeECSECPrimeRandom,
            kSecAttrKeySizeInBits to 256,
            kSecPrivateKeyAttrs to privateKeyAttrs
        )

        val error = alloc<CPointerVar<__CFError>>()
        val privateKey = SecKeyCreateRandomKey(attributes as CFDictionaryRef, error.ptr)

        if (privateKey == null) {
            throw FingerprintAuthException(
                "Failed to create key pair: ${CFErrorCopyDescription(error.value)}",
                false
            )
        }
    }

    /**
     * Load the private key from the Keychain.
     */
    private fun loadPrivateKey(): SecKeyRef? = memScoped {
        val query = mapOf<Any?, Any?>(
            kSecClass to kSecClassKey,
            kSecAttrApplicationTag to KEY_TAG.encodeToByteArray().toNSData(),
            kSecAttrKeyType to kSecAttrKeyTypeECSECPrimeRandom,
            kSecReturnRef to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)

        if (status == errSecSuccess) {
            result.value as? SecKeyRef
        } else {
            null
        }
    }

    /**
     * Load the public key by deriving it from the private key.
     */
    private fun loadPublicKey(): SecKeyRef? {
        val privateKey = loadPrivateKey() ?: return null
        return SecKeyCopyPublicKey(privateKey)
    }

    /**
     * Sign data with the private key using ECDSA SHA-256.
     *
     * @return base64-encoded signature
     */
    private fun signData(privateKey: SecKeyRef, data: ByteArray): String = memScoped {
        val nsData = data.toNSData()
        val cfData = nsData as CFDataRef

        val error = alloc<CPointerVar<__CFError>>()
        val signature = SecKeyCreateSignature(
            privateKey,
            kSecKeyAlgorithmECDSASignatureMessageX962SHA256,
            cfData,
            error.ptr
        )

        if (signature == null) {
            throw FingerprintAuthException(
                "Signing failed: ${CFErrorCopyDescription(error.value)}",
                false
            )
        }

        val signatureData = signature as NSData
        return signatureData.base64EncodedStringWithOptions(0u)
    }

    /**
     * Build an EC JWK JSON string from the public key.
     */
    private fun buildEcJwk(keyId: String, publicKey: SecKeyRef): String = memScoped {
        val error = alloc<CPointerVar<__CFError>>()
        val keyData = SecKeyCopyExternalRepresentation(publicKey, error.ptr)
            ?: throw FingerprintAuthException("Cannot export public key", false)

        val nsKeyData = keyData as NSData
        val bytes = ByteArray(nsKeyData.length.toInt())
        bytes.usePinned { pinned ->
            nsKeyData.getBytes(pinned.addressOf(0), nsKeyData.length)
        }

        // EC P-256 uncompressed point: 0x04 + 32 bytes X + 32 bytes Y = 65 bytes
        if (bytes.size != 65 || bytes[0] != 0x04.toByte()) {
            throw FingerprintAuthException("Unexpected public key format (size=${bytes.size})", false)
        }

        val x = bytes.copyOfRange(1, 33)
        val y = bytes.copyOfRange(33, 65)

        val xB64 = x.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = x.size.toULong())
        }.base64EncodedStringWithOptions(0u).trimBase64Url()

        val yB64 = y.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = y.size.toULong())
        }.base64EncodedStringWithOptions(0u).trimBase64Url()

        """{"kty":"EC","crv":"P-256","kid":"$keyId","x":"$xB64","y":"$yB64"}"""
    }

    /**
     * Convert base64 standard to base64url (no padding).
     */
    private fun String.trimBase64Url(): String =
        replace('+', '-').replace('/', '_').trimEnd('=')
}

/**
 * Helper to convert a Kotlin ByteArray to NSData.
 */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}
