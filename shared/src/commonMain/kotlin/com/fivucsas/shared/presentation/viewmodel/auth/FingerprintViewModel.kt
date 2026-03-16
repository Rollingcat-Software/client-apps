package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.repository.FingerprintRepository
import com.fivucsas.shared.domain.repository.FingerprintStep
import com.fivucsas.shared.platform.FingerprintAuthException
import com.fivucsas.shared.presentation.state.FingerprintUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FingerprintViewModel(
    private val fingerprintRepository: FingerprintRepository
) {
    private val _state = MutableStateFlow<FingerprintUiState>(FingerprintUiState.Idle)
    val state: StateFlow<FingerprintUiState> = _state.asStateFlow()

    suspend fun startStepUp() {
        _state.value = FingerprintUiState.Idle

        fingerprintRepository.performStepUp { step ->
            _state.value = when (step) {
                FingerprintStep.RegisteringDevice -> FingerprintUiState.RegisteringDevice
                FingerprintStep.RequestingChallenge -> FingerprintUiState.RequestingChallenge
                FingerprintStep.ScanningBiometric -> FingerprintUiState.ScanningBiometric
                FingerprintStep.VerifyingSignature -> FingerprintUiState.VerifyingSignature
            }
        }.fold(
            onSuccess = { token ->
                _state.value = FingerprintUiState.Success(token)
            },
            onFailure = { throwable ->
                val authError = throwable as? FingerprintAuthException
                _state.value = FingerprintUiState.Error(
                    message = authError?.message ?: "Fingerprint verification failed.",
                    recoverable = authError?.recoverable ?: true
                )
            }
        )
    }

    fun reset() {
        _state.value = FingerprintUiState.Idle
    }
}

