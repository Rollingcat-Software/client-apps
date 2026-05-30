package com.fivucsas.shared.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request body for `POST /api/v1/nfc/enroll`.
 *
 * `cardSerial` MUST be the API-canonical form: upper-case hex, NO
 * separators (e.g. `04A2245B6F7180`). Android's `Tag.getId()` already
 * yields this via `ByteArray.toHexString()`; we normalize defensively for
 * opaque/generic UIDs (see [com.fivucsas.shared.domain.usecase.nfc.normalizeCardSerial]).
 * The API also normalizes on ingest, so a mobile-enrolled card matches a
 * web-verified one and vice-versa.
 *
 * `userId` is optional — the server derives the owner from the bearer
 * token when omitted.
 */
@Serializable
data class NfcEnrollRequest(
    val cardSerial: String,
    val userId: String? = null,
    val cardType: String? = null,
    val label: String? = null
)

/**
 * Response from `POST /api/v1/nfc/enroll`.
 *
 * Spring/Jackson camelCase; all fields defaulted so a sparse server
 * response never fails deserialization.
 */
@Serializable
data class NfcEnrollResponse(
    val success: Boolean = true,
    val enrollmentId: String? = null,
    val cardSerial: String = "",
    val message: String? = null
)

/**
 * Request body for `POST /api/v1/nfc/verify`.
 */
@Serializable
data class NfcVerifyRequest(
    val cardSerial: String
)

/**
 * Response from `POST /api/v1/nfc/verify`.
 */
@Serializable
data class NfcVerifyResponse(
    val success: Boolean = false,
    val matched: Boolean = false,
    val userId: String? = null,
    val message: String? = null
)
