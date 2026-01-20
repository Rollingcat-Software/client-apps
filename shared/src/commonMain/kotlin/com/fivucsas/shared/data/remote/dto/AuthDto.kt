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
 */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

/**
 * Auth Response DTO
 */
@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)

/**
 * Convert DTO to domain model
 */
fun AuthResponseDto.toModel(): AuthTokens {
    return AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}

/**
 * Convert domain model to DTO
 */
fun AuthTokens.toDto(): AuthResponseDto {
    return AuthResponseDto(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    )
}
