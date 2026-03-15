package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.presentation.state.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    companion object {
        var devMockRole: UserRole = UserRole.ROOT
    }

    suspend fun login(email: String, password: String) {
//        _state.value = LoginState(isLoading = true)
//        loginUseCase(email, password).fold(
//            onSuccess = { tokens ->
//                _state.value = LoginState(
//                    isLoading = false,
//                    tokens = tokens,
//                    isSuccess = true,
//                    role = UserRole.fromString(tokens.role)
//                )
//            },
//            onFailure = { error ->
//                _state.value = LoginState(
//                    isLoading = false,
//                    error = error.message ?: "Login failed"
//                )
//            }
//        )
        // TODO: Geçici olarak doğrulama atlanıyor - backend hazır olduğunda kaldırılacak
        _state.value = LoginState(
            isLoading = false,
            tokens = AuthTokens(
                accessToken = "dev-token",
                refreshToken = "dev-refresh",
                expiresIn = 3600,
                role = devMockRole.name
            ),
            isSuccess = true,
            role = devMockRole
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState() {
        _state.value = LoginState()
    }
}
