package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.AuditLogEntry
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RolePermissionMatrix
import com.fivucsas.shared.domain.model.RootSystemSettings
import com.fivucsas.shared.domain.model.SecurityEvent
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary
import kotlinx.datetime.Instant

fun RootTenantDto.toDomain(): TenantSummary = TenantSummary(
    id = id,
    name = name,
    status = status,
    adminCount = adminCount,
    memberCount = memberCount,
    quotaLimit = quotaLimit,
    quotaUsed = quotaUsed
)

fun RootUserDto.toDomain(): GlobalUser = GlobalUser(
    id = id,
    tenantId = tenantId,
    fullName = fullName,
    email = email,
    role = role,
    enabled = enabled
)

fun RootTenantDetailDto.toDomain(): TenantDetail = TenantDetail(
    summary = summary.toDomain(),
    admins = admins.map { it.toDomain() },
    members = members.map { it.toDomain() },
    settings = settings,
    limits = limits
)

fun RootRoleMatrixDto.toDomain(): RolePermissionMatrix = RolePermissionMatrix(
    roleName = roleName,
    permissions = permissions
)

fun RootAuditDto.toDomain(): AuditLogEntry = AuditLogEntry(
    id = id,
    timestamp = Instant.fromEpochMilliseconds(timestampEpochMillis),
    actor = actor,
    tenantId = tenantId,
    action = action,
    status = status,
    details = details
)

fun RootSecurityEventDto.toDomain(): SecurityEvent = SecurityEvent(
    id = id,
    timestamp = Instant.fromEpochMilliseconds(timestampEpochMillis),
    severity = severity,
    eventType = eventType,
    tenantId = tenantId,
    message = message
)

fun RootSettingsDto.toDomain(): RootSystemSettings = RootSystemSettings(
    jwtPolicySummary = jwtPolicySummary,
    defaultRateLimitPerMinute = defaultRateLimitPerMinute,
    passwordPolicySummary = passwordPolicySummary
)

fun TenantSummary.toDto(): RootTenantDto = RootTenantDto(
    id = id,
    name = name,
    status = status,
    adminCount = adminCount,
    memberCount = memberCount,
    quotaLimit = quotaLimit,
    quotaUsed = quotaUsed
)

fun GlobalUser.toDto(): RootUserDto = RootUserDto(
    id = id,
    tenantId = tenantId,
    fullName = fullName,
    email = email,
    role = role,
    enabled = enabled
)

fun RootSystemSettings.toDto(): RootSettingsDto = RootSettingsDto(
    jwtPolicySummary = jwtPolicySummary,
    defaultRateLimitPerMinute = defaultRateLimitPerMinute,
    passwordPolicySummary = passwordPolicySummary
)
