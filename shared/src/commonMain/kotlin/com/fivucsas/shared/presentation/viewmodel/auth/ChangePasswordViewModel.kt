package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.usecase.auth.ChangePasswordUseCase
import com.fivucsas.shared.presentation.state.ChangePasswordUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import com.fivucsas.shared.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        _state.update { ChangePasswordUiState(isLoading = true) }

        viewModelScope.launch {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword).fold(
                onSuccess = {
                    _state.update { ChangePasswordUiState(isSuccess = true) }
                },
                onFailure = { error ->
                    _state.update {
                        ChangePasswordUiState(
                            errorMessage = ErrorMapper.mapToUserMessage(error, "change password")
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
