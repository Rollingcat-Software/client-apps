package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.AuthFlow

data class AuthFlowUiState(
    val flows: List<AuthFlow> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
