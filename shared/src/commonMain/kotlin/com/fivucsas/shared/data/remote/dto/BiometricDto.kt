package com.fivucsas.shared.data.remote.dto

import com.fivucsas.shared.domain.model.BiometricData
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult

/**
 * Data Transfer Object for Biometric Data
 *
 * TODO: Add @Serializable when Ktor is added (Week 2)
 */
data class BiometricDto(
    val id: String,
    val userId: String,
    val faceEmbedding: List<Float>, // JSON uses List instead of FloatArray
    val enrollmentDate: String,
    val lastVerificationDate: String? = null,
    val verificationCount: Int = 0
)

/**
 * Convert DTO to domain model
 */
fun BiometricDto.toModel(): BiometricData {
    return BiometricData(
        id = id,
        userId = userId,
        faceEmbedding = faceEmbedding.toFloatArray(),
        enrollmentDate = enrollmentDate,
        lastVerificationDate = lastVerificationDate,
        verificationCount = verificationCount
    )
}

/**
 * Convert domain model to DTO
 */
fun BiometricData.toDto(): BiometricDto {
    return BiometricDto(
        id = id,
        userId = userId,
        faceEmbedding = faceEmbedding.toList(),
        enrollmentDate = enrollmentDate,
        lastVerificationDate = lastVerificationDate,
        verificationCount = verificationCount
    )
}

/**
 * Verification Response DTO
 */
data class VerificationResponseDto(
    val isVerified: Boolean,
    val userId: String?,
    val confidence: Float,
    val message: String
)

/**
 * Convert DTO to domain model
 */
fun VerificationResponseDto.toModel(): VerificationResult {
    return VerificationResult(
        isVerified = isVerified,
        userId = userId,
        confidence = confidence,
        message = message
    )
}

/**
 * Convert domain model to DTO
 */
fun VerificationResult.toDto(): VerificationResponseDto {
    return VerificationResponseDto(
        isVerified = isVerified,
        userId = userId,
        confidence = confidence,
        message = message
    )
}

/**
 * Liveness Response DTO
 */
data class LivenessResponseDto(
    val isLive: Boolean,
    val confidence: Float,
    val message: String
)

/**
 * Convert DTO to domain model
 */
fun LivenessResponseDto.toModel(): LivenessResult {
    return LivenessResult(
        isLive = isLive,
        confidence = confidence,
        message = message
    )
}

/**
 * Convert domain model to DTO
 */
fun LivenessResult.toDto(): LivenessResponseDto {
    return LivenessResponseDto(
        isLive = isLive,
        confidence = confidence,
        message = message
    )
}
