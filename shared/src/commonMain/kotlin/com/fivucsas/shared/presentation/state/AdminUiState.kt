package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User

/**
 * UI State for Admin Dashboard
 *
 * Represents the complete state of the admin dashboard.
 * Immutable - all changes create new instances.
 */
data class AdminUiState(
    val selectedTab: AdminTab = AdminTab.USERS,
    val searchQuery: String = "",
    val users: List<User> = emptyList(),
    val filteredUsers: List<User> = emptyList(),
    val statistics: Statistics = Statistics(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showAddUserDialog: Boolean = false,
    val showEditUserDialog: Boolean = false,
    val editingUser: User? = null,
    val showDeleteConfirmation: Boolean = false,
    val userToDelete: User? = null
)

/**
 * Admin dashboard tabs
 */
enum class AdminTab {
    USERS,
    ANALYTICS,
    SECURITY,
    SETTINGS
}
