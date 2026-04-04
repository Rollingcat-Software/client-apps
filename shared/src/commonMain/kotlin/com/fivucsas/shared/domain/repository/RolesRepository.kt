package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.PermissionItem
import com.fivucsas.shared.domain.model.Role

interface RolesRepository {
    suspend fun getRoles(): Result<List<Role>>
    suspend fun createRole(name: String, description: String?): Result<Role>
    suspend fun updateRole(id: String, name: String?, description: String?): Result<Role>
    suspend fun deleteRole(id: String): Result<Unit>
    suspend fun getPermissions(): Result<List<PermissionItem>>
    suspend fun updateRolePermissions(roleId: String, permissionNames: List<String>): Result<Unit>
}
