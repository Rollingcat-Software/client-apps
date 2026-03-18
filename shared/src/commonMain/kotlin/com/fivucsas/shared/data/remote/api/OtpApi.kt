package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.OtpSendRequestDto
import com.fivucsas.shared.data.remote.dto.OtpSendResponseDto
import com.fivucsas.shared.data.remote.dto.OtpVerifyRequestDto
import com.fivucsas.shared.data.remote.dto.OtpVerifyResponseDto

/**
 * OTP API interface (Email and SMS)
 *
 * Endpoints:
 * - POST /otp/email/send/{userId}
 * - POST /otp/email/verify/{userId}
 * - POST /otp/sms/send/{userId}
 * - POST /otp/sms/verify/{userId}
 */
interface OtpApi {
    suspend fun sendEmailOtp(userId: String, request: OtpSendRequestDto): OtpSendResponseDto
    suspend fun verifyEmailOtp(userId: String, request: OtpVerifyRequestDto): OtpVerifyResponseDto
    suspend fun sendSmsOtp(userId: String, request: OtpSendRequestDto): OtpSendResponseDto
    suspend fun verifySmsOtp(userId: String, request: OtpVerifyRequestDto): OtpVerifyResponseDto
}
