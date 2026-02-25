package com.fivucsas.shared.presentation.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Invite(
    val id: String,
    val email: String,
    val role: String,
    val tenantId: String? = null,
    val tenantName: String? = null,
    val status: InviteStatus,
    val createdAt: String,
    val expiresAt: String
)

enum class InviteStatus { PENDING, ACCEPTED, EXPIRED, REVOKED }

data class InviteUiState(
    val invites: List<Invite> = emptyList(),
    val filteredInvites: List<Invite> = emptyList(),
    val searchQuery: String = "",
    val selectedTenantId: String? = null,
    val selectedFilter: InviteStatus? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false
)

class InviteViewModel {
    private val _state = MutableStateFlow(InviteUiState())
    val state: StateFlow<InviteUiState> = _state.asStateFlow()

    private val mockInvites = listOf(
        Invite("1", "alice@example.com", "TENANT_MEMBER", "t_1", "Acme University", InviteStatus.PENDING, "2026-02-20", "2026-03-20"),
        Invite("2", "bob@example.com", "TENANT_MEMBER", "t_1", "Acme University", InviteStatus.ACCEPTED, "2026-02-15", "2026-03-15"),
        Invite("3", "carol@example.com", "TENANT_ADMIN", "t_2", "North Labs", InviteStatus.EXPIRED, "2026-01-10", "2026-02-10"),
        Invite("4", "dave@example.com", "TENANT_MEMBER", "t_3", "Metro Health", InviteStatus.PENDING, "2026-02-22", "2026-03-22"),
        Invite("5", "eve@example.com", "TENANT_MEMBER", "t_3", "Metro Health", InviteStatus.REVOKED, "2026-02-01", "2026-03-01")
    )

    fun loadInvites() {
        _state.value = _state.value.copy(
            invites = mockInvites,
            filteredInvites = mockInvites,
            isLoading = false
        )
    }

    fun updateSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    fun setFilter(status: InviteStatus?) {
        _state.value = _state.value.copy(selectedFilter = status)
        applyFilters()
    }

    fun setTenantFilter(tenantId: String?) {
        _state.value = _state.value.copy(selectedTenantId = tenantId)
        applyFilters()
    }

    private fun applyFilters() {
        val s = _state.value
        val filtered = s.invites.filter { invite ->
            val matchesSearch = s.searchQuery.isBlank() ||
                invite.email.contains(s.searchQuery, ignoreCase = true)
            val matchesFilter = s.selectedFilter == null || invite.status == s.selectedFilter
            val matchesTenant = s.selectedTenantId == null || invite.tenantId == s.selectedTenantId
            matchesSearch && matchesFilter && matchesTenant
        }
        _state.value = s.copy(filteredInvites = filtered)
    }

    fun showCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _state.value = _state.value.copy(showCreateDialog = false)
    }

    fun createInvite(
        email: String,
        role: String,
        tenantId: String? = null,
        tenantName: String? = null
    ) {
        val newInvite = Invite(
            id = (mockInvites.size + 1).toString(),
            email = email,
            role = role,
            tenantId = tenantId,
            tenantName = tenantName,
            status = InviteStatus.PENDING,
            createdAt = "2026-02-25",
            expiresAt = "2026-03-25"
        )
        val updated = _state.value.invites + newInvite
        _state.value = _state.value.copy(
            invites = updated,
            showCreateDialog = false,
            successMessage = "Invitation sent to $email"
        )
        applyFilters()
    }

    fun revokeInvite(inviteId: String) {
        val updated = _state.value.invites.map {
            if (it.id == inviteId) it.copy(status = InviteStatus.REVOKED) else it
        }
        _state.value = _state.value.copy(
            invites = updated,
            successMessage = "Invitation revoked"
        )
        applyFilters()
    }

    fun clearMessages() {
        _state.value = _state.value.copy(errorMessage = null, successMessage = null)
    }
}
