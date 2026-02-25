package com.fivucsas.shared.domain.model

import kotlinx.datetime.Instant

data class TenantSummary(
    val id: String,
    val name: String,
    val status: String,
    val adminCount: Int,
    val memberCount: Int,
    val quotaLimit: Int,
    val quotaUsed: Int
)

data class TenantDetail(
    val summary: TenantSummary,
    val admins: List<GlobalUser>,
    val members: List<GlobalUser>,
    val settings: Map<String, String>,
    val limits: Map<String, Int>
)

data class GlobalUser(
    val id: String,
    val tenantId: String?,
    val fullName: String,
    val email: String,
    val role: String,
    val enabled: Boolean
)

data class RolePermissionMatrix(
    val roleName: String,
    val permissions: Set<String>
)

data class AuditLogEntry(
    val id: String,
    val timestamp: Instant,
    val actor: String,
    val tenantId: String?,
    val action: String,
    val status: String,
    val details: String
)

data class SecurityEvent(
    val id: String,
    val timestamp: Instant,
    val severity: String,
    val eventType: String,
    val tenantId: String?,
    val message: String
)

data class RootSystemSettings(
    val jwtPolicySummary: String,
    val defaultRateLimitPerMinute: Int,
    val passwordPolicySummary: String
)

data class RootFilter(
    val query: String = "",
    val tenantId: String? = null,
    val actor: String? = null,
    val action: String? = null,
    val status: String? = null,
    val severity: String? = null,
    val fromEpochMillis: Long? = null,
    val toEpochMillis: Long? = null
)
