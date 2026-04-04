package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateRoleRequestDto
import com.fivucsas.shared.data.remote.dto.PermissionDto
import com.fivucsas.shared.data.remote.dto.RoleDto
import com.fivucsas.shared.data.remote.dto.UpdateRoleRequestDto

/**
 * Roles API interface
 *
 * Endpoints:
 * - GET    /api/v1/roles          -> list roles
 * - POST   /api/v1/roles          -> create role
 * - PUT    /api/v1/roles/{id}     -> update role
 * - DELETE /api/v1/roles/{id}     -> delete role
 * - GET    /api/v1/permissions     -> list permissions
 * - PUT    /api/v1/roles/{id}/permissions -> update role permissions
 */
interface RolesApi {
    suspend fun getRoles(): List<RoleDto>
    suspend fun createRole(request: CreateRoleRequestDto): RoleDto
    suspend fun updateRole(id: String, request: UpdateRoleRequestDto): RoleDto
    suspend fun deleteRole(id: String)
    suspend fun getPermissions(): List<PermissionDto>
    suspend fun updateRolePermissions(roleId: String, permissionNames: List<String>)
}
