package com.fivucsas.shared.domain.model

/**
 * Domain model for a role with its assigned permissions.
 */
data class Role(
    val id: String,
    val tenantId: String? = null,
    val name: String,
    val description: String = "",
    val systemRole: Boolean = false,
    val active: Boolean = true,
    val permissions: List<PermissionItem> = emptyList()
)

/**
 * Domain model for a single permission.
 */
data class PermissionItem(
    val id: String,
    val name: String,
    val description: String = "",
    val resource: String = "",
    val action: String = "",
    val category: String = ""
)
