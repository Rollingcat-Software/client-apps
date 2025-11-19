package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.BiometricData
import com.fivucsas.shared.domain.model.FacialAction
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
     * @return Result with biometric data or error
     */
    suspend fun enrollFace(userId: String, imageData: ByteArray): Result<BiometricData>

    /**
     * Verify user's face
     * @param imageData Face image as byte array
     * @return Result with verification result or error
     */
    suspend fun verifyFace(imageData: ByteArray): Result<VerificationResult>

    /**
     * Check liveness (anti-spoofing)
     * @param actions List of facial actions performed
     * @return Result with liveness check result or error
     */
    suspend fun checkLiveness(actions: List<FacialAction>): Result<LivenessResult>

    /**
     * Get user's biometric data
     * @param userId User ID
     * @return Result with biometric data or error
     */
    suspend fun getBiometricData(userId: String): Result<BiometricData>

    /**
     * Delete user's biometric data
     * @param userId User ID
     * @return Result with success or error
     */
    suspend fun deleteBiometricData(userId: String): Result<Unit>
}
