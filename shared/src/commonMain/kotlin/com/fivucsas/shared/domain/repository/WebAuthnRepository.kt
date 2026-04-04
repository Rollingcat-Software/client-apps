package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.platform.WebAuthnAssertionResult
import com.fivucsas.shared.platform.WebAuthnCreateResult

/**
 * Steps in the WebAuthn registration/verification flow, exposed to the UI.
 */
enum class WebAuthnStep {
    FetchingOptions,
    WaitingForAuthenticator,
    VerifyingWithServer,
    Complete
}

/**
 * Repository interface for WebAuthn FIDO2 operations.
 *
 * Coordinates between the server API (challenge generation / verification)
 * and the platform authenticator (Credential Manager on Android).
 */
interface WebAuthnRepository {

    /**
     * Register a new WebAuthn credential for the given user.
     *
     * @param userId the user's UUID
     * @param authenticatorAttachment "platform" for biometric, "cross-platform" for USB/NFC keys
     * @param deviceName optional display name for the credential
     * @param onStep callback for progress updates
     * @return the created credential result
     */
    suspend fun registerCredential(
        userId: String,
        authenticatorAttachment: String,
        deviceName: String?,
        onStep: (WebAuthnStep) -> Unit = {}
    ): Result<WebAuthnCreateResult>

    /**
     * Verify (authenticate) using an existing WebAuthn credential.
     *
     * For now this uses the registration options endpoint to get a challenge,
     * then performs an assertion. Full assertion endpoint integration can be
     * added when the backend exposes dedicated assertion endpoints.
     *
     * @param userId the user's UUID
     * @param allowCredentialIds specific credential IDs to allow (empty = any)
     * @param onStep callback for progress updates
     * @return the assertion result
     */
    suspend fun verifyCredential(
        userId: String,
        allowCredentialIds: List<String>,
        onStep: (WebAuthnStep) -> Unit = {}
    ): Result<WebAuthnAssertionResult>
}
