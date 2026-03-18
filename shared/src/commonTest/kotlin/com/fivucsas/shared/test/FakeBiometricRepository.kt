package com.fivucsas.shared.test

import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository

class FakeBiometricRepository : BiometricRepository {

    var shouldSucceed = true
    var errorMessage = "Test error"

    var mockEnrollmentResult = EnrollmentResult(
        success = true,
        userId = "user-1",
        qualityScore = 0.95f,
        message = "Enrollment successful"
    )

    var mockVerificationResult = VerificationResult(
        isVerified = true,
        confidence = 0.95f,
        message = "Verification successful"
    )

    var mockLivenessResult = LivenessResult(
        isLive = true,
        livenessScore = 0.98f,
        message = "Liveness check passed"
    )

    var mockIdentifyResult = IdentifyResult(
        userId = "user-1",
        name = "Test User",
        confidence = 0.95f,
        isMatch = true
    )

    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<EnrollmentResult> {
        return if (shouldSucceed) {
            Result.success(mockEnrollmentResult.copy(userId = userId))
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun verifyFace(userId: String, imageData: ByteArray): Result<VerificationResult> {
        return if (shouldSucceed) {
            Result.success(mockVerificationResult)
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun checkLiveness(imageData: ByteArray): Result<LivenessResult> {
        return if (shouldSucceed) {
            Result.success(mockLivenessResult)
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

    override suspend fun identifyFace(imageData: ByteArray): Result<IdentifyResult> {
        return if (shouldSucceed) {
            Result.success(mockIdentifyResult)
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }
}
