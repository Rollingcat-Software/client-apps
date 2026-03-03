package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.IdentifyResult

data class IdentifyUiState(
    val isLoading: Boolean = false,
    val result: IdentifyResult? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
