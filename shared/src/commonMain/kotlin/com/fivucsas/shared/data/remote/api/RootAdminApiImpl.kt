package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.RootAuditDto
import com.fivucsas.shared.data.remote.dto.RootRoleMatrixDto
import com.fivucsas.shared.data.remote.dto.RootSecurityEventDto
import com.fivucsas.shared.data.remote.dto.RootSettingsDto
import com.fivucsas.shared.data.remote.dto.RootTenantDetailDto
import com.fivucsas.shared.data.remote.dto.RootTenantDto
import com.fivucsas.shared.data.remote.dto.RootUserDto
import com.fivucsas.shared.domain.model.RootFilter
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Real implementation of RootAdminApi calling the Identity Core API.
 *
 * Maps between the backend's actual response shapes and the client DTOs.
 */
class RootAdminApiImpl(
    private val client: HttpClient
) : RootAdminApi {

    // ─── Backend response DTOs (internal, match the real API shape) ──────────

    @Serializable
    private data class PagedResponse<T>(
        val content: List<T>,
        val page: Int = 0,
        val size: Int = 20,
        val totalPages: Int = 0
    )

    @Serializable
    private data class BackendTenant(
        val id: String,
        val name: String,
        val slug: String? = null,
        val description: String? = null,
        val contactEmail: String? = null,
        val contactPhone: String? = null,
        val status: String,
        val maxUsers: Int = 0,
        val currentUsers: Int = 0,
        val biometricEnabled: Boolean = false,
        val sessionTimeoutMinutes: Int = 30,
        val refreshTokenValidityDays: Int = 7,
        val mfaRequired: Boolean = false,
        val createdAt: String? = null,
        val updatedAt: String? = null
    )

    @Serializable
    private data class BackendUser(
        val id: String,
        val email: String,
        val firstName: String? = null,
        val lastName: String? = null,
        val phoneNumber: String? = null,
        val address: String? = null,
        val idNumber: String? = null,
        val status: String = "ACTIVE",
        val emailVerified: Boolean = false,
        val phoneVerified: Boolean = false,
        val role: String? = null,
        val roles: List<String> = emptyList(),
        val tenantId: String? = null,
        val enrolledAt: String? = null,
        val lastVerifiedAt: String? = null,
        val verificationCount: Int = 0,
        val lastLoginAt: String? = null,
        val lastLoginIp: String? = null,
        val createdAt: String? = null,
        val updatedAt: String? = null,
        val biometricEnrolled: Boolean = false
    )

    @Serializable
    private data class BackendRole(
        val id: String,
        val tenantId: String? = null,
        val name: String,
        val description: String? = null,
        val systemRole: Boolean = false,
        val active: Boolean = true,
        val permissions: List<BackendPermission> = emptyList()
    )

    @Serializable
    private data class BackendPermission(
        val id: String,
        val name: String,
        val description: String? = null,
        val resource: String? = null,
        val action: String? = null,
        val authority: String? = null
    )

    @Serializable
    private data class BackendAuditLog(
        val id: String,
        val userId: String? = null,
        val tenantId: String? = null,
        val action: String,
        val entityType: String? = null,
        val entityId: String? = null,
        val success: Boolean = true,
        val errorMessage: String? = null,
        val ipAddress: String? = null,
        val userAgent: String? = null,
        val details: JsonObject? = null,
        val timestamp: String? = null
    )

    @Serializable
    private data class CreateTenantRequest(
        val name: String,
        val slug: String,
        val contactEmail: String? = null,
        val maxUsers: Int = 100,
        val biometricEnabled: Boolean = true
    )

    @Serializable
    private data class UpdateTenantRequest(
        val name: String? = null,
        val contactEmail: String? = null,
        val maxUsers: Int? = null,
        val status: String? = null
    )

    @Serializable
    private data class UpdateUserRequest(
        val firstName: String? = null,
        val lastName: String? = null,
        val email: String? = null,
        val role: String? = null,
        val status: String? = null
    )

    @Serializable
    private data class UpdateRolePermissionsRequest(
        val permissionNames: List<String>
    )

    // ─── Mapping helpers ─────────────────────────────────────────────────────

    private fun BackendTenant.toDto(): RootTenantDto = RootTenantDto(
        id = id,
        name = name,
        status = status,
        adminCount = 0, // Not provided by the tenant list endpoint
        memberCount = currentUsers,
        quotaLimit = maxUsers,
        quotaUsed = currentUsers
    )

    private fun BackendUser.toDto(): RootUserDto = RootUserDto(
        id = id,
        tenantId = tenantId,
        fullName = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { email },
        email = email,
        role = role ?: roles.firstOrNull() ?: "USER",
        enabled = status == "ACTIVE"
    )

    private fun BackendRole.toMatrixDto(): RootRoleMatrixDto = RootRoleMatrixDto(
        roleName = name,
        permissions = permissions.map { it.authority ?: it.name }.toSet()
    )

    private fun BackendAuditLog.toDto(): RootAuditDto {
        val timestampMs = try {
            kotlinx.datetime.Instant.parse(timestamp ?: "1970-01-01T00:00:00Z").toEpochMilliseconds()
        } catch (_: Exception) {
            0L
        }
        val actorEmail = details?.get("email")?.jsonPrimitive?.content ?: userId ?: "system"
        return RootAuditDto(
            id = id,
            timestampEpochMillis = timestampMs,
            actor = actorEmail,
            tenantId = tenantId,
            action = action,
            status = if (success) "SUCCESS" else "FAILURE",
            details = errorMessage ?: entityType?.let { "$it/$entityId" } ?: ""
        )
    }

    // ─── RootAdminApi implementation ─────────────────────────────────────────

    override suspend fun getTenants(filter: RootFilter): List<RootTenantDto> {
        val response: PagedResponse<BackendTenant> = client.get("tenants") {
            parameter("page", 0)
            parameter("size", 100)
            if (filter.query.isNotBlank()) parameter("search", filter.query)
        }.body()
        return response.content.map { it.toDto() }
    }

    override suspend fun createTenant(payload: RootTenantDto): RootTenantDto {
        val slug = payload.name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        val response: BackendTenant = client.post("tenants") {
            contentType(ContentType.Application.Json)
            setBody(CreateTenantRequest(
                name = payload.name,
                slug = slug,
                maxUsers = payload.quotaLimit
            ))
        }.body()
        return response.toDto()
    }

    override suspend fun updateTenant(tenantId: String, payload: RootTenantDto): RootTenantDto {
        val response: BackendTenant = client.put("tenants/$tenantId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateTenantRequest(
                name = payload.name,
                maxUsers = payload.quotaLimit,
                status = payload.status
            ))
        }.body()
        return response.toDto()
    }

    override suspend fun deleteTenant(tenantId: String) {
        client.delete("tenants/$tenantId")
    }

    override suspend fun getTenantDetail(tenantId: String): RootTenantDetailDto {
        val tenant: BackendTenant = client.get("tenants/$tenantId").body()
        // Fetch users for this tenant
        val usersResponse: PagedResponse<BackendUser> = client.get("users") {
            parameter("tenantId", tenantId)
            parameter("size", 100)
        }.body()
        val allUsers = usersResponse.content.map { it.toDto() }
        val admins = allUsers.filter { it.role == "TENANT_ADMIN" || it.role == "SUPER_ADMIN" }
        val members = allUsers.filter { it.role != "TENANT_ADMIN" && it.role != "SUPER_ADMIN" }
        return RootTenantDetailDto(
            summary = tenant.toDto().copy(adminCount = admins.size, memberCount = members.size),
            admins = admins,
            members = members,
            settings = mapOf(
                "biometricEnabled" to tenant.biometricEnabled.toString(),
                "mfaRequired" to tenant.mfaRequired.toString(),
                "sessionTimeoutMinutes" to tenant.sessionTimeoutMinutes.toString()
            ),
            limits = mapOf(
                "maxUsers" to tenant.maxUsers,
                "currentUsers" to tenant.currentUsers
            )
        )
    }

    override suspend fun getUsers(filter: RootFilter): List<RootUserDto> {
        val response: PagedResponse<BackendUser> = client.get("users") {
            parameter("page", 0)
            parameter("size", 100)
            if (filter.query.isNotBlank()) parameter("search", filter.query)
            filter.tenantId?.let { parameter("tenantId", it) }
        }.body()
        return response.content.map { it.toDto() }
    }

    override suspend fun updateUser(userId: String, payload: RootUserDto): RootUserDto {
        val nameParts = payload.fullName.split(" ", limit = 2)
        val response: BackendUser = client.put("users/$userId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateUserRequest(
                firstName = nameParts.getOrNull(0),
                lastName = nameParts.getOrNull(1),
                email = payload.email,
                role = payload.role,
                status = if (payload.enabled) "ACTIVE" else "INACTIVE"
            ))
        }.body()
        return response.toDto()
    }

    override suspend fun deleteUser(userId: String) {
        client.delete("users/$userId")
    }

    override suspend fun getRolesPermissions(): List<RootRoleMatrixDto> {
        val roles: List<BackendRole> = client.get("roles").body()
        return roles.map { it.toMatrixDto() }
    }

    override suspend fun updateRolePermissions(role: String, permissions: Set<String>) {
        // The backend expects role ID; find it first
        val roles: List<BackendRole> = client.get("roles").body()
        val roleObj = roles.firstOrNull { it.name == role } ?: return
        client.put("roles/${roleObj.id}/permissions") {
            contentType(ContentType.Application.Json)
            setBody(UpdateRolePermissionsRequest(permissionNames = permissions.toList()))
        }
    }

    override suspend fun getAuditLogs(filter: RootFilter): List<RootAuditDto> {
        val response: PagedResponse<BackendAuditLog> = client.get("audit-logs") {
            parameter("page", 0)
            parameter("size", 50)
            filter.tenantId?.let { parameter("tenantId", it) }
            filter.actor?.let { parameter("userId", it) }
        }.body()
        return response.content.map { it.toDto() }
    }

    override suspend fun exportAuditLogs(filter: RootFilter): String {
        // The backend does not have a dedicated export endpoint yet.
        // Return a status message as the endpoint is not yet available.
        return "Export not yet available from API"
    }

    override suspend fun getSecurityEvents(filter: RootFilter): List<RootSecurityEventDto> {
        // Security events are derived from audit logs with success=false
        val response: PagedResponse<BackendAuditLog> = client.get("audit-logs") {
            parameter("page", 0)
            parameter("size", 50)
            parameter("success", false)
        }.body()
        return response.content.map { log ->
            val timestampMs = try {
                kotlinx.datetime.Instant.parse(log.timestamp ?: "1970-01-01T00:00:00Z").toEpochMilliseconds()
            } catch (_: Exception) {
                0L
            }
            RootSecurityEventDto(
                id = log.id,
                timestampEpochMillis = timestampMs,
                severity = if (log.action.contains("LOGIN")) "HIGH" else "MEDIUM",
                eventType = log.action,
                tenantId = log.tenantId,
                message = log.errorMessage ?: "Failed ${log.action}"
            )
        }
    }

    override suspend fun getSystemSettings(): RootSettingsDto {
        // System settings are not a standalone backend endpoint yet.
        // Return actual defaults from the platform configuration.
        return RootSettingsDto(
            jwtPolicySummary = "Issuer: fivucsas-auth, TTL: 15m access / 7d refresh",
            defaultRateLimitPerMinute = 120,
            passwordPolicySummary = "min 8 chars, mixed case, digit required"
        )
    }

    override suspend fun updateSystemSettings(payload: RootSettingsDto): RootSettingsDto {
        // System settings update endpoint not yet available in backend.
        // Return the payload as-is to acknowledge the request.
        return payload
    }
}
