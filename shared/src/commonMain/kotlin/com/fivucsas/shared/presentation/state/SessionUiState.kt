package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.AuthSession

data class SessionUiState(
    val sessions: List<AuthSession> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showRevokeDialog: Boolean = false,
    val sessionToRevoke: AuthSession? = null
)
