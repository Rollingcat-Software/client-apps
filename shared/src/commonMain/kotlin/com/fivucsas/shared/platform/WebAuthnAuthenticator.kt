package com.fivucsas.shared.platform

/**
 * Result of a WebAuthn credential creation (navigator.credentials.create equivalent).
 */
data class WebAuthnCreateResult(
    val credentialId: String,
    val publicKey: String,
    val publicKeyAlgorithm: String,
    val attestationFormat: String,
    val transports: String,
    val clientDataJson: String
)

/**
 * Result of a WebAuthn credential assertion (navigator.credentials.get equivalent).
 */
data class WebAuthnAssertionResult(
    val credentialId: String,
    val authenticatorData: String,
    val clientDataJson: String,
    val signature: String
)

/**
 * Platform abstraction for WebAuthn operations.
 *
 * On Android: uses AndroidX Credential Manager API.
 * On iOS: would use ASAuthorizationController (not yet implemented).
 * On Desktop: not available (returns unsupported).
 */
interface WebAuthnAuthenticator {

    /**
     * Whether WebAuthn is available on this platform.
     */
    suspend fun isAvailable(): Boolean

    /**
     * Create a new WebAuthn credential (registration / attestation).
     *
     * @param rpId relying party ID (e.g., "auth.rollingcatsoftware.com")
     * @param rpName relying party display name
     * @param userId opaque user handle (UUID)
     * @param userName user display name / email
     * @param challenge base64url-encoded challenge from the server
     * @param excludeCredentialIds credential IDs already registered (to prevent duplicates)
     * @param authenticatorAttachment "platform" for biometric, "cross-platform" for USB/NFC keys
     * @param userVerification "preferred", "required", or "discouraged"
     */
    suspend fun createCredential(
        rpId: String,
        rpName: String,
        userId: String,
        userName: String,
        challenge: String,
        excludeCredentialIds: List<String>,
        authenticatorAttachment: String,
        userVerification: String
    ): WebAuthnCreateResult

    /**
     * Request a WebAuthn assertion (authentication / verification).
     *
     * @param rpId relying party ID
     * @param challenge base64url-encoded challenge from the server
     * @param allowCredentialIds credential IDs that can be used (empty = any)
     * @param userVerification "preferred", "required", or "discouraged"
     */
    suspend fun getAssertion(
        rpId: String,
        challenge: String,
        allowCredentialIds: List<String>,
        userVerification: String
    ): WebAuthnAssertionResult
}
