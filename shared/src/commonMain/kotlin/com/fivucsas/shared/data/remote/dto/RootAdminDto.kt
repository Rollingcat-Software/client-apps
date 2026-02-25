package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RootTenantDto(
    val id: String,
    val name: String,
    val status: String,
    val adminCount: Int,
    val memberCount: Int,
    val quotaLimit: Int,
    val quotaUsed: Int
)

@Serializable
data class RootUserDto(
    val id: String,
    val tenantId: String? = null,
    val fullName: String,
    val email: String,
    val role: String,
    val enabled: Boolean
)

@Serializable
data class RootTenantDetailDto(
    val summary: RootTenantDto,
    val admins: List<RootUserDto>,
    val members: List<RootUserDto>,
    val settings: Map<String, String>,
    val limits: Map<String, Int>
)

@Serializable
data class RootRoleMatrixDto(
    val roleName: String,
    val permissions: Set<String>
)

@Serializable
data class RootAuditDto(
    val id: String,
    val timestampEpochMillis: Long,
    val actor: String,
    val tenantId: String? = null,
    val action: String,
    val status: String,
    val details: String
)

@Serializable
data class RootSecurityEventDto(
    val id: String,
    val timestampEpochMillis: Long,
    val severity: String,
    val eventType: String,
    val tenantId: String? = null,
    val message: String
)

@Serializable
data class RootSettingsDto(
    val jwtPolicySummary: String,
    val defaultRateLimitPerMinute: Int,
    val passwordPolicySummary: String
)
