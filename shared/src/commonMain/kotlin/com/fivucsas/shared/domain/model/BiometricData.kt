package com.fivucsas.shared.domain.model

/**
 * Enrollment result from biometric processor
 */
data class EnrollmentResult(
    val success: Boolean,
    val userId: String,
    val qualityScore: Float,
    val message: String,
    val embeddingDimension: Int = 0,
    val livenessScore: Float = 1.0f
)

/**
 * Verification result from biometric processor
 */
data class VerificationResult(
    val isVerified: Boolean,
    val confidence: Float,
    val distance: Float = 0f,
    val threshold: Float = 0f,
    val message: String
)

/**
 * Liveness check result from biometric processor
 */
data class LivenessResult(
    val isLive: Boolean,
    val livenessScore: Float,
    val challenge: String = "",
    val challengeCompleted: Boolean = false,
    val message: String
)

/**
 * Facial action for liveness detection
 */
enum class FacialAction {
    SMILE,
    BLINK,
    LOOK_LEFT,
    LOOK_RIGHT,
    LOOK_UP,
    LOOK_DOWN,
    OPEN_MOUTH
}
