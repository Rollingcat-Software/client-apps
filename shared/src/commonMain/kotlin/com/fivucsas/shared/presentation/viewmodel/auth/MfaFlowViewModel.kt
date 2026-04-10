package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.IPushNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * MFA Flow ViewModel
 *
 * Manages the N-step MFA verification flow after login returns mfaRequired=true.
 * Handles method selection, step input, OTP sending, QR generation, and
 * transitions between steps until authentication is complete.
 */
class MfaFlowViewModel(
    private val authRepository: AuthRepository,
    private val offlineCache: OfflineCache,
    private val pushService: IPushNotificationService
) {
    private val _uiState = MutableStateFlow<MfaFlowUiState>(MfaFlowUiState.Idle)
    val uiState: StateFlow<MfaFlowUiState> = _uiState.asStateFlow()

    private var mfaSessionToken: String = ""
    private var availableMethods: List<AvailableMethodDto> = emptyList()
    private var currentStep: Int = 1
    private var totalSteps: Int = 1
    private val usedMethods: MutableSet<String> = mutableSetOf()

    // Expose tokens and role after successful authentication
    private val _authResult = MutableStateFlow<MfaAuthResult?>(null)
    val authResult: StateFlow<MfaAuthResult?> = _authResult.asStateFlow()

    /**
     * Initialize the MFA flow with data from the login response.
     */
    fun initialize(
        sessionToken: String,
        methods: List<AvailableMethodDto>,
        step: Int,
        total: Int
    ) {
        mfaSessionToken = sessionToken
        availableMethods = methods
        currentStep = step
        totalSteps = total
        usedMethods.clear()
        _uiState.value = MfaFlowUiState.MethodSelection(
            availableMethods = availableMethods,
            currentStep = currentStep,
            totalSteps = totalSteps
        )
    }

    /**
     * User selects a method to verify.
     */
    fun selectMethod(method: String) {
        _uiState.value = MfaFlowUiState.StepInput(
            method = method,
            currentStep = currentStep,
            totalSteps = totalSteps
        )
    }

    /**
     * Go back to method selection from step input.
     */
    fun backToMethodSelection() {
        _uiState.value = MfaFlowUiState.MethodSelection(
            availableMethods = availableMethods,
            currentStep = currentStep,
            totalSteps = totalSteps
        )
    }

    /**
     * Verify an MFA step with the given method and data.
     * Applies a 30-second timeout to prevent hanging on unresponsive network.
     */
    suspend fun verifyStep(method: String, data: Map<String, String> = emptyMap()) {
        _uiState.value = MfaFlowUiState.Verifying

        try {
            val result = withTimeoutOrNull(30_000L) {
                authRepository.verifyMfaStep(mfaSessionToken, method, data)
            }

            if (result == null) {
                // Timeout occurred
                _uiState.value = MfaFlowUiState.Error(
                    message = s(StringKey.MFA_TIMEOUT),
                    canRetry = true
                )
                return
            }

            result.fold(
                onSuccess = { response ->
                    when (response.status) {
                        "AUTHENTICATED" -> {
                            val tokens = response.toModel()
                            // Cache login data for offline mode
                            offlineCache.cacheLoginData(
                                userId = tokens.userId,
                                userName = tokens.userName,
                                userEmail = tokens.userEmail,
                                role = tokens.role
                            )
                            _authResult.value = MfaAuthResult(
                                tokens = tokens,
                                role = UserRole.fromString(tokens.role)
                            )
                            _uiState.value = MfaFlowUiState.Authenticated(
                                userId = tokens.userId
                            )
                            // Register FCM push token (fire-and-forget)
                            registerPushToken(tokens.userId)
                        }

                        "STEP_COMPLETED" -> {
                            // Track used method so it can be excluded from next step
                            usedMethods.add(method)
                            // Move to next step — backend may send nextStep or currentStep
                            currentStep = response.nextStep
                                ?: response.currentStep
                                ?: (currentStep + 1)
                            totalSteps = response.totalSteps ?: totalSteps
                            // Merge backend list, then filter out already-used methods
                            val backendMethods = response.availableMethods ?: availableMethods
                            availableMethods = backendMethods.filter { it.methodType !in usedMethods }
                            _uiState.value = MfaFlowUiState.MethodSelection(
                                availableMethods = availableMethods,
                                currentStep = currentStep,
                                totalSteps = totalSteps
                            )
                        }

                        else -> {
                            _uiState.value = MfaFlowUiState.Error(
                                message = response.message ?: s(StringKey.MFA_GENERIC_ERROR),
                                canRetry = true
                            )
                        }
                    }
                },
                onFailure = { error ->
                    _uiState.value = MfaFlowUiState.Error(
                        message = mapErrorMessage(error),
                        canRetry = true
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = MfaFlowUiState.Error(
                message = s(StringKey.MFA_GENERIC_ERROR),
                canRetry = true
            )
        }
    }

    /**
     * Send OTP for EMAIL_OTP or SMS_OTP methods.
     */
    suspend fun sendOtp(method: String): Result<Unit> {
        return authRepository.sendMfaOtp(mfaSessionToken, method)
    }

    /**
     * Generate QR code for QR_CODE method.
     */
    suspend fun generateQr(): Result<MfaQrTokenResponse> {
        return authRepository.generateMfaQr(mfaSessionToken)
    }

    /**
     * Retry after error — go back to method selection.
     */
    fun retry() {
        _uiState.value = MfaFlowUiState.MethodSelection(
            availableMethods = availableMethods,
            currentStep = currentStep,
            totalSteps = totalSteps
        )
    }

    private suspend fun registerPushToken(userId: String) {
        if (!pushService.isSupported()) return
        try {
            val token = pushService.getToken() ?: return
            pushService.registerToken(userId, token)
        } catch (_: Exception) {
            // Non-critical
        }
    }

    private fun mapErrorMessage(error: Throwable): String {
        val message = error.message ?: return s(StringKey.MFA_GENERIC_ERROR)
        return when {
            "401" in message || "Unauthorized" in message -> s(StringKey.MFA_INVALID_CODE)
            "429" in message || "Too many" in message -> s(StringKey.MFA_TOO_MANY_ATTEMPTS)
            "timeout" in message.lowercase() -> s(StringKey.MFA_TIMEOUT)
            "expired" in message.lowercase() -> s(StringKey.MFA_SESSION_EXPIRED)
            else -> s(StringKey.MFA_GENERIC_ERROR)
        }
    }
}

/**
 * MFA flow UI states
 */
sealed class MfaFlowUiState {
    /** Initial state before initialization */
    object Idle : MfaFlowUiState()

    /** Show available methods for the current step */
    data class MethodSelection(
        val availableMethods: List<AvailableMethodDto>,
        val currentStep: Int,
        val totalSteps: Int
    ) : MfaFlowUiState()

    /** Show input UI for the selected method */
    data class StepInput(
        val method: String,
        val currentStep: Int,
        val totalSteps: Int
    ) : MfaFlowUiState()

    /** Verification in progress */
    object Verifying : MfaFlowUiState()

    /** All steps completed, user is authenticated */
    data class Authenticated(val userId: String) : MfaFlowUiState()

    /** Error during verification */
    data class Error(
        val message: String,
        val canRetry: Boolean
    ) : MfaFlowUiState()
}

/**
 * Result holder for tokens after MFA authentication completes.
 */
data class MfaAuthResult(
    val tokens: AuthTokens,
    val role: UserRole
)
