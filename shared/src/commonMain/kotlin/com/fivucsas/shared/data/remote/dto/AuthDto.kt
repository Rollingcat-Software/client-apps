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
 */
@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
    val user: AuthUserDto? = null
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
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn,
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
