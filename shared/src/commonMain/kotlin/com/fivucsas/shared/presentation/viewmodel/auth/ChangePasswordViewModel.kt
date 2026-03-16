package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.usecase.auth.ChangePasswordUseCase
import com.fivucsas.shared.presentation.state.ChangePasswordUiState
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
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(ChangePasswordUiState())
    val state: StateFlow<ChangePasswordUiState> = _state.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        _state.update { ChangePasswordUiState(isLoading = true) }

        scope.launch {
            changePasswordUseCase(currentPassword, newPassword, confirmPassword).fold(
                onSuccess = {
                    _state.update { ChangePasswordUiState(isSuccess = true) }
                },
                onFailure = { error ->
                    _state.update {
                        ChangePasswordUiState(
                            errorMessage = error.message ?: "Failed to change password"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }
}
