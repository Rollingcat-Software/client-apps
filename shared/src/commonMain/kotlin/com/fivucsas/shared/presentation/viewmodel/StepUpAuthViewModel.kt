package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.FingerprintRepository
import com.fivucsas.shared.domain.repository.FingerprintStep
import com.fivucsas.shared.platform.FingerprintAuthException
import com.fivucsas.shared.presentation.state.StepUpAuthUiState
import com.fivucsas.shared.presentation.state.StepUpMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Step-Up Authentication screen (Phase 2.4).
 *
 * Handles method selection and delegates to the appropriate
 * verification flow (fingerprint, face, or TOTP).
 */
class StepUpAuthViewModel(
    private val fingerprintRepository: FingerprintRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(StepUpAuthUiState())
    val uiState: StateFlow<StepUpAuthUiState> = _uiState.asStateFlow()

    fun setReason(reason: String) {
        _uiState.update { it.copy(reason = reason) }
    }

    fun selectMethod(method: StepUpMethod) {
        _uiState.update { it.copy(selectedMethod = method, errorMessage = null) }
    }

    fun verify() {
        val method = _uiState.value.selectedMethod ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }

            when (method) {
                StepUpMethod.FINGERPRINT -> verifyFingerprint()
                StepUpMethod.FACE -> {
                    // Face step-up delegates to the biometric processor.
                    // For now, report as not yet wired (placeholder for future integration).
                    _uiState.update {
                        it.copy(isVerifying = false, errorMessage = "Face step-up not yet available on this device.")
                    }
                }
                StepUpMethod.TOTP -> {
                    // TOTP step-up requires code input — handled by the screen UI directly.
                    // The screen calls verifyTotp(code) instead.
                    _uiState.update {
                        it.copy(isVerifying = false, errorMessage = "Please enter TOTP code.")
                    }
                }
            }
        }
    }

    private suspend fun verifyFingerprint() {
        fingerprintRepository.performStepUp { step ->
            val statusMsg = when (step) {
                FingerprintStep.RegisteringDevice -> "Registering device..."
                FingerprintStep.RequestingChallenge -> "Requesting challenge..."
                FingerprintStep.ScanningBiometric -> "Scan your fingerprint now..."
                FingerprintStep.VerifyingSignature -> "Verifying signature..."
            }
            // Keep verifying state, the progress is shown via the fingerprint platform UI
            _uiState.update { it.copy(isVerifying = true, errorMessage = null) }
        }.fold(
            onSuccess = { token ->
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        isSuccess = true,
                        stepUpToken = token,
                        errorMessage = null
                    )
                }
            },
            onFailure = { throwable ->
                val authError = throwable as? FingerprintAuthException
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        errorMessage = authError?.message ?: "Fingerprint verification failed."
                    )
                }
            }
        )
    }

    fun reset() {
        _uiState.update {
            StepUpAuthUiState(reason = it.reason)
        }
    }
}
