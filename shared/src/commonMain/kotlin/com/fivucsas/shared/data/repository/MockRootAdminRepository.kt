package com.fivucsas.shared.data.repository

import com.fivucsas.shared.domain.model.AuditLogEntry
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RolePermissionMatrix
import com.fivucsas.shared.domain.model.RootFilter
import com.fivucsas.shared.domain.model.RootSystemSettings
import com.fivucsas.shared.domain.model.SecurityEvent
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary
import com.fivucsas.shared.domain.repository.RootAdminRepository
import kotlinx.datetime.Clock

class MockRootAdminRepository : RootAdminRepository {
    private val tenants = mutableListOf(
        TenantSummary("t_1", "Acme University", "ACTIVE", 2, 148, 500, 173),
        TenantSummary("t_2", "North Labs", "ACTIVE", 1, 42, 200, 57),
        TenantSummary("t_3", "Metro Health", "SUSPENDED", 1, 32, 180, 38)
    )

    private val users = mutableListOf(
        GlobalUser("u_0", null, "Root Operator", "root@fivucsas.com", "ROOT", true),
        GlobalUser("u_1", "t_1", "Aylin Kaya", "aylin@acme.edu", "TENANT_ADMIN", true),
        GlobalUser("u_2", "t_1", "Emir Can", "emir@acme.edu", "TENANT_MEMBER", true),
        GlobalUser("u_3", "t_2", "Merve Oz", "merve@northlabs.io", "TENANT_ADMIN", true),
        GlobalUser("u_4", "t_3", "Cem Tuna", "cem@metrohealth.org", "TENANT_MEMBER", false),
        GlobalUser("u_5", null, "Selin Arda", "selin@global.io", "USER", true),
        GlobalUser("u_6", "t_3", "Baris Demir", "baris@metrohealth.org", "TENANT_MEMBER", true)
    )

    private val matrix = mutableListOf(
        RolePermissionMatrix("TENANT_ADMIN", setOf("TENANT_USERS_READ", "TENANT_SETTINGS_UPDATE", "HISTORY_READ_TENANT")),
        RolePermissionMatrix("TENANT_MEMBER", setOf("VERIFY_SELF", "ENROLL_SELF_CREATE", "HISTORY_READ_SELF"))
    )

    override suspend fun getTenants(filter: RootFilter): Result<List<TenantSummary>> {
        val q = filter.query.trim().lowercase()
        val list = if (q.isBlank()) tenants else tenants.filter { it.name.lowercase().contains(q) }
        return Result.success(list)
    }

    override suspend fun getTenantDetail(tenantId: String): Result<TenantDetail> {
        val summary = tenants.firstOrNull { it.id == tenantId } ?: return Result.failure(IllegalArgumentException("404"))
        return Result.success(
            TenantDetail(
                summary = summary,
                admins = users.filter { it.tenantId == tenantId && it.role == "TENANT_ADMIN" },
                members = users.filter { it.tenantId == tenantId && it.role != "TENANT_ADMIN" },
                settings = mapOf("faceThreshold" to "0.92", "allowGuestCheck" to "true"),
                limits = mapOf("dailyVerifications" to 10000, "activeDevices" to 120)
            )
        )
    }

    override suspend fun createTenant(tenant: TenantSummary): Result<TenantSummary> {
        if (tenants.any { it.name.equals(tenant.name, ignoreCase = true) }) return Result.failure(IllegalStateException("409"))
        tenants.add(tenant)
        return Result.success(tenant)
    }

    override suspend fun updateTenant(tenantId: String, tenant: TenantSummary): Result<TenantSummary> {
        val idx = tenants.indexOfFirst { it.id == tenantId }
        if (idx == -1) return Result.failure(IllegalArgumentException("404"))
        tenants[idx] = tenant
        return Result.success(tenant)
    }

    override suspend fun deleteTenant(tenantId: String): Result<Unit> {
        tenants.removeAll { it.id == tenantId }
        return Result.success(Unit)
    }

    override suspend fun getUsers(filter: RootFilter): Result<List<GlobalUser>> {
        val q = filter.query.trim().lowercase()
        val scoped = filter.tenantId?.let { tid -> users.filter { it.tenantId == tid } } ?: users
        val list = if (q.isBlank()) scoped else scoped.filter {
            it.fullName.lowercase().contains(q) || it.email.lowercase().contains(q)
        }
        return Result.success(list)
    }

