package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaStepResponse

/**
 * Authentication repository interface
 *
 * Handles user authentication and token management
 */
interface AuthRepository {
    /**
     * Login user
     * @param email User email
     * @param password User password
     * @return Result with login result (tokens or MFA challenge)
     */
    suspend fun login(email: String, password: String): Result<LoginResult>

    /**
     * Register new user
     * @param email User email
     * @param password User password
     * @param firstName User first name
     * @param lastName User last name
     * @return Result with auth tokens or error
     */
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens>

    /**
     * Logout user
     * @return Result with success or error
     */
    suspend fun logout(): Result<Unit>

    /**
     * Refresh access token
     * @param refreshToken Refresh token
     * @return Result with new tokens or error
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthTokens>

    /**
     * Change password
     * @param currentPassword Current password
     * @param newPassword New password
     * @return Result with success or error
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>

    /**
     * Check if user is authenticated
     * @return True if authenticated
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Get current access token
     * @return Access token or null
     */
    suspend fun getAccessToken(): String?

    /**
     * Verify an MFA step
     * @return MfaStepResponse with status and optional tokens
     */
    suspend fun verifyMfaStep(
        sessionToken: String,
        method: String,
        data: Map<String, String> = emptyMap()
    ): Result<MfaStepResponse>

    /**
     * Send OTP for MFA (EMAIL_OTP or SMS_OTP)
     */
    suspend fun sendMfaOtp(sessionToken: String, method: String): Result<Unit>

    /**
     * Generate QR token for MFA QR_CODE method
     */
    suspend fun generateMfaQr(sessionToken: String): Result<MfaQrTokenResponse>
}

/**
 * Login result — either direct tokens or an MFA challenge.
 */
sealed class LoginResult {
    data class Authenticated(val tokens: AuthTokens) : LoginResult()
    data class MfaChallenge(
        val mfaSessionToken: String,
        val availableMethods: List<AvailableMethodDto>,
        val currentStep: Int,
        val totalSteps: Int
    ) : LoginResult()
}

/**
 * Authentication tokens
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val role: String = "USER",
    val userName: String = "",
    val userEmail: String = "",
    val userId: String = "",
    val tenantId: String = ""
)
