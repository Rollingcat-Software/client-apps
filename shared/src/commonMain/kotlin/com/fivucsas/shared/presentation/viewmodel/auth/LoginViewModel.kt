package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tokens: AuthTokens? = null,
    val isSuccess: Boolean = false,
    val role: UserRole? = null
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    suspend fun login(email: String, password: String) {
        _state.value = LoginState(isLoading = true)
        loginUseCase(email, password).fold(
            onSuccess = { tokens ->
                _state.value = LoginState(
                    isLoading = false,
                    tokens = tokens,
                    isSuccess = true,
                    role = UserRole.fromString(tokens.role)
                )
            },
            onFailure = { error ->
                _state.value = LoginState(
                    isLoading = false,
                    error = error.message ?: "Login failed"
                )
            }
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState() {
        _state.value = LoginState()
    }
}
