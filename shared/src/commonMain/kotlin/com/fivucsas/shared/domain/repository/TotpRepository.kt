package com.fivucsas.shared.domain.repository

interface TotpRepository {
    suspend fun setup(userId: String): Result<TotpSetupResult>
    suspend fun verifySetup(userId: String, code: String): Result<TotpVerifyResult>
    suspend fun getStatus(userId: String): Result<TotpStatusResult>
}

data class TotpSetupResult(
    val success: Boolean,
    val secret: String,
    val otpAuthUri: String,
    val message: String
)

data class TotpVerifyResult(
    val success: Boolean,
    val message: String
)

data class TotpStatusResult(
    val enabled: Boolean,
    val message: String
)
