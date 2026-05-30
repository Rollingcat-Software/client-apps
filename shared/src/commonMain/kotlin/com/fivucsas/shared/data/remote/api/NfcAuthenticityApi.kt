package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.NfcVerifyAuthenticityResponse

/**
 * Passive-authentication API: submits raw EF.SOD + DGs (base64) for the
 * authoritative, fail-closed server verdict.
 *
 * POST /nfc/verify-authenticity (Bearer auth). The client-side DS→CSCA check
 * is advisory; this server verdict is authoritative.
 */
interface NfcAuthenticityApi {
    /**
     * @param sodB64 base64 of EF.SOD (required)
     * @param dg1B64 base64 of DG1 (optional)
     * @param dg2B64 base64 of DG2 (optional)
     * @return the parsed verdict body for BOTH the 200 (authentic) and the
     *         422 (not authentic) responses; 400 (missing SOD) surfaces as a
     *         response with errorCode `NFC_PA_MISSING_SOD`.
     */
    suspend fun verifyAuthenticity(
        sodB64: String,
        dg1B64: String?,
        dg2B64: String?
    ): NfcVerifyAuthenticityResponse
}
