package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthRepository
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
    private val pushService: IPushNotificationService,
    private val authRepository: AuthRepository
) {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    /**
     * Discover the active auth flow's primary step for APP_LOGIN.
     *
     * Called from `LoginScreen` on mount. While the call is in flight,
     * `flowDiscoveryLoading=true` and the screen shows a shimmer instead
     * of the credential form. On any failure (no flow, auth required, network)
     * `primaryStepMethod` stays `null` and the screen renders the legacy
     * PASSWORD form — the existing happy-path is therefore preserved.
     *
     * @param tenantId optional tenant scope. May be `null` when the app
     *                 has no remembered tenant (e.g. fresh install) — in
     *                 that case discovery is skipped.
     */
    suspend fun loadActiveFlow(tenantId: String?) {
        // Don't trigger discovery twice; once we know the answer, hold it.
        if (_state.value.primaryStepMethod != null) return
        _state.value = _state.value.copy(flowDiscoveryLoading = true)
        val step = try {
            authRepository.discoverPrimaryStep(
                operationType = "APP_LOGIN",
                tenantId = tenantId
            )
        } catch (_: Exception) {
            null
        }
        _state.value = _state.value.copy(
            flowDiscoveryLoading = false,
            // Default to "PASSWORD" so the UI branches deterministically.
            // Any unrecognised method type still flows through the
            // PASSWORD branch's fallback "method-not-supported" message.
            primaryStepMethod = step?.authMethod?.type?.takeIf { it.isNotBlank() } ?: "PASSWORD"
        )
    }

    suspend fun login(email: String, password: String) {
        _state.value = LoginState(isLoading = true)
        try {
            loginUseCase(email, password).fold(
                onSuccess = { loginResult ->
                    when (loginResult) {
                        is LoginResult.Authenticated -> {
                            val tokens = loginResult.tokens
                            // The server has AUTHENTICATED us and minted tokens —
                            // this is a committed success. Publish the Authenticated
                            // state FIRST, before any side effect, so a throw in
                            // offline-cache / push-token can NEVER flip a real 200
                            // success into a stuck button or a false failure (the
                            // Phase-0 "200 AUTHENTICATED → stuck/failed" regression —
                            // mirrors the v5.2.3 fix in MfaFlowViewModel).
                            _state.value = LoginState(
                                isLoading = false,
                                tokens = tokens,
                                isSuccess = true,
                                role = UserRole.fromString(tokens.role)
                            )
                            // Best-effort side effects — must not affect the verdict.
                            runCatching {
                                offlineCache.cacheLoginData(
                                    userId = tokens.userId,
                                    userName = tokens.userName,
                                    userEmail = tokens.userEmail,
                                    role = tokens.role
                                )
                            }
                            // Remember the tenant for next launch's flow discovery.
                            runCatching { offlineCache.cacheTenantId(tokens.tenantId) }
                            // Register FCM push token with the backend (fire-and-forget).
                            runCatching { registerPushToken(tokens.userId) }
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
        } catch (e: Exception) {
            // Never override a committed authentication. If we've already
            // published a success (server said AUTHENTICATED), a late throw
            // must not strand the user on a false failure or a stuck button.
            if (_state.value.isSuccess) return
            _state.value = LoginState(
                isLoading = false,
                error = mapErrorToUserMessage(e)
            )
        }
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

    fun resetState() {
        // Preserve discovered primary-step so the user doesn't see a
        // re-shimmer when navigating back from the MFA flow.
        val keep = _state.value
        _state.value = LoginState(
            primaryStepMethod = keep.primaryStepMethod,
            flowDiscoveryLoading = false
        )
    }
}