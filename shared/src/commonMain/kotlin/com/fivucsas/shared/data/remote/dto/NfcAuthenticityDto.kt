package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request body for `POST /api/v1/nfc/verify-authenticity` (passive
 * authentication). All fields are base64-encoded raw bytes read from the
 * chip. `sod` is `EF.SOD`; `dg1` / `dg2` are the data groups. The server is
 * authoritative and fail-closed — it re-runs the SOD signature + DG-hash +
 * DS→CSCA chain against its trust store.
 *
 * Per the identity-core-api contract (#159), either `sod` or `sod_b64` and
 * either `dg1`/`dg2` or numeric `"1"`/`"2"` keys are accepted; we send the
 * `sod`/`dg1`/`dg2` form.
 */
@Serializable
data class NfcVerifyAuthenticityRequest(
    val sod: String,
    val dg1: String? = null,
    val dg2: String? = null
)

/**
 * Response from `POST /api/v1/nfc/verify-authenticity`.
 *
 * - 200 → `{ success=true, authentic=true, reasonCode="OK" }`
 * - 422 → `{ success=false, authentic=false, errorCode="NFC_PA_NOT_AUTHENTIC",
 *            reasonCode=<DG_HASH_MISMATCH|SIGNATURE_INVALID|DS_UNTRUSTED|
 *                        SOD_PARSE_ERROR|NO_TRUST_STORE|MISSING_DG|...> }`
 * - 400 → missing SOD (`NFC_PA_MISSING_SOD`)
 *
 * All fields defaulted so the 422/400 error bodies deserialize too.
 */
@Serializable
data class NfcVerifyAuthenticityResponse(
    val success: Boolean = false,
    val authentic: Boolean = false,
    val reasonCode: String? = null,
    val errorCode: String? = null,
    val message: String? = null
)
