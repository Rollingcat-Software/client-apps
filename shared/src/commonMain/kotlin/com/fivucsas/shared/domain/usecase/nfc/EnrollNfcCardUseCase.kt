package com.fivucsas.shared.domain.usecase.nfc

import com.fivucsas.shared.domain.repository.NfcEnrollmentRepository
import com.fivucsas.shared.domain.repository.NfcEnrollmentResult

/**
 * Enroll a scanned NFC card's serial against the signed-in user.
 *
 * The serial is normalized to the API-canonical UPPERHEX (no separators)
 * form before it leaves the client so mobile-enrolled cards match
 * web-verified ones. A blank serial fails fast without a network call.
 *
 * `documentNumber` is the stable DG1 document number from a BAC read (eID /
 * passport). It is passed through VERBATIM (only trimmed; never hex-
 * normalized — it is an alphanumeric document number like `A28883159`, not
 * a hex UID) so the server can de-dup on `(userId, documentNumber)` and
 * reactivate the existing row when the same eID is re-tapped under a
 * different random UID. Omitted (null/blank) for plain UID cards (MIFARE),
 * preserving today's UID-based de-dup.
 */
open class EnrollNfcCardUseCase(
    private val repository: NfcEnrollmentRepository
) {
    open suspend operator fun invoke(
        cardSerial: String,
        cardType: String? = null,
        label: String? = null,
        documentNumber: String? = null
    ): Result<NfcEnrollmentResult> {
        val normalized = normalizeCardSerial(cardSerial)
        if (normalized.isBlank()) {
            return Result.failure(IllegalArgumentException("Card serial is blank"))
        }
        val stableDocNumber = documentNumber?.trim()?.takeIf { it.isNotBlank() }
        return repository.enroll(normalized, cardType, label, stableDocNumber)
    }
}
