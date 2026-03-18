package com.fivucsas.shared.test.mocks

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.verification.CheckLivenessUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase
import com.fivucsas.shared.test.FakeBiometricRepository
import com.fivucsas.shared.test.FakeUserRepository

/**
 * Mock implementations for Kiosk use cases
 *
 * These mocks allow testing ViewModels without actual backend dependencies.
 */

class MockEnrollUserUseCase : EnrollUserUseCase(FakeUserRepository(), FakeBiometricRepository()) {
    var shouldSucceed = true
    var lastEnrollmentData: EnrollmentData? = null
    var lastImageBytes: ByteArray? = null
    var mockUser = User(
        id = "new_user_123",
        name = "Test User",
        email = "test@example.com",
        idNumber = "ID999",
        phoneNumber = "+1234567890",
        status = UserStatus.ACTIVE,
        enrollmentDate = "2025-01-01",
        hasBiometric = true
    )
    var errorMessage = "Failed to enroll user"

    override suspend fun invoke(
        enrollmentData: EnrollmentData,
        faceImage: ByteArray
    ): Result<User> {
        lastEnrollmentData = enrollmentData
        lastImageBytes = faceImage
        return if (shouldSucceed) {
            Result.success(mockUser)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

class MockVerifyUserUseCase : VerifyUserUseCase(FakeBiometricRepository()) {
    var shouldSucceed = true
    var lastUserId: String? = null
    var lastImageBytes: ByteArray? = null
    var mockResult = VerificationResult(
        isVerified = true,
        confidence = 95.5f,
        message = "Verification successful"
    )
    var errorMessage = "Failed to verify user"

    override suspend fun invoke(userId: String, faceImage: ByteArray): Result<VerificationResult> {
        lastUserId = userId
        lastImageBytes = faceImage
        return if (shouldSucceed) {
            Result.success(mockResult)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

class MockCheckLivenessUseCase : CheckLivenessUseCase(FakeBiometricRepository()) {
    var shouldSucceed = true
    var lastImageBytes: ByteArray? = null
    var mockResult = LivenessResult(
        isLive = true,
        livenessScore = 0.95f,
        message = "Liveness check passed"
    )
    var errorMessage = "Failed to check liveness"

    override suspend fun invoke(faceImage: ByteArray): Result<LivenessResult> {
        lastImageBytes = faceImage
        return if (shouldSucceed) {
            Result.success(mockResult)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}
