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
                    error = mapErrorToUserMessage(error)
                )
            }
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun mapErrorToUserMessage(error: Throwable): String {
        val message = error.message ?: return "Registration failed. Please try again."
        return when {
            "409" in message || "Conflict" in message || "already exists" in message.lowercase() ->
                "An account with this email already exists."
            "400" in message || "Bad Request" in message ->
                "Please check your input and try again."
            "ConnectException" in message || "UnresolvedAddressException" in message ->
                "Cannot reach the server. Check your internet connection."
            "timeout" in message.lowercase() ->
                "Connection timed out. Please try again."
            "Illegal input" in message || "serializ" in message.lowercase() ->
                "Unexpected server response. Please update the app or try again later."
            else -> "Registration failed. Please try again."
        }
    }
}
