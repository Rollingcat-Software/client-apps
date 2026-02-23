package com.fivucsas.shared.test

import com.fivucsas.shared.domain.model.BiometricData
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository

class FakeBiometricRepository : BiometricRepository {

    var shouldSucceed = true
    var errorMessage = "Test error"

    var mockBiometricData = BiometricData(
        id = "bio-1",
        userId = "user-1",
        faceEmbedding = floatArrayOf(0.1f, 0.2f, 0.3f),
        enrollmentDate = "2025-01-01"
    )

    var mockVerificationResult = VerificationResult(
        isVerified = true,
        userId = "user-1",
        confidence = 0.95f,
        message = "Verification successful"
    )

    var mockLivenessResult = LivenessResult(
        isLive = true,
        confidence = 0.98f,
        message = "Liveness check passed"
    )

    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<BiometricData> {
        return if (shouldSucceed) {
            Result.success(mockBiometricData.copy(userId = userId))
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun verifyFace(imageData: ByteArray): Result<VerificationResult> {
        return if (shouldSucceed) {
            Result.success(mockVerificationResult)
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun checkLiveness(actions: List<FacialAction>): Result<LivenessResult> {
        return if (shouldSucceed) {
            Result.success(mockLivenessResult)
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun getBiometricData(userId: String): Result<BiometricData> {
        return if (shouldSucceed) {
            Result.success(mockBiometricData.copy(userId = userId))
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun deleteBiometricData(userId: String): Result<Unit> {
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }
}
