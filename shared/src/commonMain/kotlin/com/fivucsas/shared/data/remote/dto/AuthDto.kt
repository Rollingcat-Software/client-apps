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
