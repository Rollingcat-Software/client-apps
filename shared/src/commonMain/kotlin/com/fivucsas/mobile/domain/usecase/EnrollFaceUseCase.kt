package com.fivucsas.mobile.domain.usecase

import com.fivucsas.mobile.domain.model.BiometricResult
import com.fivucsas.mobile.domain.repository.BiometricRepository

class EnrollFaceUseCase(private val biometricRepository: BiometricRepository) {

    suspend operator fun invoke(userId: String, imageBytes: ByteArray): Result<BiometricResult> {
        if (userId.isBlank()) {
            return Result.failure(Exception("User ID cannot be empty"))
        }
        if (imageBytes.isEmpty()) {
            return Result.failure(Exception("Image data cannot be empty"))
        }

        return biometricRepository.enrollFace(userId, imageBytes)
    }
}
