package com.fivucsas.shared.presentation.state

sealed class FingerprintUiState {
    data object Idle : FingerprintUiState()
    data object RegisteringDevice : FingerprintUiState()
    data object RequestingChallenge : FingerprintUiState()
    data object ScanningBiometric : FingerprintUiState()
    data object VerifyingSignature : FingerprintUiState()
    data class Success(val stepUpToken: String) : FingerprintUiState()
    data class Error(val message: String, val recoverable: Boolean) : FingerprintUiState()
}
