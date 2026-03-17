package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.AuthSession
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthSessionDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("device_info") val deviceInfo: String = "",
    @SerialName("ip_address") val ipAddress: String = "",
    @SerialName("user_agent") val userAgent: String = "",
    val status: String = "ACTIVE",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("last_active_at") val lastActiveAt: String = "",
    @SerialName("expires_at") val expiresAt: String = ""
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
