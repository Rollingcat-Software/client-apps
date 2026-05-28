package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enrollment Response DTO — matches the Identity Core API
 * `BiometricVerificationResponse` schema returned by
 * `POST /api/v1/biometric/enroll/{userId}`:
 * {verified, confidence, message, distance, threshold}
 *
 * NOTE: the legacy biometric-processor enroll schema
 * ({success, user_id, quality_score, embedding_dimension, liveness_score})
 * is NOT what the identity API surfaces. Every field below has a default so a
 * partial/legacy payload still deserializes without throwing. `quality_score`
 * is kept as an optional alias in case a future identity response echoes it.
 */
@Serializable
data class BiometricEnrollmentResponseDto(
    val verified: Boolean = false,
    val confidence: Float = 0f,
    val message: String = "",
    val distance: Float? = null,
    val threshold: Float? = null,
    @SerialName("quality_score")
    val qualityScore: Float? = null
)

fun BiometricEnrollmentResponseDto.toModel(): EnrollmentResult {
    return EnrollmentResult(
        success = verified,
        // Identity enroll does not echo the user id; the caller already knows it.
        userId = "",
        // Use the processor-reported quality score when present, else fall back
        // to the verification confidence as a coarse proxy.
        qualityScore = qualityScore ?: confidence,
        message = message,
        embeddingDimension = 0,
        livenessScore = 1.0f
    )
}

/**
 * Verification Response DTO — matches biometric-processor VerificationResponse schema:
 * {verified, confidence, distance, threshold, message}
 */
@Serializable
data class VerificationResponseDto(
    val verified: Boolean,
    val confidence: Float,
    val distance: Float = 0f,
    val threshold: Float = 0f,
    val message: String
)

fun VerificationResponseDto.toModel(): VerificationResult {
    return VerificationResult(
        isVerified = verified,
        confidence = confidence,
        distance = distance,
        threshold = threshold,
        message = message
    )
}

/**
 * Liveness Response DTO — matches biometric-processor LivenessResponse schema:
 * {is_live, liveness_score, challenge, challenge_completed, message}
 */
@Serializable
data class LivenessResponseDto(
    @SerialName("is_live")
    val isLive: Boolean,
    @SerialName("liveness_score")
    val livenessScore: Float,
    val challenge: String = "",
    @SerialName("challenge_completed")
    val challengeCompleted: Boolean = false,
    val message: String
)

fun LivenessResponseDto.toModel(): LivenessResult {
    return LivenessResult(
        isLive = isLive,
        livenessScore = livenessScore,
        challenge = challenge,
        challengeCompleted = challengeCompleted,
        message = message
    )
}
