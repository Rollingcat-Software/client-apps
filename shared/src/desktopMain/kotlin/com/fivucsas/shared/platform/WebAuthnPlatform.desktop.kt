package com.fivucsas.shared.platform

actual fun provideWebAuthnAuthenticator(): WebAuthnAuthenticator {
    return object : WebAuthnAuthenticator {
        override suspend fun isAvailable(): Boolean = false

        override suspend fun createCredential(
            rpId: String,
            rpName: String,
            userId: String,
            userName: String,
            challenge: String,
            excludeCredentialIds: List<String>,
            authenticatorAttachment: String,
            userVerification: String
        ): WebAuthnCreateResult {
            throw FingerprintAuthException("WebAuthn is not available on desktop.", false)
        }

        override suspend fun getAssertion(
            rpId: String,
            challenge: String,
            allowCredentialIds: List<String>,
            userVerification: String
        ): WebAuthnAssertionResult {
            throw FingerprintAuthException("WebAuthn is not available on desktop.", false)
        }
    }
}

actual fun isWebAuthnAvailable(): Boolean = false
