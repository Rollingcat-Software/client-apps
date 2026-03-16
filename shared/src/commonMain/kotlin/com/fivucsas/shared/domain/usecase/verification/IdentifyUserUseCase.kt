package com.fivucsas.shared.domain.usecase.verification

import com.fivucsas.shared.domain.exception.BiometricErrorCode
import com.fivucsas.shared.domain.exception.BiometricException
import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.repository.BiometricRepository

/**
 * Use case for identifying a face via 1:N search.
 *
 * Business logic:
 * 1. Validate face image is present and within size bounds
 * 2. Perform 1:N identification against enrolled faces
 * 3. Return identification result with match details
 */
class IdentifyUserUseCase(
    private val biometricRepository: BiometricRepository
) {
    suspend operator fun invoke(faceImage: ByteArray): Result<IdentifyResult> {
        if (faceImage.isEmpty()) {
            return Result.failure(
                ValidationException("Face image is required for identification")
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

        return biometricRepository.identifyFace(faceImage)
    }

    companion object {
        private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10 MB
        private const val MIN_IMAGE_SIZE = 10 * 1024 // 10 KB
    }
}
