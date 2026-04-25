package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.repository.AuthTokens
import kotlinx.serialization.Serializable

/**
 * Login Request DTO
 */
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

/**
 * Register Request DTO
 *
 * Server (Spring Boot / Jackson) expects camelCase: firstName, lastName
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

/**
 * User info returned inside the auth response.
 * All fields optional with defaults so unknown/null fields don't crash deserialization.
 */
@Serializable
data class AuthUserDto(
    val id: String = "",
    val email: String = "",
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val idNumber: String? = null,
    val status: String = "ACTIVE",
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val role: String? = null,
    val roles: List<String> = emptyList(),
    val tenantId: String? = null,
    val enrolledAt: String? = null,
    val lastVerifiedAt: String? = null,
    val verificationCount: Int = 0,
    val lastLoginAt: String? = null,
    val lastLoginIp: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val biometricEnrolled: Boolean = false
)

/**
 * Auth Response DTO
 *
 * Server returns camelCase JSON (Spring Boot / Jackson default):
 *   { accessToken, refreshToken, tokenType, expiresIn, user: {...} }
 *
 * When MFA is required, accessToken/refreshToken are null and mfaRequired=true.
 */
@Serializable
data class AuthResponseDto(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val tokenType: String = "Bearer",
    val user: AuthUserDto? = null,
    val mfaRequired: Boolean = false,
    val mfaSessionToken: String? = null,
    val totalSteps: Int? = null,
    val currentStep: Int? = null,
    val availableMethods: List<AvailableMethodDto>? = null
)

/**
 * Available MFA method returned by the backend during MFA flow.
 */
@Serializable
data class AvailableMethodDto(
    val methodType: String,
    val name: String = "",
    val category: String = "",
    val enrolled: Boolean = false,
    val preferred: Boolean = false,
    val requiresEnrollment: Boolean = false
)

/**
 * Request to verify an MFA step.
 * POST /auth/mfa/step
 */
@Serializable
data class MfaStepRequest(
    val sessionToken: String,
    val method: String,
    val data: Map<String, String> = emptyMap()
)

/**
 * Response from MFA step verification.
 *
 * Server-side PR #25 added the following fields:
 *  - `alternativeMethods` — methods the user can pick instead of the
 *    `expectedMethod` for the current step (subset of `availableMethods`,
 *    filtered to ones the step config still permits and the user is
 *    enrolled in / not yet completed).
 *  - `completedMethods` — list of method types the user has already
 *    cleared in this MFA session.
 *  - `expectedMethod` — the method the server currently expects for this
 *    step (informational; user can override via switch-method).
 *  - `errorCode` / `enrollmentUrl` — populated on 4xx error envelopes
 *    (e.g. `NEEDS_ENROLLMENT`) so the client can surface a dedicated UI.
 *
 * All new fields are nullable to keep backwards compatibility with old
 * server builds.
 */
@Serializable
data class MfaStepResponse(
    val status: String = "", // "STEP_COMPLETED" or "AUTHENTICATED"
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val user: AuthUserDto? = null,
    val amr: List<String>? = null, // Authentication Methods References (RFC 8176)
    val nextStep: Int? = null,
    val remainingSteps: Int? = null,
    val currentStep: Int? = null,
    val totalSteps: Int? = null,
    val availableMethods: List<AvailableMethodDto>? = null,
    val alternativeMethods: List<AvailableMethodDto>? = null,
    val completedMethods: List<String>? = null,
    val expectedMethod: String? = null,
    val errorCode: String? = null,
    val enrollmentUrl: String? = null,
    val message: String? = null
)

/**
 * Request body for POST /auth/mfa/switch-method.
 *
 * Lets the user swap the active method for the current MFA step
 * (e.g. from TOTP to EMAIL_OTP). Server validates that:
 *  - the requested method is permitted by the step config,
 *  - the user is enrolled in it,
 *  - the method has not already been completed in this session.
 */
