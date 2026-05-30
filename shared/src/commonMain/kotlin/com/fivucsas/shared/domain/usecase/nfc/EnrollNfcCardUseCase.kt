package com.fivucsas.shared.domain.usecase.nfc

import com.fivucsas.shared.domain.repository.NfcEnrollmentRepository
import com.fivucsas.shared.domain.repository.NfcEnrollmentResult

/**
 * Enroll a scanned NFC card's serial against the signed-in user.
 *
 * The serial is normalized to the API-canonical UPPERHEX (no separators)
 * form before it leaves the client so mobile-enrolled cards match
 * web-verified ones. A blank serial fails fast without a network call.
 */
open class EnrollNfcCardUseCase(
    private val repository: NfcEnrollmentRepository
) {
    open suspend operator fun invoke(
        cardSerial: String,
        cardType: String? = null,
        label: String? = null
    ): Result<NfcEnrollmentResult> {
        val normalized = normalizeCardSerial(cardSerial)
        if (normalized.isBlank()) {
            return Result.failure(IllegalArgumentException("Card serial is blank"))
        }
        return repository.enroll(normalized, cardType, label)
    }
}
