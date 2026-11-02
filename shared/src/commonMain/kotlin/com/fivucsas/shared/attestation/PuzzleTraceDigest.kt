package com.fivucsas.shared.attestation

/**
 * One frame of a puzzle contract-v2 landmark trace (wire form): `t` client clock
 * in ms, `f` = 32 face landmarks x (x, y, z) ints (1e-4 units) in the server's
 * `trace_face_indices` order, `h` = 21 hand landmarks x 3 ints, either may be null.
 */
data class PuzzleTraceFrame(val t: Long, val f: List<Int>? = null, val h: List<Int>? = null)

/** A v2 trace exactly as SUBMITted to `/auth/mfa/puzzle/session/{id}/challenge`. */
data class PuzzleTrace(
    val frameWidth: Int,
    val frameHeight: Int,
    val frames: List<PuzzleTraceFrame>,
    val version: Int = 2
)

/** A challenge the server ACCEPTED (`verified:true`), with the trace that was scored. */
data class AcceptedPuzzleChallenge(val action: String, val trace: PuzzleTrace)

/**
 * Canonical digest of the accepted puzzle traces, identical to biometric-processor
 * `app/application/services/puzzle_trace_digest.py` (identity-core-api
 * `docs/ATTESTED_PUZZLE_BINDING.md` §4.4). Only traces the server accepted, in the
 * server's ISSUED order, may be included: bio digests exactly what it scored.
 */
object PuzzleTraceDigest {

    private val TRACE_MAGIC = "FPT1".encodeToByteArray()
    private val SESSION_MAGIC = "FPS1".encodeToByteArray()

    fun challengeDigest(action: String, trace: PuzzleTrace): ByteArray {
        val actionBytes = action.encodeToByteArray()
        require(actionBytes.size <= 255) { "action name too long" }
        val w = BigEndianWriter()
            .bytes(TRACE_MAGIC)
            .u8(actionBytes.size).bytes(actionBytes)
            .i32(trace.version).i32(trace.frameWidth).i32(trace.frameHeight)
            .i32(trace.frames.size)
        for (frame in trace.frames) {
            val mask = (if (frame.f != null) 1 else 0) or (if (frame.h != null) 2 else 0)
            w.i64(frame.t).u8(mask)
            frame.f?.forEach { w.i32(it) }
            frame.h?.forEach { w.i32(it) }
        }
        return Sha256.digest(w.toByteArray())
    }

    /** Lowercase hex session digest over the accepted challenges, in issued order. */
    fun sessionDigestHex(accepted: List<AcceptedPuzzleChallenge>): String {
        val w = BigEndianWriter().bytes(SESSION_MAGIC).i32(accepted.size)
        for (c in accepted) {
            w.bytes(challengeDigest(c.action, c.trace))
        }
        return Sha256.digest(w.toByteArray()).toHex()
    }
}
