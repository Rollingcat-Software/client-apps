package com.fivucsas.shared.domain.repository

/**
 * Result of enrolling an NFC card serial against the current user.
 */
data class NfcEnrollmentResult(
    val enrollmentId: String?,
    val cardSerial: String
)

/**
 * Repository for NFC document enrollment / verification against the
 * identity-core-api. The serial is normalized to the API-canonical form
 * (upper-case hex, no separators) before it leaves the client.
 */
interface NfcEnrollmentRepository {
    suspend fun enroll(
        cardSerial: String,
        cardType: String? = null,
        label: String? = null
    ): Result<NfcEnrollmentResult>

    suspend fun verify(cardSerial: String): Result<Boolean>
}
