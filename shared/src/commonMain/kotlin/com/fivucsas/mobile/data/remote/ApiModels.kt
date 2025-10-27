package com.fivucsas.mobile.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isBiometricEnrolled: Boolean,
    val createdAt: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val tokenType: String,
    val user: UserDto
)

@Serializable
data class BiometricVerificationResponse(
    val verified: Boolean,
    val confidence: Double,
    val message: String
)
