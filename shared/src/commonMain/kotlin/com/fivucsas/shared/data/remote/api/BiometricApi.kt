package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto

/**
 * Biometric API interface
 *
 * Defines contract for biometric service communication.
 * TODO: Implement with Ktor client (Week 2, Day 6)
 *
 * Base URL: http://localhost:8000/api/v1/
 *
 * Endpoints:
 * - POST /biometric/enroll      → enrollFace()
 * - POST /biometric/verify      → verifyFace()
 * - POST /biometric/liveness    → checkLiveness()
 * - GET  /biometric/{userId}    → getBiometricData()
 * - DELETE /biometric/{userId}  → deleteBiometricData()
 */
interface BiometricApi {

    /**
     * Enroll face
     * POST /biometric/enroll
     *
     * @param userId User ID
     * @param imageData Base64-encoded image
     */
    suspend fun enrollFace(userId: String, imageData: String): BiometricDto

    /**
     * Verify face
     * POST /biometric/verify
     *
     * @param imageData Base64-encoded image
     */
    suspend fun verifyFace(userId: String, imageData: String): VerificationResponseDto

    /**
     * Check liveness
     * POST /biometric/liveness
     *
     * @param actions List of facial action names
     */
    suspend fun checkLiveness(actions: List<String>): LivenessResponseDto

    /**
     * Get biometric data
     * GET /biometric/{userId}
     */
    suspend fun getBiometricData(userId: String): BiometricDto

    /**
     * Delete biometric data
     * DELETE /biometric/{userId}
     */
    suspend fun deleteBiometricData(userId: String)
}
