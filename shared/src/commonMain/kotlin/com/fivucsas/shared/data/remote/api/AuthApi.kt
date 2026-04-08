package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.ChangePasswordRequestDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaSendOtpRequest
import com.fivucsas.shared.data.remote.dto.MfaStepRequest
import com.fivucsas.shared.data.remote.dto.MfaStepResponse
import com.fivucsas.shared.data.remote.dto.RegisterRequestDto

/**
 * Auth API interface
 *
 * Defines contract for authentication service.
 *
 * Base URL: https://api.fivucsas.com/api/v1/
 *
 * Endpoints:
 * - POST /auth/login          → login()
 * - POST /auth/register       → register()
 * - POST /auth/logout         → logout()
 * - POST /auth/refresh        → refreshToken()
 * - POST /auth/mfa/step       → verifyMfaStep()
 * - POST /auth/mfa/send-otp   → sendMfaOtp()
 * - POST /auth/mfa/qr-generate → generateMfaQr()
 */
interface AuthApi {

    /**
     * Login
     * POST /auth/login
     * Returns MFA challenge (mfaRequired=true) or tokens directly.
     */
    suspend fun login(request: LoginRequestDto): AuthResponseDto

    /**
     * Register new user
     * POST /auth/register
     */
    suspend fun register(request: RegisterRequestDto): AuthResponseDto

    /**
     * Logout
     * POST /auth/logout
     */
    suspend fun logout()

    /**
     * Refresh token
     * POST /auth/refresh
     */
    suspend fun refreshToken(refreshToken: String): AuthResponseDto

    /**
     * Change password
     * POST /auth/change-password
     */
    suspend fun changePassword(request: ChangePasswordRequestDto)

    /**
     * Verify an MFA step
     * POST /auth/mfa/step (PUBLIC — no JWT required)
     */
    suspend fun verifyMfaStep(request: MfaStepRequest): MfaStepResponse

    /**
     * Send OTP for MFA
     * POST /auth/mfa/send-otp (PUBLIC — no JWT required)
     */
    suspend fun sendMfaOtp(request: MfaSendOtpRequest)

    /**
     * Generate QR token for MFA
     * POST /auth/mfa/qr-generate (PUBLIC — no JWT required)
     */
    suspend fun generateMfaQr(sessionToken: String): MfaQrTokenResponse
}
