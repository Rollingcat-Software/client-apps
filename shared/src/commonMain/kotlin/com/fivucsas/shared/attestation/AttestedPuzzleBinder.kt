package com.fivucsas.shared.attestation

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** The binding frame: trace timestamp + 96 face landmark ints of the embedding's source frame. */
data class BindingFrame(val t: Long, val f: List<Int>) {
    init {
        require(f.size == 96) { "binding frame must have 96 ints (32 landmarks x 3)" }
    }
}

/**
 * Builds the attested PUZZLE `/auth/mfa/step` `data` object (identity-core-api
 * `docs/ATTESTED_PUZZLE_BINDING.md` §4.2): computes the trace digest of the
 * accepted traces, encodes the canonical payload, signs it with a fresh
 * hardware-attested key bound to the server challenge, and assembles
 * `{puzzle_session_id, embedding, binding_frame, attestation{...}}`.
 *
 * The embedding passed here must be the exact `List<Double>` that is sent: this
 * class serializes the same list it signs.
 */
class AttestedPuzzleBinder(private val keyProvider: AttestedKeyProvider) {

    fun isSupported(): Boolean = keyProvider.isSupported()

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun buildStepData(
        puzzleSessionId: String,
        attestationChallenge: String,
        embedding: List<Double>,
        acceptedInIssuedOrder: List<AcceptedPuzzleChallenge>,
        bindingFrame: BindingFrame
    ): JsonObject {
        require(embedding.size == EMBEDDING_DIM) { "embedding must have $EMBEDDING_DIM values" }
        require(acceptedInIssuedOrder.isNotEmpty()) { "no accepted puzzle challenges" }
        val nonce = decodeChallenge(attestationChallenge)
        val traceDigestHex = PuzzleTraceDigest.sessionDigestHex(acceptedInIssuedOrder)
        val payload = AttestedBindingPayload.encode(
            puzzleSessionId, nonce, embedding, traceDigestHex.hexToBytes(), bindingFrame.t, bindingFrame.f
        )
        val signed = keyProvider.signWithAttestedKey(nonce, payload)
        return buildJsonObject {
            put("puzzle_session_id", puzzleSessionId)
            put("embedding", buildJsonArray { embedding.forEach { add(JsonPrimitive(it)) } })
            put("binding_frame", buildJsonObject {
                put("t", bindingFrame.t)
                put("f", buildJsonArray { bindingFrame.f.forEach { add(JsonPrimitive(it)) } })
            })
            put("attestation", buildJsonObject {
                put("format", FORMAT_ANDROID_KEY)
                put("payload_version", AttestedBindingPayload.VERSION)
                put("certificate_chain", buildJsonArray {
                    signed.certificateChainDer.forEach { add(JsonPrimitive(Base64.encode(it))) }
                })
                put("signature", Base64.encode(signed.signatureDer))
                put("trace_digest", traceDigestHex)
            })
        }
    }

    companion object {
        const val EMBEDDING_DIM = 512
        const val FORMAT_ANDROID_KEY = "android-key"

        /** The server sends base64url without padding; accept standard base64 too. */
        @OptIn(ExperimentalEncodingApi::class)
        fun decodeChallenge(value: String): ByteArray {
            val normalized = value.trim().replace('-', '+').replace('_', '/')
            val padded = normalized + "=".repeat((4 - normalized.length % 4) % 4)
            return Base64.decode(padded)
        }
    }
}
