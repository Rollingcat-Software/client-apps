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
        var shouldSucceed: Boolean = true

        override suspend fun enroll(
            cardSerial: String,
            cardType: String?,
            label: String?
        ): Result<NfcEnrollmentResult> {
            lastSerial = cardSerial
            lastCardType = cardType
            lastLabel = label
            return if (shouldSucceed) {
                Result.success(NfcEnrollmentResult(enrollmentId = "enr-1", cardSerial = cardSerial))
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
}
