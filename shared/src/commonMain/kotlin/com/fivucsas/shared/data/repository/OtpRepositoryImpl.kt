package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.OtpApi
import com.fivucsas.shared.data.remote.dto.OtpSendRequestDto
import com.fivucsas.shared.data.remote.dto.OtpVerifyRequestDto
import com.fivucsas.shared.domain.repository.OtpRepository
import com.fivucsas.shared.domain.repository.OtpResult

class OtpRepositoryImpl(
    private val otpApi: OtpApi
) : OtpRepository {

    override suspend fun sendEmailOtp(userId: String, email: String): Result<OtpResult> {
        return runCatching {
            val response = otpApi.sendEmailOtp(userId, OtpSendRequestDto(email = email))
            OtpResult(success = response.success, message = response.message)
        }
    }

    override suspend fun verifyEmailOtp(userId: String, code: String): Result<OtpResult> {
        return runCatching {
            val response = otpApi.verifyEmailOtp(userId, OtpVerifyRequestDto(code = code))
            OtpResult(success = response.success, message = response.message)
        }
    }

    override suspend fun sendSmsOtp(userId: String, phoneNumber: String): Result<OtpResult> {
        return runCatching {
            val response = otpApi.sendSmsOtp(userId, OtpSendRequestDto(phoneNumber = phoneNumber))
            OtpResult(success = response.success, message = response.message)
        }
    }

    override suspend fun verifySmsOtp(userId: String, code: String): Result<OtpResult> {
        return runCatching {
            val response = otpApi.verifySmsOtp(userId, OtpVerifyRequestDto(code = code))
            OtpResult(success = response.success, message = response.message)
        }
    }
}
