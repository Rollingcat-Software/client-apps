package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuditLogEntry
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RolePermissionMatrix
import com.fivucsas.shared.domain.model.RootFilter
import com.fivucsas.shared.domain.model.RootSystemSettings
import com.fivucsas.shared.domain.model.SecurityEvent
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary

interface RootAdminRepository {
    suspend fun getTenants(filter: RootFilter = RootFilter()): Result<List<TenantSummary>>
    suspend fun getTenantDetail(tenantId: String): Result<TenantDetail>
    suspend fun createTenant(tenant: TenantSummary): Result<TenantSummary>
    suspend fun updateTenant(tenantId: String, tenant: TenantSummary): Result<TenantSummary>
    suspend fun deleteTenant(tenantId: String): Result<Unit>

    suspend fun getUsers(filter: RootFilter = RootFilter()): Result<List<GlobalUser>>
    suspend fun updateUser(userId: String, enabled: Boolean): Result<GlobalUser>
    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        email: String,
        role: String,
        tenantId: String?
    ): Result<GlobalUser>
    suspend fun deleteUser(userId: String): Result<Unit>

    suspend fun getTenantAdmins(filter: RootFilter = RootFilter()): Result<List<GlobalUser>>
    suspend fun assignTenantAdmin(userId: String, tenantId: String): Result<Unit>
    suspend fun unassignTenantAdmin(userId: String, tenantId: String): Result<Unit>
    suspend fun resetAdminPassword(userId: String): Result<Unit>

    suspend fun getRolesAndPermissions(): Result<List<RolePermissionMatrix>>
    suspend fun updateRolePermissions(role: String, permissions: Set<String>): Result<Unit>

    suspend fun getAuditLogs(filter: RootFilter = RootFilter()): Result<List<AuditLogEntry>>
    suspend fun exportAuditLogs(filter: RootFilter = RootFilter()): Result<String>

    suspend fun getSecurityEvents(filter: RootFilter = RootFilter()): Result<List<SecurityEvent>>
    suspend fun getSystemSettings(): Result<RootSystemSettings>
    suspend fun updateSystemSettings(settings: RootSystemSettings): Result<RootSystemSettings>
}
