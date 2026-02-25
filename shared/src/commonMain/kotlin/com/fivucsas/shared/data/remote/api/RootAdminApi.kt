package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.RootAuditDto
import com.fivucsas.shared.data.remote.dto.RootRoleMatrixDto
import com.fivucsas.shared.data.remote.dto.RootSecurityEventDto
import com.fivucsas.shared.data.remote.dto.RootSettingsDto
import com.fivucsas.shared.data.remote.dto.RootTenantDetailDto
import com.fivucsas.shared.data.remote.dto.RootTenantDto
import com.fivucsas.shared.data.remote.dto.RootUserDto
import com.fivucsas.shared.domain.model.RootFilter

interface RootAdminApi {
    // /api/v1/tenants
    suspend fun getTenants(filter: RootFilter): List<RootTenantDto>
    suspend fun createTenant(payload: RootTenantDto): RootTenantDto
    suspend fun updateTenant(tenantId: String, payload: RootTenantDto): RootTenantDto
    suspend fun deleteTenant(tenantId: String)
    suspend fun getTenantDetail(tenantId: String): RootTenantDetailDto

    // /api/v1/users
    suspend fun getUsers(filter: RootFilter): List<RootUserDto>
    suspend fun updateUser(userId: String, payload: RootUserDto): RootUserDto
    suspend fun deleteUser(userId: String)

    // /api/v1/roles, /api/v1/permissions
    suspend fun getRolesPermissions(): List<RootRoleMatrixDto>
    suspend fun updateRolePermissions(role: String, permissions: Set<String>)

    // /api/v1/audit
    suspend fun getAuditLogs(filter: RootFilter): List<RootAuditDto>
    suspend fun exportAuditLogs(filter: RootFilter): String

    // /api/v1/security-events
    suspend fun getSecurityEvents(filter: RootFilter): List<RootSecurityEventDto>

    // /api/v1/settings
    suspend fun getSystemSettings(): RootSettingsDto
    suspend fun updateSystemSettings(payload: RootSettingsDto): RootSettingsDto
}
