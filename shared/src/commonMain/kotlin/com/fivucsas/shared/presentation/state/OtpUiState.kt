package com.fivucsas.shared.presentation.state

data class OtpUiState(
    val isLoading: Boolean = false,
    val otpSent: Boolean = false,
    val otpVerified: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
