package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.AuthSession
import kotlinx.serialization.Serializable

/**
 * Auth session DTO — server returns camelCase JSON (Spring Boot / Jackson)
 */
@Serializable
data class AuthSessionDto(
    val id: String = "",
    val userId: String = "",
    val deviceInfo: String = "",
    val ipAddress: String = "",
    val userAgent: String = "",
    val status: String = "ACTIVE",
    val createdAt: String = "",
    val lastActiveAt: String = "",
    val expiresAt: String = ""
)

fun AuthSessionDto.toDomain(): AuthSession = AuthSession(
    id = id,
    userId = userId,
    deviceInfo = deviceInfo,
    ipAddress = ipAddress,
    userAgent = userAgent,
    status = status,
    createdAt = createdAt,
    lastActiveAt = lastActiveAt,
    expiresAt = expiresAt
)
