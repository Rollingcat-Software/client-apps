package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.local.StepUpTokenManager
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.dto.ChangePasswordRequestDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import com.fivucsas.shared.data.remote.dto.MfaChallengeData
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaSendOtpRequest
import com.fivucsas.shared.data.remote.dto.MfaStepRequest
import com.fivucsas.shared.data.remote.dto.MfaStepResponse
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodRequest
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodResponse
import com.fivucsas.shared.data.remote.dto.RegisterRequestDto
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.AuthFlowStep
import com.fivucsas.shared.domain.repository.AuthFlowRepository
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.repository.LoginResult

/**
 * Real implementation of AuthRepository
 *
 * Connects to Identity Core API via AuthApi.
 * Manages tokens via TokenManager.
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val stepUpTokenManager: StepUpTokenManager? = null,
    private val authFlowRepository: AuthFlowRepository? = null
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<LoginResult> {
        return try {
            val request = LoginRequestDto(email = email, password = password)
            val response = authApi.login(request)

            if (response.mfaRequired) {
                // MFA challenge — do NOT store tokens yet
                Result.success(
                    LoginResult.MfaChallenge(
                        mfaSessionToken = response.mfaSessionToken ?: "",
                        availableMethods = response.availableMethods ?: emptyList(),
                        currentStep = response.currentStep ?: 1,
                        totalSteps = response.totalSteps ?: 1
                    )
                )
            } else {
                // Direct authentication — store tokens
                val tokens = response.toModel()
                tokenManager.saveTokens(tokens)
                Result.success(LoginResult.Authenticated(tokens))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens> {
        return try {
            val request = RegisterRequestDto(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
            val response = authApi.register(request)
            val tokens = response.toModel()

            // Store tokens in TokenManager
            tokenManager.saveTokens(tokens)

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authApi.logout()

            // Clear all sensitive data
            tokenManager.clearTokens()
            stepUpTokenManager?.clear()

            Result.success(Unit)
        } catch (e: Exception) {
            // Clear sensitive data even if API call fails
            tokenManager.clearTokens()
            stepUpTokenManager?.clear()
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return try {
            val response = authApi.refreshToken(refreshToken)
            val tokens = response.toModel()

            // Update tokens in TokenManager
            tokenManager.updateTokens(tokens)

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val request = ChangePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            authApi.changePassword(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    override suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    override suspend fun verifyMfaStep(
        sessionToken: String,
        method: String,
        data: Map<String, String>
    ): Result<MfaStepResponse> {
        return try {
            val request = MfaStepRequest(
                sessionToken = sessionToken,
                method = method,
                data = data
            )
            val response = authApi.verifyMfaStep(request)

            // If authenticated, store tokens
            if (response.status == "AUTHENTICATED" && response.accessToken != null) {
                val tokens = response.toModel()
                tokenManager.saveTokens(tokens)
            }

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestMfaChallenge(
        sessionToken: String,
        method: String
    ): Result<MfaChallengeData> {
        return try {
            val request = MfaStepRequest(
                sessionToken = sessionToken,
                method = method,
                data = mapOf("action" to "challenge")
            )
            val response = authApi.requestMfaChallenge(request)
            if (response.status == "CHALLENGE") {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unexpected response: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMfaOtp(sessionToken: String, method: String): Result<Unit> {
        return try {
            authApi.sendMfaOtp(MfaSendOtpRequest(sessionToken = sessionToken, method = method))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateMfaQr(sessionToken: String): Result<MfaQrTokenResponse> {
        return try {
            val response = authApi.generateMfaQr(sessionToken)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelMfaSession(sessionToken: String): Result<Unit> {
        return try {
            authApi.cancelMfaSession(sessionToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun switchMfaMethod(
        sessionToken: String,
        method: String
    ): Result<MfaSwitchMethodResponse> {
        return try {
            val response = authApi.switchMfaMethod(
                MfaSwitchMethodRequest(sessionToken = sessionToken, method = method)
            )
            // 409 envelopes still come back as Result.success — they carry an
            // errorCode the ViewModel maps to a precise UI message.
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun discoverPrimaryStep(
        operationType: String,
        tenantId: String?
    ): AuthFlowStep? {
        // Without an injected AuthFlowRepository we cannot discover — fall
        // back to "no flow" so the LoginScreen renders the legacy PASSWORD
        // form. This keeps the legacy DI graph (which does not yet wire
        // AuthFlowRepository into AuthRepositoryImpl) working unchanged.
        val flowRepo = authFlowRepository ?: return null
        return flowRepo.getActiveFlow(operationType, tenantId)
            .getOrNull()
            ?.steps
            ?.minByOrNull { it.stepOrder }
    }
}
