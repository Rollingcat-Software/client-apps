package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.PermissionItem
import com.fivucsas.shared.domain.model.Role

data class RoleManagementUiState(
    val roles: List<Role> = emptyList(),
    val allPermissions: List<PermissionItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Create/Edit dialog state
    val showEditDialog: Boolean = false,
    val editingRole: Role? = null,
    val editName: String = "",
    val editDescription: String = "",
    val selectedPermissions: Set<String> = emptySet(),

    // Delete dialog state
    val showDeleteDialog: Boolean = false,
    val roleToDelete: Role? = null
)
