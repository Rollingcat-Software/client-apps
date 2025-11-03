package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.repository.AuthTokens

/**
 * Login Request DTO
 * 
 * TODO: Add @Serializable when Ktor is added (Week 2)
 */
data class LoginRequestDto(
    val email: String,
    val password: String
)

/**
 * Auth Response DTO
 * 
 * TODO: Add @Serializable when Ktor is added (Week 2)
 */
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
