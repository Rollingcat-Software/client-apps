package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricEnrollmentResponseDto
import com.fivucsas.shared.data.remote.dto.IdentificationResponseDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto

/**
 * Biometric API interface.
 *
 * Contract for the Identity Core API biometric endpoints (api.fivucsas.com),
 * which proxy to the internal biometric-processor. Paths are relative to
 * `{base}/api/v1`.
 *
 * Endpoints:
 * - POST   biometric/enroll/{userId}  → enrollFace()  (multipart `image`)
 * - POST   biometric/verify/{userId}  → verifyFace()  (multipart `image`)
 * - POST   biometric/search           → identifyFace() (multipart `file`)
 * - DELETE biometric/face/{userId}    → deleteBiometricData()
 *
 * Liveness has NO standalone identity endpoint — see checkLiveness().
 */
interface BiometricApi {

    /**
     * Enroll face using multipart form-data.
     * POST biometric/enroll/{userId}
     *
     * @param userId User ID (goes in the PATH)
     * @param imageBytes Raw image bytes (JPEG/PNG), multipart part name `image`
     * @param imageName Filename for the image
     * @param tenantId Optional tenant context (accepted as `tenant_id` form field)
     */
    suspend fun enrollFace(userId: String, imageBytes: ByteArray, imageName: String = "face.jpg", tenantId: String? = null): BiometricEnrollmentResponseDto

    /**
     * Verify face using multipart form-data.
     * POST biometric/verify/{userId}
     *
     * @param userId User ID to verify against (goes in the PATH)
     * @param imageBytes Raw image bytes (JPEG/PNG), multipart part name `image`
     * @param imageName Filename for the image
     */
    suspend fun verifyFace(userId: String, imageBytes: ByteArray, imageName: String = "face.jpg"): VerificationResponseDto

    /**
     * Check liveness.
     *
     * The Identity Core API has no standalone liveness endpoint; passive
     * liveness is performed server-side inside /verify. This call therefore
     * returns a safe, non-blocking result instead of hitting the network — it
     * must never block login. See BiometricApiImpl for the TODO.
     */
    suspend fun checkLiveness(imageBytes: ByteArray, imageName: String = "face.jpg"): LivenessResponseDto

    /**
     * Delete biometric (face) data for a user.
     * DELETE biometric/face/{userId}
     */
    suspend fun deleteBiometricData(userId: String)

    /**
     * Identify face (1:N search).
     * POST biometric/search (multipart `file`; tenant derived server-side)
     *
     * @param imageBytes Raw image bytes (JPEG/PNG)
     * @param imageName Filename for the image
     */
    suspend fun identifyFace(imageBytes: ByteArray, imageName: String = "face.jpg"): IdentificationResponseDto
}
