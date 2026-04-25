package com.fivucsas.shared.test

import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.data.remote.dto.MfaChallengeData
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaStepResponse
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodResponse
import com.fivucsas.shared.domain.model.AuthFlowStep
import com.fivucsas.shared.domain.model.AuthMethodInfo
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.repository.LoginResult

class FakeAuthRepository : AuthRepository {

    var shouldSucceed = true
    var errorMessage = "Test error"
    var loginCalled = false
    var lastLoginEmail: String? = null
    var lastLoginPassword: String? = null
    var mockAccessToken = "fake-access-token"
    var mockRefreshToken = "fake-refresh-token"
    var mockRole = "USER"

    /** When true, login() returns an MFA challenge instead of direct auth. */
    var mfaRequired = false
    var mockMfaSessionToken = "fake-mfa-session-token"
    var mockMfaAvailableMethods = listOf(
        AvailableMethodDto(methodType = "TOTP", name = "Authenticator App", category = "OTP", enrolled = true, preferred = true)
    )

    override suspend fun login(email: String, password: String): Result<LoginResult> {
        loginCalled = true
        lastLoginEmail = email
        lastLoginPassword = password
        return if (shouldSucceed) {
            if (mfaRequired) {
                Result.success(
                    LoginResult.MfaChallenge(
                        mfaSessionToken = mockMfaSessionToken,
                        availableMethods = mockMfaAvailableMethods,
                        currentStep = 1,
                        totalSteps = 2
                    )
                )
            } else {
                Result.success(
                    LoginResult.Authenticated(
                        AuthTokens(
                            accessToken = mockAccessToken,
                            refreshToken = mockRefreshToken,
                            expiresIn = 3600,
                            role = mockRole
                        )
                    )
                )
            }
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens> {
        return if (shouldSucceed) {
            Result.success(
                AuthTokens(
                    accessToken = mockAccessToken,
                    refreshToken = mockRefreshToken,
                    expiresIn = 3600,
                    role = mockRole
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return if (shouldSucceed) {
            Result.success(
                AuthTokens(
                    accessToken = "refreshed-token",
                    refreshToken = refreshToken,
                    expiresIn = 3600,
                    role = mockRole
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun isAuthenticated(): Boolean = shouldSucceed

    override suspend fun getAccessToken(): String? = if (shouldSucceed) mockAccessToken else null

    override suspend fun verifyMfaStep(
        sessionToken: String,
        method: String,
        data: Map<String, String>
    ): Result<MfaStepResponse> {
        return if (shouldSucceed) {
            Result.success(
                MfaStepResponse(
                    status = "AUTHENTICATED",
                    accessToken = mockAccessToken,
                    refreshToken = mockRefreshToken,
                    expiresIn = 3600
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun requestMfaChallenge(
        sessionToken: String,
        method: String
    ): Result<MfaChallengeData> {
        return if (shouldSucceed) {
            Result.success(
                MfaChallengeData(
                    challenge = "fake-challenge",
                    rpId = "fivucsas.local",
                    timeout = "60000",
                    allowCredentials = emptyList()
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun sendMfaOtp(sessionToken: String, method: String): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun generateMfaQr(sessionToken: String): Result<MfaQrTokenResponse> {
        return if (shouldSucceed) {
            Result.success(MfaQrTokenResponse(qrToken = "fake-qr-token", expiresIn = 300))
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    // ---- PR #25 endpoints ----
    var cancelMfaSessionCalled = false
    var lastCancelledSessionToken: String? = null

    var switchMfaMethodCalled = false
    var lastSwitchedMethod: String? = null
    var mockSwitchMfaResponse: MfaSwitchMethodResponse = MfaSwitchMethodResponse(
        status = "METHOD_SWITCHED",
        currentStep = 1,
        totalSteps = 2,
        expectedMethod = "EMAIL_OTP",
        availableMethods = emptyList(),
        alternativeMethods = emptyList(),
        completedMethods = emptyList()
    )

    override suspend fun cancelMfaSession(sessionToken: String): Result<Unit> {
        cancelMfaSessionCalled = true
        lastCancelledSessionToken = sessionToken
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun switchMfaMethod(
        sessionToken: String,
        method: String
    ): Result<MfaSwitchMethodResponse> {
        switchMfaMethodCalled = true
        lastSwitchedMethod = method
        return if (shouldSucceed) Result.success(mockSwitchMfaResponse) else Result.failure(RuntimeException(errorMessage))
    }

    // ---- PR #18 client side: primary-step discovery ----
    var discoverPrimaryStepCalled = false
    var lastDiscoverOperationType: String? = null
    var lastDiscoverTenantId: String? = null
    /**
     * The auth method TYPE this fake should claim is the primary step,
     * or `null` to simulate "no flow configured" (the LoginScreen's
     * legacy PASSWORD fallback).
     */
    var mockPrimaryStepMethodType: String? = null

    override suspend fun discoverPrimaryStep(
        operationType: String,
        tenantId: String?
    ): AuthFlowStep? {
        discoverPrimaryStepCalled = true
        lastDiscoverOperationType = operationType
        lastDiscoverTenantId = tenantId
        val type = mockPrimaryStepMethodType ?: return null
        return AuthFlowStep(
            id = "fake-step",
            stepOrder = 1,
            authMethod = AuthMethodInfo(
                id = "fake-method",
                type = type,
                name = type,
                category = "FAKE",
                isActive = true
            )
        )
    }
}
