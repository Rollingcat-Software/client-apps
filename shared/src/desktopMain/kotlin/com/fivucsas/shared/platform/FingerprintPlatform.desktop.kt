package com.fivucsas.shared.platform

actual fun providePlatformFingerprintAuthenticator(): FingerprintAuthenticator {
    return object : FingerprintAuthenticator {
        override suspend fun isSupported(): Boolean = false

        override suspend fun getOrCreateKeyId(): String {
            throw FingerprintAuthException("Fingerprint is unsupported on desktop.", false)
        }

        override suspend fun getPublicKeyJwk(): String {
            throw FingerprintAuthException("Fingerprint is unsupported on desktop.", false)
        }

        override suspend fun signNonce(nonce: String): String {
            throw FingerprintAuthException("Fingerprint is unsupported on desktop.", false)
        }
    }
}

actual fun isFingerprintFlowAvailable(): Boolean = false

