package com.fivucsas.shared.attestation

/**
 * Canonical payload the attested key signs (identity-core-api
 * `AttestedBindingPayload.java`, `docs/ATTESTED_PUZZLE_BINDING.md` §4.3), version 1:
 *
 * ```
 * "FIVUCSAS-PUZZLE-BINDING-V1"   26 ASCII bytes
 * u32 len || puzzle_session_id   UTF-8
 * u32 len || nonce               raw
 * u32 n   || n x f64             embedding (big-endian IEEE-754 binary64)
 * trace_digest                   32 raw bytes
 * i64 t   || u32 n || n x i32    binding frame
 * ```
 *
 * The embedding doubles MUST be exactly the values serialized into the request
 * JSON (widen a FloatArray to Double ONCE and use that list for both).
 */
object AttestedBindingPayload {

    const val MAGIC = "FIVUCSAS-PUZZLE-BINDING-V1"
    const val VERSION = 1

    fun encode(
        puzzleSessionId: String,
        nonce: ByteArray,
        embedding: List<Double>,
        traceDigest: ByteArray,
        bindingFrameT: Long,
        bindingFrameCoords: List<Int>
    ): ByteArray {
        require(traceDigest.size == 32) { "trace digest must be 32 bytes" }
        val sid = puzzleSessionId.encodeToByteArray()
        val w = BigEndianWriter()
            .bytes(MAGIC.encodeToByteArray())
            .i32(sid.size).bytes(sid)
            .i32(nonce.size).bytes(nonce)
            .i32(embedding.size)
        embedding.forEach { w.f64(it) }
        w.bytes(traceDigest).i64(bindingFrameT).i32(bindingFrameCoords.size)
        bindingFrameCoords.forEach { w.i32(it) }
        return w.toByteArray()
    }
}
