package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.NfcAuthenticityApi
import com.fivucsas.shared.domain.repository.NfcAuthenticityRepository
import com.fivucsas.shared.domain.repository.NfcAuthenticityVerdict
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class NfcAuthenticityRepositoryImpl(
    private val api: NfcAuthenticityApi
) : NfcAuthenticityRepository {

    override suspend fun verify(
        sod: ByteArray,
        dg1: ByteArray?,
        dg2: ByteArray?
    ): Result<NfcAuthenticityVerdict> {
        if (sod.isEmpty()) {
            return Result.success(
                NfcAuthenticityVerdict(authentic = false, reasonCode = "MISSING_SOD", message = null)
            )
        }
        return try {
            val response = api.verifyAuthenticity(
                sodB64 = Base64.encode(sod),
                dg1B64 = dg1?.let { Base64.encode(it) },
                dg2B64 = dg2?.let { Base64.encode(it) }
            )
            Result.success(
                NfcAuthenticityVerdict(
                    authentic = response.authentic,
                    reasonCode = response.reasonCode ?: response.errorCode,
                    message = response.message
                )
            )
        } catch (e: Exception) {
            // Network failure on an authenticity check fails closed.
            Result.success(
                NfcAuthenticityVerdict(authentic = false, reasonCode = "NETWORK_ERROR", message = e.message)
            )
        }
    }
}
