package com.fivucsas.shared.domain.model

/**
 * Privacy-safe guest face-check outcome.
 *
 * Guest flow must not expose user identity details.
 */
enum class GuestFaceCheckOutcome {
    FOUND,
    NOT_FOUND
}

/**
 * Confidence is returned as a coarse band only.
 */
enum class ConfidenceBand {
    LOW,
    MEDIUM,
    HIGH
}

fun confidenceToBand(confidence: Float): ConfidenceBand {
    return when {
        confidence >= 0.85f -> ConfidenceBand.HIGH
        confidence >= 0.60f -> ConfidenceBand.MEDIUM
        else -> ConfidenceBand.LOW
    }
}
