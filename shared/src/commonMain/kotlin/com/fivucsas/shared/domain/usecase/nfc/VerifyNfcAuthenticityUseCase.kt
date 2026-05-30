package com.fivucsas.shared.domain.usecase.nfc

import com.fivucsas.shared.domain.repository.NfcAuthenticityRepository
import com.fivucsas.shared.domain.repository.NfcAuthenticityVerdict

/**
 * Run passive authentication on a freshly-read identity document: submit the
 * raw EF.SOD (+ DG1/DG2) to the server for the authoritative, fail-closed
 * verdict. A missing SOD short-circuits to a not-authentic verdict without a
 * network call.
 */
open class VerifyNfcAuthenticityUseCase(
    private val repository: NfcAuthenticityRepository
) {
    open suspend operator fun invoke(
        sod: ByteArray?,
        dg1: ByteArray?,
        dg2: ByteArray?
    ): Result<NfcAuthenticityVerdict> {
        if (sod == null || sod.isEmpty()) {
            return Result.success(
                NfcAuthenticityVerdict(authentic = false, reasonCode = "MISSING_SOD", message = null)
            )
        }
        return repository.verify(sod, dg1, dg2)
    }
}
