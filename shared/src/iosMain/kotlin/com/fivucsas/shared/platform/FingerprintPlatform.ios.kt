package com.fivucsas.shared.platform

actual fun providePlatformFingerprintAuthenticator(): FingerprintAuthenticator {
    return object : FingerprintAuthenticator {
        override suspend fun isSupported(): Boolean = false

        override suspend fun getOrCreateKeyId(): String {
            throw FingerprintAuthException("Fingerprint is not available on this iOS build.", false)
        }

        override suspend fun getPublicKeyJwk(): String {
            throw FingerprintAuthException("Fingerprint is not available on this iOS build.", false)
        }

        override suspend fun signNonce(nonce: String): String {
            throw FingerprintAuthException("Fingerprint is not available on this iOS build.", false)
        }
    }
}

actual fun isFingerprintFlowAvailable(): Boolean = true

