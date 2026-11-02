package com.fivucsas.shared.attestation

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Golden vectors shared with biometric-processor (`test_puzzle_trace_digest.py`) and
 * identity-core-api (`AttestedBindingPayloadTest`). If one changes, all three change.
 */
@OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
class AttestationEncodingTest {

    @Test
    fun sha256KnownAnswers() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Sha256.digest(ByteArray(0)).toHex()
        )
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256.digest("abc".encodeToByteArray()).toHex()
        )
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            Sha256.digest("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray()).toHex()
        )
        assertEquals(
            "cdc76e5c9914fb9281a1c7e284d73e67f1809a48a497200e046d39ccc7112cd0",
            Sha256.digest(ByteArray(1_000_000) { 'a'.code.toByte() }).toHex()
        )
    }

    private val goldenTrace = PuzzleTrace(
        frameWidth = 640,
        frameHeight = 480,
        frames = listOf(
            PuzzleTraceFrame(t = 0, f = List(96) { it }),
            PuzzleTraceFrame(t = 40, h = List(63) { -it }),
            PuzzleTraceFrame(t = 1L shl 53)
        )
    )

    @Test
    fun traceDigestGoldenVector() {
        assertEquals(
            "6c30c5f05962a473576a2d514dea245bbc5954addaadf8d79231bfe4fdccb8b8",
            PuzzleTraceDigest.challengeDigest("blink", goldenTrace).toHex()
        )
        assertEquals(
            "9d27d609842afc02754e30221d3b0441b6b17812ba71095bc67ce753b1f09a25",
            PuzzleTraceDigest.challengeDigest("wave", goldenTrace).toHex()
        )
        assertEquals(
            GOLDEN_TRACE_DIGEST,
            PuzzleTraceDigest.sessionDigestHex(
                listOf(AcceptedPuzzleChallenge("blink", goldenTrace), AcceptedPuzzleChallenge("wave", goldenTrace))
            )
        )
    }

    private fun goldenPayload(): ByteArray = AttestedBindingPayload.encode(
        puzzleSessionId = "puzzle-session-1",
        nonce = ByteArray(32) { it.toByte() },
        embedding = listOf(0.1, -0.25, 1.0 / 3, 0.0, -0.0, 1e-7),
        traceDigest = GOLDEN_TRACE_DIGEST.hexToBytes(),
        bindingFrameT = 1234567L,
        bindingFrameCoords = List(96) { it * 3 - 100 }
    )

    @Test
    fun payloadGoldenVector() {
        val p = goldenPayload()
        assertEquals(562, p.size)
        assertEquals("26b536124412eac9347ea789a73bda97a3406b7e5cddb55c2fbe5cfdf538e9f7", Sha256.digest(p).toHex())
    }

    @Test
    fun payloadRejectsShortDigest() {
        assertFailsWith<IllegalArgumentException> {
            AttestedBindingPayload.encode("s", ByteArray(32), listOf(1.0), ByteArray(31), 0, emptyList())
        }
    }

    private class RecordingProvider : AttestedKeyProvider {
        var challenge: ByteArray? = null
        var payload: ByteArray? = null
        override fun isSupported() = true
        override suspend fun signWithAttestedKey(challenge: ByteArray, payload: ByteArray): AttestedSignature {
            this.challenge = challenge
            this.payload = payload
            return AttestedSignature(listOf(byteArrayOf(1, 2), byteArrayOf(3)), byteArrayOf(9, 9), strongBox = false)
        }
    }

    @Test
    fun binderSignsTheCanonicalPayloadAndSendsTheSameValues() = runTest {
        val provider = RecordingProvider()
        val nonce = ByteArray(32) { (it * 7).toByte() }
        val challenge = kotlin.io.encoding.Base64.UrlSafe.encode(nonce).trimEnd('=')
        val embedding = List(512) { kotlin.math.sin(it.toDouble()) / 23.0 }
        val accepted = listOf(AcceptedPuzzleChallenge("blink", goldenTrace))
        val frame = BindingFrame(4321, List(96) { it - 50 })

        val data = AttestedPuzzleBinder(provider).buildStepData("ps-1", challenge, embedding, accepted, frame)

        assertContentEquals(nonce, provider.challenge)
        val digest = PuzzleTraceDigest.sessionDigestHex(accepted)
        assertContentEquals(
            AttestedBindingPayload.encode("ps-1", nonce, embedding, digest.hexToBytes(), 4321, frame.f),
            provider.payload
        )
        assertEquals("ps-1", data["puzzle_session_id"]!!.jsonPrimitive.content)
        val sent = data["embedding"]!!.jsonArray.map { it.jsonPrimitive.content.toDouble() }
        assertEquals(embedding, sent)
        assertEquals(4321L, data["binding_frame"]!!.jsonObject["t"]!!.jsonPrimitive.long)
        val att = data["attestation"]!!.jsonObject
        assertEquals("android-key", att["format"]!!.jsonPrimitive.content)
        assertEquals(1, att["payload_version"]!!.jsonPrimitive.int)
        assertEquals(2, att["certificate_chain"]!!.jsonArray.size)
        assertEquals(digest, att["trace_digest"]!!.jsonPrimitive.content)
    }

    @Test
    fun binderValidatesInputs() = runTest {
        val binder = AttestedPuzzleBinder(RecordingProvider())
        val frame = BindingFrame(1, List(96) { 0 })
        assertFailsWith<IllegalArgumentException> {
            binder.buildStepData("ps", "AAAA", List(10) { 0.0 }, listOf(AcceptedPuzzleChallenge("blink", goldenTrace)), frame)
        }
        assertFailsWith<IllegalArgumentException> {
            binder.buildStepData("ps", "AAAA", List(512) { 0.0 }, emptyList(), frame)
        }
        assertFailsWith<IllegalArgumentException> { BindingFrame(1, List(63) { 0 }) }
    }

    @Test
    fun challengeDecodingAcceptsUrlSafeUnpadded() {
        val bytes = ByteArray(32) { (255 - it).toByte() }
        val url = kotlin.io.encoding.Base64.UrlSafe.encode(bytes).trimEnd('=')
        assertContentEquals(bytes, AttestedPuzzleBinder.decodeChallenge(url))
        assertFalse(url.contains('='))
    }

    companion object {
        const val GOLDEN_TRACE_DIGEST = "8843a760a10fd102f3cf6206ec69437632f5895072999f5cdba5c3230193364c"
    }
}
