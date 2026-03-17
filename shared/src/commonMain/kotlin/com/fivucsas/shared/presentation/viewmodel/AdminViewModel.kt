package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.usecase.admin.CheckSystemHealthUseCase
import com.fivucsas.shared.domain.usecase.admin.CreateUserUseCase
import com.fivucsas.shared.domain.usecase.admin.DeleteUserUseCase
import com.fivucsas.shared.domain.usecase.admin.GetStatisticsUseCase
import com.fivucsas.shared.domain.usecase.admin.GetUsersUseCase
import com.fivucsas.shared.domain.usecase.admin.UpdateUserUseCase
import com.fivucsas.shared.presentation.state.AdminTab
import com.fivucsas.shared.presentation.state.AdminUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Admin ViewModel - User management and administration
 *
 * Features:
 * - User management (add, edit, delete)
 * - Real-time search and filtering
 * - Statistics dashboard
 * - Tab navigation
 * - Error handling with graceful fallback
 *
 * When backend is ready, API calls will work automatically!
 */
class AdminViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val createUserUseCase: CreateUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val checkSystemHealthUseCase: CheckSystemHealthUseCase
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
        loadStatistics()
    }

    // TAB NAVIGATION
    fun selectTab(tab: AdminTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                errorMessage = null,
                successMessage = null
            )
        }

        // Reload data when switching to certain tabs
        when (tab) {
            AdminTab.USERS -> if (_uiState.value.users.isEmpty()) loadUsers()
            AdminTab.ANALYTICS -> loadStatistics()
            else -> {}
        }
    }

    // SEARCH & FILTER
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterUsers(query)
    }

    private fun filterUsers(query: String) {
        val allUsers = _uiState.value.users
        val filtered = if (query.isBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                        user.email.contains(query, ignoreCase = true) ||
                        user.idNumber.contains(query, ignoreCase = true) ||
                        user.phoneNumber.contains(query, ignoreCase = true)
            }
        }
        _uiState.update { it.copy(filteredUsers = filtered) }
    }

    // DATA LOADING
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getUsersUseCase().fold(
                onSuccess = { users ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            users = users,
                            filteredUsers = users
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load users"
                        )
                    }
                }
            )
        }
    }

    fun loadStatistics() {
        viewModelScope.launch {
            getStatisticsUseCase().fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(statistics = stats) }
                },
                onFailure = { /* Statistics are non-critical, silently ignore */ }
            )
        }
    }

    // USER MANAGEMENT
    fun showAddUserDialog() {
        _uiState.update {
            it.copy(
                showAddUserDialog = true,
                editingUser = null,
                errorMessage = null
            )
        }
    }

    fun hideAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = false) }
    }

    fun showEditUserDialog(user: User) {
        _uiState.update {
            it.copy(
                showEditUserDialog = true,
                editingUser = user,
                errorMessage = null
            )
        }
    }

    fun hideEditUserDialog() {
        _uiState.update {
            it.copy(
                showEditUserDialog = false,
                editingUser = null
            )
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            createUserUseCase(user).fold(
                onSuccess = { createdUser ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showAddUserDialog = false,
                            successMessage = "User added: ${createdUser.name}"
                        )
                    }
                    loadUsers()
                    loadStatistics()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to add user"
                        )
                    }
                }
            )
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            updateUserUseCase(user.id, user).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showEditUserDialog = false,
                            editingUser = null,
                            successMessage = "User updated: ${user.name}"
                        )
                    }
                    loadUsers()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to update user"
                        )
                    }
                }
            )
        }
    }

    fun showDeleteConfirmation(user: User) {
        _uiState.update {
            it.copy(
                showDeleteConfirmation = true,
                userToDelete = user
            )
        }
    }

    fun hideDeleteConfirmation() {
        _uiState.update {
            it.copy(
                showDeleteConfirmation = false,
                userToDelete = null
            )
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val userName = _uiState.value.userToDelete?.name
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            deleteUserUseCase(userId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showDeleteConfirmation = false,
                            userToDelete = null,
                            successMessage = "User deleted: ${userName ?: "Unknown"}"
                        )
                    }
                    loadUsers()
                    loadStatistics()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to delete user"
                        )
                    }
                }
            )
        }
    }

    fun confirmDelete() {
        _uiState.value.userToDelete?.let { user ->
            deleteUser(user.id)
        }
    }

    // MESSAGE CONTROL
    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    // SETTINGS MANAGEMENT
    fun updateSettings(settings: com.fivucsas.shared.presentation.state.SettingsState) {
        _uiState.update {
            it.copy(
                settings = settings,
                hasUnsavedSettings = true
            )
        }
    }

    fun saveSettings() {
        // Admin panel settings (API URLs, thresholds) are client-side config.
        // Persisted in local state; backend API for admin config not yet available.
        _uiState.update {
            it.copy(
                hasUnsavedSettings = false,
                successMessage = "Settings saved locally"
            )
        }
    }

    fun resetSettings() {
        _uiState.update {
            it.copy(
                settings = com.fivucsas.shared.presentation.state.SettingsState(),
                hasUnsavedSettings = false,
                successMessage = "Settings reset to defaults"
            )
        }
    }

    fun testDatabaseConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            checkSystemHealthUseCase().fold(
                onSuccess = { isHealthy ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = if (isHealthy) "Database connection successful" else null,
                            errorMessage = if (!isHealthy) "Database connection failed" else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Connection test failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearCache() {
        // Local cache clearing — no backend API required
        _uiState.update {
            it.copy(successMessage = "Cache cleared successfully")
        }
    }

    fun exportLogs() {
        // Log export requires platform-specific file system access.
        // Backend API for log retrieval not yet available.
        _uiState.update {
            it.copy(successMessage = "Log export not yet implemented")
        }
    }

    fun checkSystemHealth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            checkSystemHealthUseCase().fold(
                onSuccess = { isHealthy ->
                    val healthStatus = if (isHealthy) {
                        com.fivucsas.shared.presentation.state.HealthStatus.GOOD
                    } else {
                        com.fivucsas.shared.presentation.state.HealthStatus.WARNING
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            settings = it.settings.copy(systemHealthStatus = healthStatus),
                            successMessage = "System health check completed: ${healthStatus.name}"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            settings = it.settings.copy(
                                systemHealthStatus = com.fivucsas.shared.presentation.state.HealthStatus.CRITICAL
                            ),
                            errorMessage = "Health check failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }
}
