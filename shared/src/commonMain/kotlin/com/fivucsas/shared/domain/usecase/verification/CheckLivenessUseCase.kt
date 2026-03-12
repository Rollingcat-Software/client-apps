package com.fivucsas.shared.domain.usecase.verification

import com.fivucsas.shared.domain.exception.BiometricErrorCode
import com.fivucsas.shared.domain.exception.BiometricException
import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.repository.BiometricRepository

/**
 * Use case for checking liveness (anti-spoofing)
 *
 * Business logic:
 * 1. Validate face image
 * 2. Send image to biometric processor for passive liveness detection
 * 3. Return liveness result with score
 *
 * Liveness detection prevents spoofing attacks:
 * - Photo attacks (holding a photo)
 * - Video replay attacks
 * - Mask attacks (3D masks)
 *
 * Example usage:
 * ```
 * val useCase = CheckLivenessUseCase(biometricRepo)
 * val result = useCase(faceImageBytes)
 * ```
 */
open class CheckLivenessUseCase(
    private val biometricRepository: BiometricRepository
) {
    /**
     * Execute liveness check with face image
     *
     * @param faceImage Face image as byte array
     * @return Result with liveness result or error
     */
    open suspend operator fun invoke(faceImage: ByteArray): Result<LivenessResult> {
        if (faceImage.isEmpty()) {
            return Result.failure(
                ValidationException("Face image is required for liveness check")
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

        return biometricRepository.checkLiveness(faceImage)
    }

    companion object {
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10 MB
        private const val MIN_IMAGE_SIZE = 10 * 1024 // 10 KB
    }
}
