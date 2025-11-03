package com.fivucsas.shared.domain.usecase.verification

import com.fivucsas.shared.domain.exception.BiometricErrorCode
import com.fivucsas.shared.domain.exception.BiometricException
import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository

/**
 * Use case for verifying a user's face
 * 
 * Business logic:
 * 1. Validate face image
 * 2. Perform face verification against enrolled faces
 * 3. Return verification result with confidence score
 * 
 * Example usage:
 * ```
 * val useCase = VerifyUserUseCase(biometricRepo)
 * val result = useCase(faceImageBytes)
 * when {
 *     result.isSuccess -> {
 *         val verification = result.getOrThrow()
 *         if (verification.isVerified) {
 *             showSuccess(verification.userId, verification.confidence)
 *         } else {
 *             showFailed(verification.message)
 *         }
 *     }
 *     result.isFailure -> showError(result.exceptionOrNull())
 * }
 * ```
 */
class VerifyUserUseCase(
    private val biometricRepository: BiometricRepository
) {
    /**
     * Execute face verification
     * 
     * @param faceImage Captured face image (as byte array)
     * @return Result with verification result or error
     */
    suspend operator fun invoke(faceImage: ByteArray): Result<VerificationResult> {
        // Validate face image
        if (faceImage.isEmpty()) {
            return Result.failure(
                ValidationException("Face image is required for verification")
            )
        }
        
        if (faceImage.size > MAX_IMAGE_SIZE) {
            return Result.failure(
                ValidationException("Face image is too large (max ${MAX_IMAGE_SIZE / 1024 / 1024}MB)")
            )
        }
        
        if (faceImage.size < MIN_IMAGE_SIZE) {
            return Result.failure(
                BiometricException(
                    message = "Face image is too small (min ${MIN_IMAGE_SIZE / 1024}KB)",
                    errorCode = BiometricErrorCode.LOW_QUALITY_IMAGE
                )
            )
        }
        
        // Perform verification
        return biometricRepository.verifyFace(faceImage)
    }
    
    companion object {
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10 MB
        private const val MIN_IMAGE_SIZE = 10 * 1024 // 10 KB
    }
}
