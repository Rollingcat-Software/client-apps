package com.fivucsas.shared.domain.repository

interface OtpRepository {
    suspend fun sendEmailOtp(userId: String, email: String): Result<OtpResult>
    suspend fun verifyEmailOtp(userId: String, code: String): Result<OtpResult>
    suspend fun sendSmsOtp(userId: String, phoneNumber: String): Result<OtpResult>
    suspend fun verifySmsOtp(userId: String, code: String): Result<OtpResult>
}

data class OtpResult(
    val success: Boolean,
    val message: String
)