    override suspend fun updateUser(userId: String, enabled: Boolean): Result<GlobalUser> {
        val idx = users.indexOfFirst { it.id == userId }
        if (idx == -1) return Result.failure(IllegalArgumentException("404"))
        users[idx] = users[idx].copy(enabled = enabled)
        return Result.success(users[idx])
    }

    override suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        email: String,
        role: String,
        tenantId: String?
    ): Result<GlobalUser> {
        val idx = users.indexOfFirst { it.id == userId }
        if (idx == -1) return Result.failure(IllegalArgumentException("404"))
        val updated = users[idx].copy(
            fullName = fullName,
            email = email,
            role = role,
            tenantId = tenantId
        )
        users[idx] = updated
        return Result.success(updated)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> {
        users.removeAll { it.id == userId }
        return Result.success(Unit)
    }

    override suspend fun getTenantAdmins(filter: RootFilter): Result<List<GlobalUser>> {
        return getUsers(filter).map { all -> all.filter { it.role == "TENANT_ADMIN" } }
    }

    override suspend fun assignTenantAdmin(userId: String, tenantId: String): Result<Unit> {
        val idx = users.indexOfFirst { it.id == userId }
        if (idx == -1) return Result.failure(IllegalArgumentException("404"))
        users[idx] = users[idx].copy(tenantId = tenantId, role = "TENANT_ADMIN")
        return Result.success(Unit)
    }

    override suspend fun unassignTenantAdmin(userId: String, tenantId: String): Result<Unit> {
        val idx = users.indexOfFirst { it.id == userId && it.tenantId == tenantId }
        if (idx == -1) return Result.failure(IllegalArgumentException("404"))
        users[idx] = users[idx].copy(role = "TENANT_MEMBER")
        return Result.success(Unit)
    }

    override suspend fun resetAdminPassword(userId: String): Result<Unit> {
        return if (users.any { it.id == userId }) Result.success(Unit) else Result.failure(IllegalArgumentException("404"))
    }

    override suspend fun getRolesAndPermissions(): Result<List<RolePermissionMatrix>> = Result.success(matrix)

    override suspend fun updateRolePermissions(role: String, permissions: Set<String>): Result<Unit> {
        val idx = matrix.indexOfFirst { it.roleName == role }
        if (idx == -1) matrix.add(RolePermissionMatrix(role, permissions)) else matrix[idx] = matrix[idx].copy(permissions = permissions)
        return Result.success(Unit)
    }

    override suspend fun getAuditLogs(filter: RootFilter): Result<List<AuditLogEntry>> {
        val now = Clock.System.now()
        return Result.success(
            listOf(
                AuditLogEntry("a_1", now, "root@fivucsas.com", "t_1", "TENANT_UPDATE", "SUCCESS", "Updated quota limit"),
                AuditLogEntry("a_2", now, "root@fivucsas.com", "t_2", "USER_DISABLE", "SUCCESS", "Disabled user u_44")
            )
        )
    }

    override suspend fun exportAuditLogs(filter: RootFilter): Result<String> = Result.success("audit-export-${Clock.System.now().toEpochMilliseconds()}.csv")

    override suspend fun getSecurityEvents(filter: RootFilter): Result<List<SecurityEvent>> {
        val now = Clock.System.now()
        return Result.success(
            listOf(
                SecurityEvent("s_1", now, "HIGH", "RATE_LIMIT", "t_1", "Too many verification attempts"),
                SecurityEvent("s_2", now, "MEDIUM", "LOCKOUT", "t_2", "Admin account lockout after failed logins")
            )
        )
    }

    override suspend fun getSystemSettings(): Result<RootSystemSettings> {
        return Result.success(
            RootSystemSettings(
                jwtPolicySummary = "Issuer: fivucsas-auth, TTL: 15m access / 7d refresh",
                defaultRateLimitPerMinute = 120,
                passwordPolicySummary = "min 10 chars, mixed case, digit, symbol"
            )
        )
    }

    override suspend fun updateSystemSettings(settings: RootSystemSettings): Result<RootSystemSettings> = Result.success(settings)
}
