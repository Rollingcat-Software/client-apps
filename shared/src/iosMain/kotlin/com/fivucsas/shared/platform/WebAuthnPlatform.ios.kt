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
            throw FingerprintAuthException(
                "WebAuthn not yet implemented on iOS. Use ASAuthorizationController.",
                false
            )
        }

        override suspend fun getAssertion(
            rpId: String,
            challenge: String,
            allowCredentialIds: List<String>,
            userVerification: String
        ): WebAuthnAssertionResult {
            throw FingerprintAuthException(
                "WebAuthn not yet implemented on iOS. Use ASAuthorizationController.",
                false
            )
        }
    }
}

actual fun isWebAuthnAvailable(): Boolean = false
