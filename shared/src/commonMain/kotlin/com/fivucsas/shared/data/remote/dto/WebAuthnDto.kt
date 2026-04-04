package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response from POST /api/v1/webauthn/register/options/{userId}.
 * Contains the challenge and relying party info needed by the Credential Manager.
 */
@Serializable
data class WebAuthnRegistrationOptionsDto(
    val sessionId: String = "",
    val challenge: String = "",
    val rpId: String = "",
    val rpName: String = "",
    val userId: String = "",
    val userName: String = "",
    val excludeCredentials: List<String> = emptyList(),
    val attestation: String = "direct",
    val authenticatorSelection: AuthenticatorSelectionDto = AuthenticatorSelectionDto()
)

@Serializable
data class AuthenticatorSelectionDto(
    val authenticatorAttachment: String = "platform",
    val requireResidentKey: Boolean = false,
    val userVerification: String = "preferred"
)

/**
 * Request body for POST /api/v1/webauthn/register/verify.
 */
@Serializable
data class WebAuthnRegistrationVerifyRequestDto(
    val userId: String,
    val sessionId: String,
    val credentialId: String,
    val publicKey: String,
    val publicKeyAlgorithm: String = "ES256",
    val attestationFormat: String? = null,
    val transports: String? = null,
    val deviceName: String? = null,
    @SerialName("clientDataJSON")
    val clientDataJson: String? = null
)

/**
 * Response from POST /api/v1/webauthn/register/verify.
 */
@Serializable
data class WebAuthnRegistrationVerifyResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val credentialId: String = ""
)
