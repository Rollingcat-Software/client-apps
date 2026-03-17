package com.fivucsas.shared.domain.model

/**
 * Auth Session domain model
 * Represents an active authentication session
 */
data class AuthSession(
    val id: String,
    val userId: String,
    val deviceInfo: String = "",
    val ipAddress: String = "",
    val userAgent: String = "",
    val status: String = "ACTIVE",
    val createdAt: String = "",
    val lastActiveAt: String = "",
    val expiresAt: String = ""
)
