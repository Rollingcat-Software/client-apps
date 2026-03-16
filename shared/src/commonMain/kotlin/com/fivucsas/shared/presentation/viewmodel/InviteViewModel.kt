package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.InviteStatus
import com.fivucsas.shared.domain.usecase.invite.CreateInviteUseCase
import com.fivucsas.shared.domain.usecase.invite.GetInvitesUseCase
import com.fivucsas.shared.domain.usecase.invite.RevokeInviteUseCase
import com.fivucsas.shared.presentation.state.InviteUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InviteViewModel(
    private val getInvitesUseCase: GetInvitesUseCase,
    private val createInviteUseCase: CreateInviteUseCase,
    private val revokeInviteUseCase: RevokeInviteUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(InviteUiState())
    val state: StateFlow<InviteUiState> = _state.asStateFlow()

    fun loadInvites() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            getInvitesUseCase().fold(
                onSuccess = { invites ->
                    _state.update {
                        it.copy(
                            invites = invites,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    applyFilters()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load invitations"
                        )
                    }
                }
            )
        }
    }

    fun updateSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun setFilter(status: InviteStatus?) {
        _state.update { it.copy(selectedFilter = status) }
        applyFilters()
    }

    fun setTenantFilter(tenantId: String?) {
        _state.update { it.copy(selectedTenantId = tenantId) }
        applyFilters()
    }

    private fun applyFilters() {
        _state.update { s ->
            val filtered = s.invites.filter { invite ->
                val matchesSearch = s.searchQuery.isBlank() ||
                    invite.email.contains(s.searchQuery, ignoreCase = true)
                val matchesFilter = s.selectedFilter == null || invite.status == s.selectedFilter
                val matchesTenant = s.selectedTenantId == null || invite.tenantId == s.selectedTenantId
                matchesSearch && matchesFilter && matchesTenant
            }
            s.copy(filteredInvites = filtered)
        }
    }

    fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _state.update { it.copy(showCreateDialog = false) }
    }

    fun createInvite(
        email: String,
        role: String,
        tenantId: String? = null,
        @Suppress("UNUSED_PARAMETER") tenantName: String? = null
    ) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            createInviteUseCase(
                email = email,
                role = role,
                tenantId = tenantId
            ).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            showCreateDialog = false,
                            successMessage = "Invitation sent to $email"
                        )
                    }
                    loadInvites()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to send invitation"
                        )
                    }
                }
            )
        }
    }

    fun revokeInvite(inviteId: String) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            revokeInviteUseCase(inviteId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Invitation revoked"
                        )
                    }
                    loadInvites()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to revoke invitation"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
