package com.fivucsas.shared.presentation.state

data class TotpUiState(
    val isLoading: Boolean = false,
    val isEnabled: Boolean = false,
    val otpAuthUri: String? = null,
    val secret: String? = null,
    val setupComplete: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
