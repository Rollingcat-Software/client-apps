package com.fivucsas.shared.domain.usecase.nfc

import com.fivucsas.shared.domain.repository.NfcEnrollmentRepository
import com.fivucsas.shared.domain.repository.NfcEnrollmentResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnrollNfcCardUseCaseTest {

    private class FakeNfcEnrollmentRepository : NfcEnrollmentRepository {
        var lastSerial: String? = null
        var lastCardType: String? = null
        var lastLabel: String? = null
        var lastDocumentNumber: String? = null
        var documentNumberWasPassed: Boolean = false
        var shouldSucceed: Boolean = true
        var alreadyRegistered: Boolean = false

        override suspend fun enroll(
            cardSerial: String,
            cardType: String?,
            label: String?,
            documentNumber: String?
        ): Result<NfcEnrollmentResult> {
            lastSerial = cardSerial
            lastCardType = cardType
            lastLabel = label
            lastDocumentNumber = documentNumber
            documentNumberWasPassed = true
            return if (shouldSucceed) {
                Result.success(
                    NfcEnrollmentResult(
                        enrollmentId = "enr-1",
                        cardSerial = cardSerial,
                        alreadyRegistered = alreadyRegistered
                    )
                )
            } else {
                Result.failure(RuntimeException("server error"))
            }
        }

        override suspend fun verify(cardSerial: String): Result<Boolean> =
            Result.success(true)
    }

    @Test
    fun `enroll normalizes the serial to canonical UPPERHEX before sending`() = runTest {
        val repo = FakeNfcEnrollmentRepository()
        val useCase = EnrollNfcCardUseCase(repo)

        val result = useCase(cardSerial = "04:a2:24:5b", cardType = "Turkish eID")

        assertTrue(result.isSuccess)
        assertEquals("04A2245B", repo.lastSerial)
        assertEquals("Turkish eID", repo.lastCardType)
    }

    @Test
    fun `blank serial fails fast without hitting the repository`() = runTest {
        val repo = FakeNfcEnrollmentRepository()
        val useCase = EnrollNfcCardUseCase(repo)

        val result = useCase(cardSerial = "   ")

        assertFalse(result.isSuccess)
        assertNull(repo.lastSerial)
    }

    @Test
    fun `repository failure propagates`() = runTest {
        val repo = FakeNfcEnrollmentRepository().apply { shouldSucceed = false }
        val useCase = EnrollNfcCardUseCase(repo)

        val result = useCase(cardSerial = "DEADBEEF")

        assertFalse(result.isSuccess)
    }

    @Test
    fun `eID document number is passed through verbatim (not hex-normalized)`() = runTest {
        // A28883159 contains letters → it must NOT be stripped/normalized like a
        // hex UID; it is the stable de-dup key the server keys (userId, docNumber) on.
        val repo = FakeNfcEnrollmentRepository()
        val useCase = EnrollNfcCardUseCase(repo)

        val result = useCase(
            cardSerial = "08570ECC",
            cardType = "Turkish eID",
            documentNumber = "A28883159"
        )

        assertTrue(result.isSuccess)
        assertEquals("08570ECC", repo.lastSerial)
        assertEquals("A28883159", repo.lastDocumentNumber)
    }

    @Test
    fun `blank document number is sent as null (plain UID card de-dup preserved)`() = runTest {
        val repo = FakeNfcEnrollmentRepository()
        val useCase = EnrollNfcCardUseCase(repo)

        useCase(cardSerial = "04A2245B", documentNumber = "   ")

        assertTrue(repo.documentNumberWasPassed)
        assertNull(repo.lastDocumentNumber)
    }

    @Test
    fun `omitted document number defaults to null`() = runTest {
        val repo = FakeNfcEnrollmentRepository()
        val useCase = EnrollNfcCardUseCase(repo)

        useCase(cardSerial = "04A2245B")

        assertNull(repo.lastDocumentNumber)
    }

    @Test
    fun `alreadyRegistered from the server is surfaced in the result`() = runTest {
        val repo = FakeNfcEnrollmentRepository().apply { alreadyRegistered = true }
        val useCase = EnrollNfcCardUseCase(repo)

        val result = useCase(cardSerial = "08570ECC", documentNumber = "A28883159")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().alreadyRegistered)
    }

    @Test
    fun `newly created card reports alreadyRegistered false`() = runTest {
        val repo = FakeNfcEnrollmentRepository().apply { alreadyRegistered = false }
        val useCase = EnrollNfcCardUseCase(repo)

        val result = useCase(cardSerial = "DEADBEEF")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow().alreadyRegistered)
    }
}
