package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricDto
import com.fivucsas.shared.data.remote.dto.IdentificationResponseDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto

/**
 * Biometric API interface
 *
 * Defines contract for biometric processor service communication.
 * Connects to FastAPI service on port 8001.
 *
 * Endpoints:
 * - POST /enroll       → enrollFace() (multipart form-data)
 * - POST /verify       → verifyFace() (multipart form-data)
 * - POST /liveness     → checkLiveness() (multipart form-data)
 * - DELETE /enroll/{id} → deleteBiometricData()
 */
interface BiometricApi {

    /**
     * Enroll face using multipart form-data
     * POST /enroll
     *
     * @param userId User ID
     * @param imageBytes Raw image bytes (JPEG/PNG)
     * @param imageName Filename for the image
     */
    suspend fun enrollFace(userId: String, imageBytes: ByteArray, imageName: String = "face.jpg", tenantId: String? = null): BiometricEnrollmentResponseDto

    /**
     * Verify face using multipart form-data
     * POST /verify
     *
     * @param userId User ID to verify against
     * @param imageBytes Raw image bytes (JPEG/PNG)
     * @param imageName Filename for the image
     */
    suspend fun verifyFace(userId: String, imageBytes: ByteArray, imageName: String = "face.jpg"): VerificationResponseDto

    /**
     * Check liveness using multipart form-data with image
     * POST /liveness
     *
     * @param imageBytes Raw image bytes (JPEG/PNG)
     * @param imageName Filename for the image
     */
    suspend fun checkLiveness(imageBytes: ByteArray, imageName: String = "face.jpg"): LivenessResponseDto

    /**
     * Delete biometric data for a user
     * DELETE /enroll/{userId}
     */
    suspend fun deleteBiometricData(userId: String)

    /**
     * Identify face (1:N search)
     * POST /search
     *
     * @param imageData Base64-encoded image
     */
    suspend fun identifyFace(imageData: String): IdentificationResponseDto
}
