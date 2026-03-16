package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.BiometricData
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult

/**
 * Biometric repository interface
 *
 * Handles all biometric data operations (face recognition, liveness detection)
 */
interface BiometricRepository {
    /**
     * Enroll user's face
     * @param userId User ID
     * @param imageData Face image as byte array
     * @return Result with enrollment result or error
     */
    suspend fun enrollFace(userId: String, imageData: ByteArray): Result<EnrollmentResult>

    /**
     * Verify user's face
     * @param userId User ID to verify against
     * @param imageData Face image as byte array
     * @return Result with verification result or error
     */
    suspend fun verifyFace(userId: String, imageData: ByteArray): Result<VerificationResult>

    /**
     * Check liveness (anti-spoofing) with face image
     * @param imageData Face image as byte array
     * @return Result with liveness check result or error
     */
    suspend fun checkLiveness(imageData: ByteArray): Result<LivenessResult>

    /**
     * Delete user's biometric data
     * @param userId User ID
     * @return Result with success or error
     */
    suspend fun deleteBiometricData(userId: String): Result<Unit>

    /**
     * Identify face (1:N search)
     * @param imageData Face image as byte array
     * @return Result with identification result or error
     */
    suspend fun identifyFace(imageData: ByteArray): Result<IdentifyResult>
}
