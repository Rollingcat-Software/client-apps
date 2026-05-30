package com.fivucsas.shared.domain.usecase.nfc

import com.fivucsas.shared.domain.repository.NfcAuthenticityRepository
import com.fivucsas.shared.domain.repository.NfcAuthenticityVerdict
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VerifyNfcAuthenticityUseCaseTest {

    private class FakeRepo(
        private val verdict: NfcAuthenticityVerdict = NfcAuthenticityVerdict(true, "OK", null)
    ) : NfcAuthenticityRepository {
        var calledWithSodSize: Int? = null
        override suspend fun verify(sod: ByteArray, dg1: ByteArray?, dg2: ByteArray?): Result<NfcAuthenticityVerdict> {
            calledWithSodSize = sod.size
            return Result.success(verdict)
        }
    }

    @Test
    fun `null SOD short-circuits to not-authentic without calling the repository`() = runTest {
        val repo = FakeRepo()
        val useCase = VerifyNfcAuthenticityUseCase(repo)

        val result = useCase(sod = null, dg1 = null, dg2 = null)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow().authentic)
        assertEquals("MISSING_SOD", result.getOrThrow().reasonCode)
        assertNull(repo.calledWithSodSize)
    }

    @Test
    fun `empty SOD short-circuits to not-authentic`() = runTest {
        val repo = FakeRepo()
        val useCase = VerifyNfcAuthenticityUseCase(repo)

        val result = useCase(sod = ByteArray(0), dg1 = null, dg2 = null)

        assertFalse(result.getOrThrow().authentic)
        assertNull(repo.calledWithSodSize)
    }

    @Test
    fun `present SOD is forwarded to the repository`() = runTest {
        val repo = FakeRepo()
        val useCase = VerifyNfcAuthenticityUseCase(repo)

        val result = useCase(sod = ByteArray(64) { 1 }, dg1 = ByteArray(10), dg2 = null)

        assertTrue(result.getOrThrow().authentic)
        assertEquals(64, repo.calledWithSodSize)
    }
}
