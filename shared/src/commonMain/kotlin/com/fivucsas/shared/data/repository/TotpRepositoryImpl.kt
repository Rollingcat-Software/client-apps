package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.TotpApi
import com.fivucsas.shared.data.remote.dto.TotpVerifyRequestDto
import com.fivucsas.shared.domain.repository.TotpRepository
import com.fivucsas.shared.domain.repository.TotpSetupResult
import com.fivucsas.shared.domain.repository.TotpStatusResult
import com.fivucsas.shared.domain.repository.TotpVerifyResult

class TotpRepositoryImpl(
    private val totpApi: TotpApi
) : TotpRepository {

    override suspend fun setup(userId: String): Result<TotpSetupResult> {
        return runCatching {
            val response = totpApi.setup(userId)
            TotpSetupResult(
                success = response.success,
                secret = response.secret,
                otpAuthUri = response.otpAuthUri,
                message = response.message
            )
        }
    }

    override suspend fun verifySetup(userId: String, code: String): Result<TotpVerifyResult> {
        return runCatching {
            val response = totpApi.verifySetup(userId, TotpVerifyRequestDto(code = code))
            TotpVerifyResult(success = response.success, message = response.message)
        }
    }

    override suspend fun getStatus(userId: String): Result<TotpStatusResult> {
        return runCatching {
            val response = totpApi.getStatus(userId)
            TotpStatusResult(enabled = response.configured, message = "")
        }
    }
}
