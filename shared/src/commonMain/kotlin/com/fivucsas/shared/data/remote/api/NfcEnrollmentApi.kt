package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.NfcEnrollRequest
import com.fivucsas.shared.data.remote.dto.NfcEnrollResponse
import com.fivucsas.shared.data.remote.dto.NfcVerifyRequest
import com.fivucsas.shared.data.remote.dto.NfcVerifyResponse

/**
 * NFC document enrollment / verification API.
 *
 * Endpoints (identity-core-api, Bearer auth):
 * - POST /nfc/enroll  → enroll a card serial against the current user
 * - POST /nfc/verify  → look a card serial up (advisory match)
 */
interface NfcEnrollmentApi {
    suspend fun enroll(request: NfcEnrollRequest): NfcEnrollResponse
    suspend fun verify(request: NfcVerifyRequest): NfcVerifyResponse
}