@Serializable
data class MfaSwitchMethodRequest(
    val sessionToken: String,
    val method: String
)

/**
 * Response body for POST /auth/mfa/switch-method.
 *
 * On success (200) `status = "METHOD_SWITCHED"` and the server has
 * pre-dispatched any required side-channel (OTP for EMAIL_OTP / SMS_OTP).
 *
 * On a 409 the server returns this same shape with `errorCode` populated
 * (`METHOD_NOT_PERMITTED`, `METHOD_ALREADY_USED`, `NEEDS_ENROLLMENT`)
 * and an optional `enrollmentUrl` for the NEEDS_ENROLLMENT case.
 */
@Serializable
data class MfaSwitchMethodResponse(
    val status: String = "",
    val currentStep: Int? = null,
    val totalSteps: Int? = null,
    val expectedMethod: String? = null,
    val availableMethods: List<AvailableMethodDto>? = null,
    val alternativeMethods: List<AvailableMethodDto>? = null,
    val completedMethods: List<String>? = null,
    val errorCode: String? = null,
    val enrollmentUrl: String? = null,
    val message: String? = null
)

/**
 * Inner challenge payload returned by the server when requesting a WebAuthn challenge
 * (FINGERPRINT or HARDWARE_KEY) via POST /auth/mfa/step with `data: { action: "challenge" }`.
 */
@Serializable
data class MfaChallengeData(
    val challenge: String = "",
    val rpId: String = "",
    val timeout: String? = null,
    val allowCredentials: List<String> = emptyList()
)

/**
 * Response wrapper for MFA WebAuthn challenge requests.
 * Server returns: { "status": "CHALLENGE", "data": { challenge, rpId, allowCredentials, timeout } }
 */
@Serializable
data class MfaChallengeResponse(
    val status: String = "",
    val data: MfaChallengeData = MfaChallengeData(),
    val message: String? = null
)

/**
 * Request to send an OTP for MFA.
 * POST /auth/mfa/send-otp
 */
@Serializable
data class MfaSendOtpRequest(
    val sessionToken: String,
    val method: String // "EMAIL_OTP" or "SMS_OTP"
)

/**
 * Request to generate a QR token for MFA.
 * POST /auth/mfa/qr-generate
 */
@Serializable
data class MfaQrGenerateRequest(
    val sessionToken: String
)

/**
 * Response from QR token generation.
 */
@Serializable
data class MfaQrTokenResponse(
    val qrToken: String = "",
    val expiresIn: Long = 300
)

/**
 * Refresh token request DTO — server expects camelCase: { "refreshToken": "..." }
 */
@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)

/**
 * Convert DTO to domain model
 */
fun AuthResponseDto.toModel(): AuthTokens {
    val fullName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { user?.email ?: "" }
    return AuthTokens(
        accessToken = accessToken ?: "",
        refreshToken = refreshToken ?: "",
        expiresIn = expiresIn ?: 0L,
        role = user?.role ?: user?.roles?.firstOrNull() ?: "USER",
        userName = fullName,
        userEmail = user?.email ?: "",
        userId = user?.id ?: "",
        tenantId = user?.tenantId ?: ""
    )
}

/**
 * Convert MfaStepResponse to AuthTokens (when AUTHENTICATED).
 */
fun MfaStepResponse.toModel(): AuthTokens {
    val fullName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { user?.email ?: "" }
    return AuthTokens(
        accessToken = accessToken ?: "",
        refreshToken = refreshToken ?: "",
        expiresIn = expiresIn ?: 0L,
        role = user?.role ?: user?.roles?.firstOrNull() ?: "USER",
        userName = fullName,
        userEmail = user?.email ?: "",
        userId = user?.id ?: "",
        tenantId = user?.tenantId ?: ""
    )
}

/**
 * Change password request DTO — server expects camelCase
 */
@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)

fun AuthTokens.toDto(): AuthResponseDto {
    return AuthResponseDto(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}
