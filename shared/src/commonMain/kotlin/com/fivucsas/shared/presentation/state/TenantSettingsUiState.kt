package com.fivucsas.shared.presentation.state

data class TenantSettingsUiState(
    val tenantName: String = "",
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
