package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.VerificationFlow
import com.fivucsas.shared.domain.model.VerificationSession

data class VerificationUiState(
    val flows: List<VerificationFlow> = emptyList(),
    val sessions: List<VerificationSession> = emptyList(),
    val selectedSession: VerificationSession? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val statusFilter: String? = null
)
