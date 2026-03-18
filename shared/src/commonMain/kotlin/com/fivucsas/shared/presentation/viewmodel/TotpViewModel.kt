package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.TotpRepository
import com.fivucsas.shared.presentation.state.TotpUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TotpViewModel(
    private val totpRepository: TotpRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(TotpUiState())
    val uiState: StateFlow<TotpUiState> = _uiState.asStateFlow()

    fun checkStatus(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            totpRepository.getStatus(userId).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(isLoading = false, isEnabled = result.enabled)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "check TOTP status")
                        )
                    }
                }
            )
        }
    }

    fun setup(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            totpRepository.setup(userId).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpAuthUri = result.otpAuthUri,
                            secret = result.secret,
                            successMessage = if (result.success) "TOTP setup initiated. Scan the QR code." else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "setup TOTP")
                        )
                    }
                }
            )
        }
    }

    fun verifySetup(userId: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            totpRepository.verifySetup(userId, code).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            setupComplete = result.success,
                            isEnabled = result.success,
                            successMessage = if (result.success) result.message.ifBlank { "TOTP verified and enabled" } else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "verify TOTP")
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun reset() {
        _uiState.value = TotpUiState()
    }
}
