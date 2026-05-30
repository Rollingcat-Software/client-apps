package com.fivucsas.shared.domain.repository

/**
 * Authoritative server verdict for NFC passive authentication.
 *
 * @param authentic true only on the server's 200 OK; false (fail-closed) for
 *        any 422 / 400 / error.
 * @param reasonCode server reason (OK, DG_HASH_MISMATCH, SIGNATURE_INVALID,
 *        DS_UNTRUSTED, SOD_PARSE_ERROR, NO_TRUST_STORE, MISSING_DG,
 *        MISSING_SOD, …) — surfaced to the UI/logs.
 */
data class NfcAuthenticityVerdict(
    val authentic: Boolean,
    val reasonCode: String?,
    val message: String?
)

/**
 * Repository for NFC passive authentication. Sends the raw EF.SOD + DG bytes
 * to the server for the authoritative, fail-closed verdict. The client-side
 * DS→CSCA check (CscaCertificateStore) is advisory only.
 */
interface NfcAuthenticityRepository {
    suspend fun verify(
        sod: ByteArray,
        dg1: ByteArray?,
        dg2: ByteArray?
    ): Result<NfcAuthenticityVerdict>
}
