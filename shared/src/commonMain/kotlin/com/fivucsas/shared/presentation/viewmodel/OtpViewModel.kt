package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.OtpRepository
import com.fivucsas.shared.presentation.state.OtpUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpViewModel(
    private val otpRepository: OtpRepository
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun sendEmailOtp(userId: String, email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            otpRepository.sendEmailOtp(userId, email).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpSent = result.success,
                            successMessage = if (result.success) result.message.ifBlank { "OTP sent to your email" } else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "send email OTP")
                        )
                    }
                }
            )
        }
    }

    fun verifyEmailOtp(userId: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            otpRepository.verifyEmailOtp(userId, code).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpVerified = result.success,
                            successMessage = if (result.success) result.message.ifBlank { "Email OTP verified" } else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "verify email OTP")
                        )
                    }
                }
            )
        }
    }

    fun sendSmsOtp(userId: String, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            otpRepository.sendSmsOtp(userId, phoneNumber).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpSent = result.success,
                            successMessage = if (result.success) result.message.ifBlank { "SMS OTP sent" } else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "send SMS OTP")
                        )
                    }
                }
            )
        }
    }

    fun verifySmsOtp(userId: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            otpRepository.verifySmsOtp(userId, code).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpVerified = result.success,
                            successMessage = if (result.success) result.message.ifBlank { "SMS OTP verified" } else null,
                            errorMessage = if (!result.success) result.message else null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "verify SMS OTP")
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
        _uiState.value = OtpUiState()
    }
}
