package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.TotpSetupResponseDto
import com.fivucsas.shared.data.remote.dto.TotpVerifyRequestDto
import com.fivucsas.shared.data.remote.dto.TotpVerifyResponseDto
import com.fivucsas.shared.data.remote.dto.TotpStatusResponseDto

/**
 * TOTP API interface
 *
 * Endpoints:
 * - POST /totp/setup/{userId}
 * - POST /totp/verify-setup/{userId}
 * - GET  /totp/status/{userId}
 */
interface TotpApi {
    suspend fun setup(userId: String): TotpSetupResponseDto
    suspend fun verifySetup(userId: String, request: TotpVerifyRequestDto): TotpVerifyResponseDto
    suspend fun getStatus(userId: String): TotpStatusResponseDto
}
