package com.fivucsas.shared.presentation.viewmodel.auth

import com.fivucsas.shared.data.local.OfflineCache
import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.data.remote.dto.MfaChallengeData
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodResponse
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.StringResources
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.IPushNotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    private var alternativeMethods: List<AvailableMethodDto> = emptyList()
    private var currentStep: Int = 1
    private var totalSteps: Int = 1
    private val usedMethods: MutableSet<String> = mutableSetOf()
    private var expectedMethod: String? = null

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
                            // The server has AUTHENTICATED us and minted tokens —
                            // this is a committed success. Publish the auth result
                            // and Authenticated state FIRST, before any side effect,
                            // so a throw in offline-cache / push-token can NEVER flip
                            // a real 200 success into MFA_GENERIC_ERROR (the v5.2.2
                            // false-failure regression).
                            _authResult.value = MfaAuthResult(
                                tokens = tokens,
                                role = UserRole.fromString(tokens.role)
                            )
                            _uiState.value = MfaFlowUiState.Authenticated(
                                userId = tokens.userId
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
                            // Register FCM push token (fire-and-forget).
                            runCatching { registerPushToken(tokens.userId) }
                        }

                        "STEP_COMPLETED" -> {
                            // Track used method so it can be excluded from next step
                            usedMethods.add(method)
                            // Server-side authoritative completedMethods overrides the local set
                            response.completedMethods?.let {
                                usedMethods.clear(); usedMethods.addAll(it)
                            }
                            // Move to next step — backend may send nextStep or currentStep
                            currentStep = response.nextStep
                                ?: response.currentStep
                                ?: (currentStep + 1)
                            totalSteps = response.totalSteps ?: totalSteps
                            expectedMethod = response.expectedMethod
                            // Merge backend list, then filter out already-used methods
                            val backendMethods = response.availableMethods ?: availableMethods
                            availableMethods = backendMethods.filter { it.methodType !in usedMethods }
                            // PR #25: per-step alternativeMethods come from the server.
                            alternativeMethods = response.alternativeMethods
                                ?.filter { it.methodType !in usedMethods }
                                ?: emptyList()
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
                    val parsed = parseErrorEnvelope(error)
                    if (parsed != null && parsed.errorCode == "NEEDS_ENROLLMENT") {
                        handleNeedsEnrollment(
                            method = parsed.method ?: method,
                            enrollmentUrl = parsed.enrollmentUrl ?: "",
                            description = parsed.message
                        )
                    } else {
                        _uiState.value = MfaFlowUiState.Error(
                            message = parsed?.let { mapErrorCodeMessage(it.errorCode, it.message) }
                                ?: mapErrorMessage(error),
                            canRetry = true
                        )
                    }
                }
            )
        } catch (e: Exception) {
            // Never override a committed authentication. If we've already
            // produced an auth result (server said AUTHENTICATED), a late
            // throw must not strand the user on a false "Verification failed".
            if (_authResult.value != null) return
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
     * Request a WebAuthn challenge for the current FINGERPRINT or HARDWARE_KEY step.
     * The caller is expected to drive the platform Credential Manager / authenticator
     * with the returned challenge and then submit the assertion via [verifyStep].
     */
    suspend fun requestStepUpChallenge(method: String): Result<MfaChallengeData> {
        return authRepository.requestMfaChallenge(mfaSessionToken, method)
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

    /**
     * Cancel the in-progress MFA session on the server and transition to a
     * terminal Cancelled state. Maps to `DELETE /auth/mfa/session/{token}`
     * (PR #25). Network/server errors are intentionally swallowed —
     * cancellation is best-effort and the user must always be able to leave
     * the screen.
     */
    suspend fun cancelSession() {
        val token = mfaSessionToken
        if (token.isNotBlank()) {
            // Best-effort — ignore failures so the UI can always navigate back.
            authRepository.cancelMfaSession(token)
        }
        _uiState.value = MfaFlowUiState.Cancelled
    }

    /**
     * Ask the server to switch the active method for the current step.
     * Maps to `POST /auth/mfa/switch-method` (PR #25).
     *
     * On success the UI returns to method selection with the updated step
     * snapshot. On a 409 the server-supplied `errorCode` is mapped to a
     * dedicated UI message (or the NEEDS_ENROLLMENT overlay).
     */
    suspend fun switchMethod(newMethod: String) {
        val previousState = _uiState.value
        _uiState.value = MfaFlowUiState.Verifying

        val result = authRepository.switchMfaMethod(mfaSessionToken, newMethod)
        result.fold(
            onSuccess = { response ->
                if (response.status == "METHOD_SWITCHED") {
                    applySwitchSuccess(response)
                } else {
                    when (response.errorCode) {
                        "NEEDS_ENROLLMENT" -> handleNeedsEnrollment(
                            method = newMethod,
                            enrollmentUrl = response.enrollmentUrl ?: "",
                            description = response.message
                        )
                        else -> {
                            _uiState.value = MfaFlowUiState.Error(
                                message = mapErrorCodeMessage(response.errorCode, response.message),
                                canRetry = true
                            )
                        }
                    }
                }
            },
            onFailure = { error ->
                val parsed = parseErrorEnvelope(error)
                if (parsed?.errorCode == "NEEDS_ENROLLMENT") {
                    handleNeedsEnrollment(
                        method = parsed.method ?: newMethod,
                        enrollmentUrl = parsed.enrollmentUrl ?: "",
                        description = parsed.message
                    )
                } else {
                    // Restore the previous state so the user can pick again.
                    _uiState.value = previousState
                    _uiState.value = MfaFlowUiState.Error(
                        message = parsed?.let { mapErrorCodeMessage(it.errorCode, it.message) }
                            ?: mapErrorMessage(error),
                        canRetry = true
                    )
                }
            }
        )
    }

    private fun applySwitchSuccess(response: MfaSwitchMethodResponse) {
        currentStep = response.currentStep ?: currentStep
        totalSteps = response.totalSteps ?: totalSteps
        expectedMethod = response.expectedMethod
        response.completedMethods?.let { usedMethods.clear(); usedMethods.addAll(it) }
        val backendMethods = response.availableMethods ?: availableMethods
        availableMethods = backendMethods.filter { it.methodType !in usedMethods }
        alternativeMethods = response.alternativeMethods
            ?.filter { it.methodType !in usedMethods }
            ?: emptyList()
        // After a successful switch, jump straight to step input for the
        // new expected method so the user can submit a code immediately.
        val target = response.expectedMethod
        if (target != null) {
            _uiState.value = MfaFlowUiState.StepInput(
                method = target,
                currentStep = currentStep,
                totalSteps = totalSteps
            )
        } else {
            _uiState.value = MfaFlowUiState.MethodSelection(
                availableMethods = availableMethods,
                currentStep = currentStep,
                totalSteps = totalSteps
            )
        }
    }

    /**
     * Surface a NEEDS_ENROLLMENT envelope as a dedicated full-screen state
     * the UI can render with an "Enroll now" button that opens
     * [enrollmentUrl] in a Custom Tab.
     */
    fun handleNeedsEnrollment(
        method: String,
        enrollmentUrl: String,
        description: String? = null
    ) {
        _uiState.value = MfaFlowUiState.NeedsEnrollment(
            method = method,
            enrollmentUrl = enrollmentUrl,
            description = description
        )
    }

    /**
     * Read-only snapshot of the latest server-supplied alternative methods
     * for the current step. The screen uses this to render the
     * "Try a different method" surface.
     */
    fun currentAlternativeMethods(): List<AvailableMethodDto> = alternativeMethods

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

    /**
     * Map a server `errorCode` (METHOD_NOT_PERMITTED / METHOD_ALREADY_USED /
     * NEEDS_ENROLLMENT) to a localized user-facing message. Falls back to the
     * server-supplied `serverMessage` then to the generic MFA error.
     */
    private fun mapErrorCodeMessage(errorCode: String?, serverMessage: String?): String {
        return when (errorCode) {
            "METHOD_NOT_PERMITTED" -> s(StringKey.MFA_METHOD_NOT_PERMITTED)
            "METHOD_ALREADY_USED" -> s(StringKey.MFA_METHOD_ALREADY_USED)
            "NEEDS_ENROLLMENT" -> StringResources.get(
                StringKey.MFA_NEEDS_ENROLLMENT_DESC,
                serverMessage ?: ""
            )
            else -> serverMessage?.takeIf { it.isNotBlank() } ?: s(StringKey.MFA_GENERIC_ERROR)
        }
    }

    /**
     * Parse a JSON-shaped error envelope out of an exception message produced
     * by [com.fivucsas.shared.data.remote.api.AuthApiImpl] (which formats
     * non-2xx as "{statusCode} {responseBody}"). Returns null if the body is
     * not parseable or doesn't carry an `error`/`errorCode` field.
     *
     * The server contract (PR #25) is:
     * {
     *   "timestamp": "...", "status": 400, "error": "NEEDS_ENROLLMENT",
     *   "message": "...", "method": "TOTP", "enrollmentUrl": "/enroll/totp",
     *   "path": "..."
     * }
     */
    internal fun parseErrorEnvelope(error: Throwable): MfaErrorEnvelope? {
        val raw = error.message ?: return null
        val jsonStart = raw.indexOf('{')
        if (jsonStart < 0) return null
        val jsonText = raw.substring(jsonStart)
        return try {
            val parsed = mfaErrorJson.parseToJsonElement(jsonText).jsonObject
            // Server uses `error` for the code; some flows may use `errorCode`.
            val code = parsed["error"]?.jsonPrimitive?.contentOrNull
                ?: parsed["errorCode"]?.jsonPrimitive?.contentOrNull
            MfaErrorEnvelope(
                errorCode = code,
                method = parsed["method"]?.jsonPrimitive?.contentOrNull,
                enrollmentUrl = parsed["enrollmentUrl"]?.jsonPrimitive?.contentOrNull,
                message = parsed["message"]?.jsonPrimitive?.contentOrNull
            )
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val mfaErrorJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
    }
}

/**
 * Parsed shape of the server's 4xx error envelope for MFA endpoints.
 */
data class MfaErrorEnvelope(
    val errorCode: String?,
    val method: String?,
    val enrollmentUrl: String?,
    val message: String?
)

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

    /** MFA session was cancelled by the user (DELETE /mfa/session). */
    object Cancelled : MfaFlowUiState()

    /**
     * The user picked (or the server expects) a method that the user hasn't
     * enrolled. Surface a full-screen state with an "Enroll now" CTA that
     * opens [enrollmentUrl] (Custom Tab on Android).
     */
    data class NeedsEnrollment(
        val method: String,
        val enrollmentUrl: String,
        val description: String? = null
    ) : MfaFlowUiState()

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
