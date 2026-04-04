package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.RolesApi
import com.fivucsas.shared.data.remote.dto.CreateRoleRequestDto
import com.fivucsas.shared.data.remote.dto.UpdateRoleRequestDto
import com.fivucsas.shared.domain.model.PermissionItem
import com.fivucsas.shared.domain.model.Role
import com.fivucsas.shared.domain.repository.RolesRepository

class RolesRepositoryImpl(
    private val rolesApi: RolesApi
) : RolesRepository {

    override suspend fun getRoles(): Result<List<Role>> = runCatching {
        rolesApi.getRoles().map { dto ->
            Role(
                id = dto.id,
                tenantId = dto.tenantId,
                name = dto.name,
                description = dto.description ?: "",
                systemRole = dto.systemRole,
                active = dto.active,
                permissions = dto.permissions.map { p ->
                    PermissionItem(
                        id = p.id,
                        name = p.authority ?: p.name,
                        description = p.description ?: "",
                        resource = p.resource ?: "",
                        action = p.action ?: "",
                        category = categorize(p.authority ?: p.name)
                    )
                }
            )
        }
    }

    override suspend fun createRole(name: String, description: String?): Result<Role> = runCatching {
        val dto = rolesApi.createRole(CreateRoleRequestDto(name = name, description = description))
        Role(
            id = dto.id,
            tenantId = dto.tenantId,
            name = dto.name,
            description = dto.description ?: "",
            systemRole = dto.systemRole,
            active = dto.active
        )
    }

    override suspend fun updateRole(id: String, name: String?, description: String?): Result<Role> = runCatching {
        val dto = rolesApi.updateRole(id, UpdateRoleRequestDto(name = name, description = description))
        Role(
            id = dto.id,
            tenantId = dto.tenantId,
            name = dto.name,
            description = dto.description ?: "",
            systemRole = dto.systemRole,
            active = dto.active
        )
    }

    override suspend fun deleteRole(id: String): Result<Unit> = runCatching {
        rolesApi.deleteRole(id)
    }

    override suspend fun getPermissions(): Result<List<PermissionItem>> = runCatching {
        rolesApi.getPermissions().map { p ->
            PermissionItem(
                id = p.id,
                name = p.authority ?: p.name,
                description = p.description ?: "",
                resource = p.resource ?: "",
                action = p.action ?: "",
                category = categorize(p.authority ?: p.name)
            )
        }
    }

    override suspend fun updateRolePermissions(roleId: String, permissionNames: List<String>): Result<Unit> = runCatching {
        rolesApi.updateRolePermissions(roleId, permissionNames)
    }

    private fun categorize(permName: String): String {
        val lower = permName.lowercase()
        return when {
            lower.contains("user") || lower.contains("profile") -> "user"
            lower.contains("enroll") || lower.contains("verify") || lower.contains("biometric") -> "enrollment"
            lower.contains("audit") || lower.contains("history") -> "audit"
            lower.contains("tenant") || lower.contains("invite") -> "tenant"
            lower.contains("platform") || lower.contains("settings") || lower.contains("health") -> "platform"
            else -> "other"
        }
    }
}
