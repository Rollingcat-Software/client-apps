package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.ChangePasswordRequestDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import com.fivucsas.shared.data.remote.dto.MfaChallengeResponse
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaSendOtpRequest
import com.fivucsas.shared.data.remote.dto.MfaStepRequest
import com.fivucsas.shared.data.remote.dto.MfaStepResponse
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodRequest
import com.fivucsas.shared.data.remote.dto.MfaSwitchMethodResponse
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
     * Request a WebAuthn challenge for FINGERPRINT or HARDWARE_KEY MFA step.
     * POST /auth/mfa/step with `data: { action: "challenge" }`. The server returns
     * a CHALLENGE-shaped response that does not match MfaStepResponse.
     */
    suspend fun requestMfaChallenge(request: MfaStepRequest): MfaChallengeResponse

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

    /**
     * Cancel an in-progress MFA session.
     * DELETE /auth/mfa/session/{sessionToken} (PUBLIC — rate limited via login bucket)
     *
     * Returns Unit on 204. Throws on any non-2xx (including 404 for an
     * already-expired session — caller should treat that as a soft success).
     */
    suspend fun cancelMfaSession(sessionToken: String)

    /**
     * Switch the active method for the current MFA step.
     * POST /auth/mfa/switch-method (PUBLIC — no JWT required)
     *
     * On 200 returns `status = "METHOD_SWITCHED"` plus the updated step
     * snapshot. On 409 the response carries an `errorCode` describing why
     * the switch was rejected (METHOD_NOT_PERMITTED / METHOD_ALREADY_USED /
     * NEEDS_ENROLLMENT). Other non-2xx responses are surfaced as exceptions.
     */
    suspend fun switchMfaMethod(request: MfaSwitchMethodRequest): MfaSwitchMethodResponse
}
