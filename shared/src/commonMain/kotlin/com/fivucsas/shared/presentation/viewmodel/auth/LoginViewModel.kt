package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.LoginResult
import com.fivucsas.shared.domain.usecase.auth.LoginUseCase
import com.fivucsas.shared.platform.IPushNotificationService
import com.fivucsas.shared.presentation.state.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val offlineCache: OfflineCache,
    private val pushService: IPushNotificationService
) {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    suspend fun login(email: String, password: String) {
        _state.value = LoginState(isLoading = true)
        loginUseCase(email, password).fold(
            onSuccess = { loginResult ->
                when (loginResult) {
                    is LoginResult.Authenticated -> {
                        val tokens = loginResult.tokens
                        // Cache login data for offline mode
                        offlineCache.cacheLoginData(
                            userId = tokens.userId,
                            userName = tokens.userName,
                            userEmail = tokens.userEmail,
                            role = tokens.role
                        )
                        _state.value = LoginState(
                            isLoading = false,
                            tokens = tokens,
                            isSuccess = true,
                            role = UserRole.fromString(tokens.role)
                        )

                        // Register FCM push token with the backend (fire-and-forget)
                        registerPushToken(tokens.userId)
                    }

                    is LoginResult.MfaChallenge -> {
                        _state.value = LoginState(
                            isLoading = false,
                            mfaRequired = true,
                            mfaSessionToken = loginResult.mfaSessionToken,
                            mfaAvailableMethods = loginResult.availableMethods,
                            mfaCurrentStep = loginResult.currentStep,
                            mfaTotalSteps = loginResult.totalSteps
                        )
                    }
                }
            },
            onFailure = { error ->
                _state.value = LoginState(
                    isLoading = false,
                    error = mapErrorToUserMessage(error)
                )
            }
        )
    }

    /**
     * Register the device push token with the backend after login.
     * Best-effort: failures are silently ignored (user can still use the app).
     */
    private suspend fun registerPushToken(userId: String) {
        if (!pushService.isSupported()) return
        try {
            val token = pushService.getToken() ?: return
            pushService.registerToken(userId, token)
        } catch (_: Exception) {
            // Non-critical — push notifications are a convenience feature
        }
    }

    /**
     * Map technical exceptions to user-friendly error messages.
     * Prevents raw serialization / network errors from leaking to UI.
     */
    private fun mapErrorToUserMessage(error: Throwable): String {
        val message = error.message ?: return "Login failed. Please try again."
        return when {
            // HTTP 401 / 403
            "401" in message || "Unauthorized" in message ->
                "Invalid email or password."
            "403" in message || "Forbidden" in message ->
                "Your account does not have access. Contact your administrator."
            // HTTP 429 rate limit
            "429" in message || "Rate Limit" in message || "Too many" in message ->
                "Too many login attempts. Please wait and try again."
            // Network errors
            "UnresolvedAddressException" in message || "ConnectException" in message
                || "Unable to resolve host" in message || "No address" in message ->
                "Cannot reach the server. Check your internet connection."
            "timeout" in message.lowercase() || "Timeout" in message ->
                "Connection timed out. Please try again."
            // Serialization errors (should not happen after DTO fix, but just in case)
            "Illegal input" in message || "serializ" in message.lowercase()
                || "JsonDecodingException" in message || "MissingFieldException" in message ->
                "Unexpected server response. Please update the app or try again later."
            // Generic fallback
            else -> "Login failed. Please try again."
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState(){_state.value=LoginState()}
}