package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoleDto(
    val id: String = "",
    val tenantId: String? = null,
    val name: String = "",
    val description: String? = null,
    val systemRole: Boolean = false,
    val active: Boolean = true,
    val permissions: List<PermissionDto> = emptyList()
)

@Serializable
data class PermissionDto(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val resource: String? = null,
    val action: String? = null,
    val authority: String? = null
)

@Serializable
data class CreateRoleRequestDto(
    val name: String,
    val description: String? = null
)

@Serializable
data class UpdateRoleRequestDto(
    val name: String? = null,
    val description: String? = null,
    val permissionNames: List<String>? = null
)
