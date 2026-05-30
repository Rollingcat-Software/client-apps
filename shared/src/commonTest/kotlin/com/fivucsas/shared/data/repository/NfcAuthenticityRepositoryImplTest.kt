package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.NfcAuthenticityApi
import com.fivucsas.shared.data.remote.dto.NfcVerifyAuthenticityResponse
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class NfcAuthenticityRepositoryImplTest {

    private class CapturingApi(
        private val response: NfcVerifyAuthenticityResponse
    ) : NfcAuthenticityApi {
        var lastSod: String? = null
        var lastDg1: String? = null
        var lastDg2: String? = null
        override suspend fun verifyAuthenticity(sodB64: String, dg1B64: String?, dg2B64: String?): NfcVerifyAuthenticityResponse {
            lastSod = sodB64; lastDg1 = dg1B64; lastDg2 = dg2B64
            return response
        }
    }

    @Test
    fun `raw bytes are base64-encoded before sending`() = runTest {
        val api = CapturingApi(NfcVerifyAuthenticityResponse(success = true, authentic = true, reasonCode = "OK"))
        val repo = NfcAuthenticityRepositoryImpl(api)
        val sod = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val dg1 = byteArrayOf(0x10, 0x20)

        val result = repo.verify(sod = sod, dg1 = dg1, dg2 = null)

        assertTrue(result.getOrThrow().authentic)
        assertEquals(Base64.encode(sod), api.lastSod)
        assertEquals(Base64.encode(dg1), api.lastDg1)
        assertEquals(null, api.lastDg2)
    }

    @Test
    fun `not-authentic response maps reasonCode through`() = runTest {
        val api = CapturingApi(
            NfcVerifyAuthenticityResponse(success = false, authentic = false, reasonCode = "NO_TRUST_STORE")
        )
        val repo = NfcAuthenticityRepositoryImpl(api)

        val verdict = repo.verify(sod = byteArrayOf(1, 2), dg1 = null, dg2 = null).getOrThrow()

        assertFalse(verdict.authentic)
        assertEquals("NO_TRUST_STORE", verdict.reasonCode)
    }

    @Test
    fun `empty SOD fails closed without calling the api`() = runTest {
        val api = CapturingApi(NfcVerifyAuthenticityResponse(authentic = true))
        val repo = NfcAuthenticityRepositoryImpl(api)

        val verdict = repo.verify(sod = ByteArray(0), dg1 = null, dg2 = null).getOrThrow()

        assertFalse(verdict.authentic)
        assertEquals("MISSING_SOD", verdict.reasonCode)
        assertEquals(null, api.lastSod)
    }
}
