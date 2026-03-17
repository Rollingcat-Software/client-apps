package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.RootAdminApi
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.data.remote.dto.toDto
import com.fivucsas.shared.domain.model.AuditLogEntry
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RolePermissionMatrix
import com.fivucsas.shared.domain.model.RootFilter
import com.fivucsas.shared.domain.model.RootSystemSettings
import com.fivucsas.shared.domain.model.SecurityEvent
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary
import com.fivucsas.shared.domain.repository.RootAdminRepository

/**
 * Real implementation of RootAdminRepository that delegates to RootAdminApi.
 * Replaces the former MockRootAdminRepository.
 */
class RootAdminRepositoryImpl(
    private val api: RootAdminApi
) : RootAdminRepository {

    override suspend fun getTenants(filter: RootFilter): Result<List<TenantSummary>> = runCatching {
        api.getTenants(filter).map { it.toDomain() }
    }

    override suspend fun getTenantDetail(tenantId: String): Result<TenantDetail> = runCatching {
        api.getTenantDetail(tenantId).toDomain()
    }

    override suspend fun createTenant(tenant: TenantSummary): Result<TenantSummary> = runCatching {
        api.createTenant(tenant.toDto()).toDomain()
    }

    override suspend fun updateTenant(tenantId: String, tenant: TenantSummary): Result<TenantSummary> = runCatching {
        api.updateTenant(tenantId, tenant.toDto()).toDomain()
    }

    override suspend fun deleteTenant(tenantId: String): Result<Unit> = runCatching {
        api.deleteTenant(tenantId)
    }

    override suspend fun getUsers(filter: RootFilter): Result<List<GlobalUser>> = runCatching {
        api.getUsers(filter).map { it.toDomain() }
    }

    override suspend fun updateUser(userId: String, enabled: Boolean): Result<GlobalUser> = runCatching {
        // Fetch current user first, then update with enabled/disabled status
        val users = api.getUsers(RootFilter())
        val current = users.firstOrNull { it.id == userId }
            ?: throw IllegalArgumentException("User not found: $userId")
        api.updateUser(userId, current.copy(enabled = enabled)).toDomain()
    }

    override suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        email: String,
        role: String,
        tenantId: String?
    ): Result<GlobalUser> = runCatching {
        val payload = com.fivucsas.shared.data.remote.dto.RootUserDto(
            id = userId,
            tenantId = tenantId,
            fullName = fullName,
            email = email,
            role = role,
            enabled = true
        )
        api.updateUser(userId, payload).toDomain()
    }

    override suspend fun deleteUser(userId: String): Result<Unit> = runCatching {
        api.deleteUser(userId)
    }

    override suspend fun getTenantAdmins(filter: RootFilter): Result<List<GlobalUser>> = runCatching {
        api.getUsers(filter).filter { it.role == "TENANT_ADMIN" || it.role == "SUPER_ADMIN" }.map { it.toDomain() }
    }

    override suspend fun assignTenantAdmin(userId: String, tenantId: String): Result<Unit> = runCatching {
        val users = api.getUsers(RootFilter())
        val current = users.firstOrNull { it.id == userId }
            ?: throw IllegalArgumentException("User not found: $userId")
        api.updateUser(userId, current.copy(role = "TENANT_ADMIN"))
    }

    override suspend fun unassignTenantAdmin(userId: String, tenantId: String): Result<Unit> = runCatching {
        val users = api.getUsers(RootFilter())
        val current = users.firstOrNull { it.id == userId }
            ?: throw IllegalArgumentException("User not found: $userId")
        api.updateUser(userId, current.copy(role = "TENANT_MEMBER"))
    }

    override suspend fun resetAdminPassword(userId: String): Result<Unit> {
        // Password reset endpoint not available for root-initiated resets yet.
        return Result.failure(UnsupportedOperationException("Password reset via admin is not yet available"))
    }

    override suspend fun getRolesAndPermissions(): Result<List<RolePermissionMatrix>> = runCatching {
        api.getRolesPermissions().map { it.toDomain() }
    }

    override suspend fun updateRolePermissions(role: String, permissions: Set<String>): Result<Unit> = runCatching {
        api.updateRolePermissions(role, permissions)
    }

    override suspend fun getAuditLogs(filter: RootFilter): Result<List<AuditLogEntry>> = runCatching {
        api.getAuditLogs(filter).map { it.toDomain() }
    }

    override suspend fun exportAuditLogs(filter: RootFilter): Result<String> = runCatching {
        api.exportAuditLogs(filter)
    }

    override suspend fun getSecurityEvents(filter: RootFilter): Result<List<SecurityEvent>> = runCatching {
        api.getSecurityEvents(filter).map { it.toDomain() }
    }

    override suspend fun getSystemSettings(): Result<RootSystemSettings> = runCatching {
        api.getSystemSettings().toDomain()
    }

    override suspend fun updateSystemSettings(settings: RootSystemSettings): Result<RootSystemSettings> = runCatching {
        api.updateSystemSettings(settings.toDto()).toDomain()
    }
}
