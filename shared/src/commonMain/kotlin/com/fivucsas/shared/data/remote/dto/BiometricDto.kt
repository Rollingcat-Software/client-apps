package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enrollment Response DTO — matches biometric-processor EnrollmentResponse schema:
 * {success, user_id, quality_score, message, embedding_dimension, liveness_score}
 */
@Serializable
data class BiometricEnrollmentResponseDto(
    val success: Boolean,
    @SerialName("user_id")
    val userId: String,
    @SerialName("quality_score")
    val qualityScore: Float,
    val message: String,
    @SerialName("embedding_dimension")
    val embeddingDimension: Int = 0,
    @SerialName("liveness_score")
    val livenessScore: Float = 1.0f
)

fun BiometricEnrollmentResponseDto.toModel(): EnrollmentResult {
    return EnrollmentResult(
        success = success,
        userId = userId,
        qualityScore = qualityScore,
        message = message,
        embeddingDimension = embeddingDimension,
        livenessScore = livenessScore
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
