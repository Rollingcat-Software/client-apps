package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.platform.IPushNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
     */
    suspend fun verifyStep(method: String, data: Map<String, String> = emptyMap()) {
        _uiState.value = MfaFlowUiState.Verifying

        authRepository.verifyMfaStep(mfaSessionToken, method, data).fold(
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
                        // Move to next step — backend may send nextStep or currentStep
                        currentStep = response.nextStep
                            ?: response.currentStep
                            ?: (currentStep + 1)
                        totalSteps = response.totalSteps ?: totalSteps
                        availableMethods = response.availableMethods ?: availableMethods
                        _uiState.value = MfaFlowUiState.MethodSelection(
                            availableMethods = availableMethods,
                            currentStep = currentStep,
                            totalSteps = totalSteps
                        )
                    }

                    else -> {
                        _uiState.value = MfaFlowUiState.Error(
                            message = response.message ?: "Verification failed.",
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
        val message = error.message ?: return "Verification failed. Please try again."
        return when {
            "401" in message || "Unauthorized" in message -> "Invalid verification code."
            "429" in message || "Too many" in message -> "Too many attempts. Please wait."
            "timeout" in message.lowercase() -> "Connection timed out. Please try again."
            "expired" in message.lowercase() -> "MFA session expired. Please log in again."
            else -> "Verification failed. Please try again."
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
