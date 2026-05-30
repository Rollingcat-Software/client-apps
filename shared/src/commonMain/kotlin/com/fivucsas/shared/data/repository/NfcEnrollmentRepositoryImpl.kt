package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.NfcEnrollmentApi
import com.fivucsas.shared.data.remote.dto.NfcEnrollRequest
import com.fivucsas.shared.data.remote.dto.NfcVerifyRequest
import com.fivucsas.shared.domain.repository.NfcEnrollmentRepository
import com.fivucsas.shared.domain.repository.NfcEnrollmentResult

class NfcEnrollmentRepositoryImpl(
    private val api: NfcEnrollmentApi
) : NfcEnrollmentRepository {

    override suspend fun enroll(
        cardSerial: String,
        cardType: String?,
        label: String?
    ): Result<NfcEnrollmentResult> {
        return try {
            val response = api.enroll(
                NfcEnrollRequest(
                    cardSerial = cardSerial,
                    cardType = cardType,
                    label = label
                )
            )
            Result.success(
                NfcEnrollmentResult(
                    enrollmentId = response.enrollmentId,
                    cardSerial = response.cardSerial.ifBlank { cardSerial }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verify(cardSerial: String): Result<Boolean> {
        return try {
            val response = api.verify(NfcVerifyRequest(cardSerial = cardSerial))
            Result.success(response.matched)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
