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
 * `documentNumber` is the STABLE DG1 document number read during BAC
 * (e.g. `A28883159`) for eID / passport reads. A Turkish eID emits a
 * RANDOM NFC UID per tap, so keying card identity on the UID inserts a
 * NEW row every tap; when `documentNumber` is present the server keys
 * de-dup on `(userId, documentNumber)` and UPDATES/REACTIVATES the
 * existing row instead. Omitted for plain UID cards (MIFARE), which keep
 * today's UID/serial-based de-dup — fully backward-compatible.
 *
 * `userId` is optional — the server derives the owner from the bearer
 * token when omitted.
 */
@Serializable
data class NfcEnrollRequest(
    val cardSerial: String,
    val userId: String? = null,
    val cardType: String? = null,
    val label: String? = null,
    val documentNumber: String? = null
)

/**
 * Response from `POST /api/v1/nfc/enroll`.
 *
 * Spring/Jackson camelCase; all fields defaulted so a sparse server
 * response never fails deserialization.
 *
 * `alreadyRegistered` is `true` when the card/document already existed and
 * was reactivated/updated (e.g. re-tapping the same eID under its stable
 * document number), `false` when a brand-new row was created. Older
 * servers omit it → defaults to `false` (newly-created semantics).
 */
@Serializable
data class NfcEnrollResponse(
    val success: Boolean = true,
    val enrollmentId: String? = null,
    val cardSerial: String = "",
    val message: String? = null,
    val alreadyRegistered: Boolean = false
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
