package com.fivucsas.shared.test.mocks

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.usecase.enrollment.EnrollUserUseCase
import com.fivucsas.shared.domain.usecase.verification.CheckLivenessUseCase
import com.fivucsas.shared.domain.usecase.verification.VerifyUserUseCase

/**
 * Mock implementations for Kiosk use cases
 *
 * These mocks allow testing ViewModels without actual backend dependencies.
 */

/**
 * Mock EnrollUserUseCase
 */
class MockEnrollUserUseCase : EnrollUserUseCase {
    var shouldSucceed = true
    var lastEnrollmentData: EnrollmentData? = null
    var lastImageBytes: ByteArray? = null
    var mockResult = EnrollmentResult(
        userId = "new_user_123",
        success = true,
        message = "Enrollment successful"
    )
    var errorMessage = "Failed to enroll user"

    override suspend fun invoke(
        data: EnrollmentData,
        imageBytes: ByteArray
    ): Result<EnrollmentResult> {
        lastEnrollmentData = data
        lastImageBytes = imageBytes
        return if (shouldSucceed) {
            Result.success(mockResult)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

/**
 * Mock VerifyUserUseCase
 */
class MockVerifyUserUseCase : VerifyUserUseCase {
    var shouldSucceed = true
    var lastImageBytes: ByteArray? = null
    var mockResult = VerificationResult(
        userId = "user_123",
        isVerified = true,
        confidence = 95.5f,
        message = "Verification successful"
    )
    var errorMessage = "Failed to verify user"

    override suspend fun invoke(imageBytes: ByteArray): Result<VerificationResult> {
        lastImageBytes = imageBytes
        return if (shouldSucceed) {
            Result.success(mockResult)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

/**
 * Mock CheckLivenessUseCase
 */
class MockCheckLivenessUseCase : CheckLivenessUseCase {
    var shouldSucceed = true
    var lastImageBytes: ByteArray? = null
    var isLive = true
    var confidence = 0.95f
    var errorMessage = "Failed to check liveness"

    override suspend fun invoke(imageBytes: ByteArray): Result<Pair<Boolean, Float>> {
        lastImageBytes = imageBytes
        return if (shouldSucceed) {
            Result.success(Pair(isLive, confidence))
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}
