package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.Role
import com.fivucsas.shared.domain.repository.RolesRepository
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.RoleManagementUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Role Management screen (Phase 2.5).
 */
class RoleManagementViewModel(
    private val rolesRepository: RolesRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(RoleManagementUiState())
    val uiState: StateFlow<RoleManagementUiState> = _uiState.asStateFlow()

    fun loadRoles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            rolesRepository.getRoles().fold(
                onSuccess = { roles ->
                    _uiState.update { it.copy(isLoading = false, roles = roles) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load roles")
                        )
                    }
                }
            )
        }
    }

    fun loadPermissions() {
        viewModelScope.launch {
            rolesRepository.getPermissions().fold(
                onSuccess = { permissions ->
                    _uiState.update { it.copy(allPermissions = permissions) }
                },
                onFailure = { /* Non-critical, silently ignore */ }
            )
        }
    }

    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editingRole = null,
                editName = "",
                editDescription = "",
                selectedPermissions = emptySet()
            )
        }
    }

    fun showEditDialog(role: Role) {
        _uiState.update {
            it.copy(
                showEditDialog = true,
                editingRole = role,
                editName = role.name,
                editDescription = role.description,
                selectedPermissions = role.permissions.map { p -> p.name }.toSet()
            )
        }
    }

    fun hideEditDialog() {
        _uiState.update {
            it.copy(showEditDialog = false, editingRole = null)
        }
    }

    fun updateEditName(name: String) {
        _uiState.update { it.copy(editName = name) }
    }

    fun updateEditDescription(description: String) {
        _uiState.update { it.copy(editDescription = description) }
    }

    fun togglePermission(permissionName: String) {
        _uiState.update {
            val current = it.selectedPermissions
            val updated = if (current.contains(permissionName)) {
                current - permissionName
            } else {
                current + permissionName
            }
            it.copy(selectedPermissions = updated)
        }
    }

    fun saveRole() {
        val state = _uiState.value
        if (state.editName.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val editing = state.editingRole
            if (editing != null) {
                // Update existing role
                rolesRepository.updateRole(
                    id = editing.id,
                    name = state.editName,
                    description = state.editDescription
                ).fold(
                    onSuccess = {
                        // Also update permissions
                        rolesRepository.updateRolePermissions(
                            editing.id,
                            state.selectedPermissions.toList()
                        )
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showEditDialog = false,
                                successMessage = s(StringKey.ROLE_UPDATED)
                            )
                        }
                        loadRoles()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = ErrorMapper.mapToUserMessage(error, "update role")
                            )
                        }
                    }
                )
            } else {
                // Create new role
                rolesRepository.createRole(
                    name = state.editName,
                    description = state.editDescription
                ).fold(
                    onSuccess = { newRole ->
                        // Assign permissions to the new role
                        if (state.selectedPermissions.isNotEmpty()) {
                            rolesRepository.updateRolePermissions(
                                newRole.id,
                                state.selectedPermissions.toList()
                            )
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showEditDialog = false,
                                successMessage = s(StringKey.ROLE_CREATED)
                            )
                        }
                        loadRoles()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = ErrorMapper.mapToUserMessage(error, "create role")
                            )
                        }
                    }
                )
            }
        }
    }

    fun showDeleteDialog(role: Role) {
        _uiState.update {
            it.copy(showDeleteDialog = true, roleToDelete = role)
        }
    }

    fun hideDeleteDialog() {
        _uiState.update {
            it.copy(showDeleteDialog = false, roleToDelete = null)
        }
    }

    fun confirmDelete() {
        val role = _uiState.value.roleToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteDialog = false) }

            rolesRepository.deleteRole(role.id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            roleToDelete = null,
                            successMessage = s(StringKey.ROLE_DELETED)
                        )
                    }
                    loadRoles()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "delete role")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
