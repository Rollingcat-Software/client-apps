package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.BiometricCapability
import com.fivucsas.shared.domain.model.BiometricError
import com.fivucsas.shared.domain.model.BiometricStepUpException
import com.fivucsas.shared.domain.model.StepUpDto
import com.fivucsas.shared.domain.usecase.auth.BiometricStepUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SecuritySettingsUiState(
    val isLoading: Boolean = false,
    val capability: BiometricCapability? = null,
    val isRegistered: Boolean = false,
    val lastStepUp: StepUpDto? = null,
    val error: String? = null,
    val lastActionMessage: String? = null
)

class SecuritySettingsViewModel(
    private val stepUpUseCase: BiometricStepUpUseCase
) {
    private val _state = MutableStateFlow(
        SecuritySettingsUiState(
            isRegistered = stepUpUseCase.isDeviceRegistered(),
            lastStepUp = stepUpUseCase.getCurrentStepUpToken()
        )
    )
    val state: StateFlow<SecuritySettingsUiState> = _state.asStateFlow()

    suspend fun loadCapability() {
        _state.value = _state.value.copy(isLoading = true, error = null, lastActionMessage = null)
        runCatching { stepUpUseCase.canAuthenticate() }
            .onSuccess { capability ->
                _state.value = _state.value.copy(isLoading = false, capability = capability)
            }
            .onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    capability = BiometricCapability.UnknownError,
                    error = throwable.message ?: "Failed to read biometric capability."
                )
            }
    }

    suspend fun registerDevice(deviceLabel: String?) {
        _state.value = _state.value.copy(isLoading = true, error = null, lastActionMessage = null)
        runCatching { stepUpUseCase.ensureRegisteredDevice(deviceLabel) }
            .onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRegistered = true,
                    lastActionMessage = "Device registered."
                )
            }
            .onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = resolveErrorMessage(throwable)
                )
            }
    }

    suspend fun stepUp(reason: String) {
        _state.value = _state.value.copy(isLoading = true, error = null, lastActionMessage = null)
        runCatching { stepUpUseCase.stepUp(reason) }
            .onSuccess { stepUp ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    lastStepUp = stepUp,
                    lastActionMessage = "Step-up succeeded."
                )
            }
            .onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = resolveErrorMessage(throwable)
                )
            }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun resolveErrorMessage(throwable: Throwable): String {
        val error = (throwable as? BiometricStepUpException)?.error
        return when (error) {
            BiometricError.Canceled -> "Biometric prompt canceled."
            BiometricError.Lockout -> "Biometric is locked out. Try again later."
            BiometricError.NotEnrolled -> "No biometric enrolled on this device."
            BiometricError.NoHardware -> "Biometric hardware unavailable."
            BiometricError.KeyInvalidated -> "Biometric key invalidated. Register device again."
            BiometricError.Failed -> "Biometric verification failed."
            is BiometricError.Unknown -> error.message ?: "Unknown biometric error."
            null -> throwable.message ?: "Operation failed."
        }
    }
}
