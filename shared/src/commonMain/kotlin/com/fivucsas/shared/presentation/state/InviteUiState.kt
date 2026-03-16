package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.model.InviteStatus
import com.fivucsas.shared.domain.model.ReceivedInvite

/**
 * UI State for the admin invite management screen.
 *
 * Immutable — all changes create new instances via [copy].
 */
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

/**
 * UI State for the member's received invitations screen.
 *
 * Immutable — all changes create new instances via [copy].
 */
data class ReceivedInviteUiState(
    val invites: List<ReceivedInvite> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
