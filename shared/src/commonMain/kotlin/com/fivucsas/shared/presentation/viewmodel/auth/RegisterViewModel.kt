package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.usecase.auth.RegisterUseCase
import com.fivucsas.shared.presentation.state.RegisterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    suspend fun register(email: String, password: String, firstName: String, lastName: String) {
        _state.value = RegisterState(isLoading = true)

        registerUseCase(email, password, firstName, lastName).fold(
            onSuccess = { tokens ->
                _state.value = RegisterState(
                    isLoading = false,
                    tokens = tokens,
                    isSuccess = true,
                    role = UserRole.fromString(tokens.role)
                )
            },
            onFailure = { error ->
                _state.value = RegisterState(
                    isLoading = false,
                    error = error.message ?: "Registration failed"
                )
            }
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
