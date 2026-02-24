package com.fivucsas.shared.presentation.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TenantSettingsUiState(
    val tenantName: String = "Acme Corporation",
    val livenessCheckEnabled: Boolean = true,
    val confidenceThreshold: Float = 0.85f,
    val maxEnrollmentAttempts: Int = 3,
    val sessionTimeoutMinutes: Int = 30,
    val autoLockEnabled: Boolean = true,
    val nfcExamEntryEnabled: Boolean = false,
    val inviteExpiryDays: Int = 30,
    val isLoading: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class TenantSettingsViewModel {
    private val _state = MutableStateFlow(TenantSettingsUiState())
    val state: StateFlow<TenantSettingsUiState> = _state.asStateFlow()

    fun loadSettings() {
        // Mock: settings already initialized with defaults
        _state.value = TenantSettingsUiState(isLoading = false)
    }

    fun setLivenessCheck(enabled: Boolean) {
        _state.value = _state.value.copy(livenessCheckEnabled = enabled, hasUnsavedChanges = true)
    }

    fun setConfidenceThreshold(value: Float) {
        _state.value = _state.value.copy(confidenceThreshold = value, hasUnsavedChanges = true)
    }

    fun setMaxEnrollmentAttempts(value: Int) {
        _state.value = _state.value.copy(maxEnrollmentAttempts = value, hasUnsavedChanges = true)
    }

    fun setSessionTimeout(minutes: Int) {
        _state.value = _state.value.copy(sessionTimeoutMinutes = minutes, hasUnsavedChanges = true)
    }

    fun setAutoLock(enabled: Boolean) {
        _state.value = _state.value.copy(autoLockEnabled = enabled, hasUnsavedChanges = true)
    }

    fun setNfcExamEntry(enabled: Boolean) {
        _state.value = _state.value.copy(nfcExamEntryEnabled = enabled, hasUnsavedChanges = true)
    }

    fun setInviteExpiryDays(days: Int) {
        _state.value = _state.value.copy(inviteExpiryDays = days, hasUnsavedChanges = true)
    }

    fun saveSettings() {
        // Mock: pretend to save
        _state.value = _state.value.copy(
            hasUnsavedChanges = false,
            successMessage = "Settings saved successfully"
        )
    }

    fun clearMessages() {
        _state.value = _state.value.copy(successMessage = null, errorMessage = null)
    }
}
