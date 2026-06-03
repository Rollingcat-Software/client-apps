package com.fivucsas.shared.domain.repository

/**
 * Result of enrolling an NFC card serial against the current user.
 *
 * `alreadyRegistered` is `true` when the card/document already existed and
 * was reactivated/updated server-side (re-reading the same eID under its
 * stable document number), `false` when a new row was created.
 */
data class NfcEnrollmentResult(
    val enrollmentId: String?,
    val cardSerial: String,
    val alreadyRegistered: Boolean = false
)

/**
 * Repository for NFC document enrollment / verification against the
 * identity-core-api. The serial is normalized to the API-canonical form
 * (upper-case hex, no separators) before it leaves the client.
 *
 * `documentNumber` (when non-null) is the stable DG1 document number from a
 * BAC read; the server de-dups on `(userId, documentNumber)` so re-reading
 * the same eID (different random UID per tap) reactivates the existing row.
 */
interface NfcEnrollmentRepository {
    suspend fun enroll(
        cardSerial: String,
        cardType: String? = null,
        label: String? = null,
        documentNumber: String? = null
    ): Result<NfcEnrollmentResult>

    suspend fun verify(cardSerial: String): Result<Boolean>
}
