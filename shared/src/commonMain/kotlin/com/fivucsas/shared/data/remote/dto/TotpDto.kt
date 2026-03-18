package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TotpSetupResponseDto(
    val success: Boolean = true,
    val secret: String = "",
    val otpAuthUri: String = "",
    val message: String = ""
)

@Serializable
data class TotpVerifyRequestDto(
    val code: String
)

@Serializable
data class TotpVerifyResponseDto(
    val success: Boolean = false,
    val message: String = ""
)

/**
 * TOTP status response DTO — matches server response:
 * GET /totp/status/{userId}
 *
 * Server returns: { "userId": "...", "configured": true/false }
 */
@Serializable
data class TotpStatusResponseDto(
    val userId: String = "",
    val configured: Boolean = false
)
