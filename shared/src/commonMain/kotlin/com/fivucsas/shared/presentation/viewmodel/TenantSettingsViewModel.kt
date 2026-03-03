package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.TenantSettings
import com.fivucsas.shared.domain.usecase.tenant.GetTenantSettingsUseCase
import com.fivucsas.shared.domain.usecase.tenant.UpdateTenantSettingsUseCase
import com.fivucsas.shared.presentation.state.TenantSettingsUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TenantSettingsViewModel(
    private val getTenantSettingsUseCase: GetTenantSettingsUseCase,
    private val updateTenantSettingsUseCase: UpdateTenantSettingsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(TenantSettingsUiState())
    val state: StateFlow<TenantSettingsUiState> = _state.asStateFlow()

    fun loadSettings() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        scope.launch {
            getTenantSettingsUseCase().fold(
                onSuccess = { settings ->
                    _state.update {
                        TenantSettingsUiState(
                            tenantName = settings.tenantName,
                            livenessCheckEnabled = settings.livenessCheckEnabled,
                            confidenceThreshold = settings.confidenceThreshold,
                            maxEnrollmentAttempts = settings.maxEnrollmentAttempts,
                            sessionTimeoutMinutes = settings.sessionTimeoutMinutes,
                            autoLockEnabled = settings.autoLockEnabled,
                            nfcExamEntryEnabled = settings.nfcExamEntryEnabled,
                            inviteExpiryDays = settings.inviteExpiryDays,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load settings"
                        )
                    }
                }
            )
        }
    }

    fun setLivenessCheck(enabled: Boolean) {
        _state.update { it.copy(livenessCheckEnabled = enabled, hasUnsavedChanges = true) }
    }

    fun setConfidenceThreshold(value: Float) {
        _state.update { it.copy(confidenceThreshold = value, hasUnsavedChanges = true) }
    }

    fun setMaxEnrollmentAttempts(value: Int) {
        _state.update { it.copy(maxEnrollmentAttempts = value, hasUnsavedChanges = true) }
    }

    fun setSessionTimeout(minutes: Int) {
        _state.update { it.copy(sessionTimeoutMinutes = minutes, hasUnsavedChanges = true) }
    }

    fun setAutoLock(enabled: Boolean) {
        _state.update { it.copy(autoLockEnabled = enabled, hasUnsavedChanges = true) }
    }

    fun setNfcExamEntry(enabled: Boolean) {
        _state.update { it.copy(nfcExamEntryEnabled = enabled, hasUnsavedChanges = true) }
    }

    fun setInviteExpiryDays(days: Int) {
        _state.update { it.copy(inviteExpiryDays = days, hasUnsavedChanges = true) }
    }

    fun saveSettings() {
        val current = _state.value
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        val settings = TenantSettings(
            tenantName = current.tenantName,
            livenessCheckEnabled = current.livenessCheckEnabled,
            confidenceThreshold = current.confidenceThreshold,
            maxEnrollmentAttempts = current.maxEnrollmentAttempts,
            sessionTimeoutMinutes = current.sessionTimeoutMinutes,
            autoLockEnabled = current.autoLockEnabled,
            nfcExamEntryEnabled = current.nfcExamEntryEnabled,
            inviteExpiryDays = current.inviteExpiryDays
        )

        scope.launch {
            updateTenantSettingsUseCase(settings).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasUnsavedChanges = false,
                            successMessage = "Settings saved successfully"
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to save settings"
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
