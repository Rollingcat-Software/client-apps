package com.fivucsas.mobile.presentation.login

import com.fivucsas.mobile.domain.model.User
import com.fivucsas.mobile.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    suspend fun login(email: String, password: String) {
        _state.value = LoginState(isLoading = true)

        loginUseCase(email, password).fold(
            onSuccess = { (user, _) ->
                _state.value = LoginState(
                    isLoading = false,
                    user = user,
                    isSuccess = true
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
}
