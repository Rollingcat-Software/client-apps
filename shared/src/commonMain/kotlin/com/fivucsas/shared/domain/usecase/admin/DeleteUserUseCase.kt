package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.exception.BusinessException
import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.UserRepository

/**
 * Use case for deleting a user
 *
 * Business logic:
 * 1. Validate user ID
 * 2. Get user to check status
 * 3. Prevent deletion of active users (business rule)
 * 4. Delete biometric data first
 * 5. Delete user
 * 6. Rollback if deletion fails
 */
open class DeleteUserUseCase(
    private val userRepository: UserRepository,
    private val biometricRepository: BiometricRepository
) {
    /**
     * Delete user and associated biometric data
     *
     * @param userId User ID to delete
     * @return Result with Unit or error
     */
    open suspend operator fun invoke(userId: String): Result<Unit> {
        // Validate ID
        if (userId.isBlank()) {
            return Result.failure(ValidationException("User ID is required"))
        }

        // Get user to check status
        val userResult = userRepository.getUserById(userId)
        if (userResult.isFailure) {
            return Result.failure(
                userResult.exceptionOrNull() ?: Exception("User not found")
            )
        }

        val user = userResult.getOrThrow()

        // Business rule: Cannot delete active users
        // Must set to INACTIVE first
        if (user.status == UserStatus.ACTIVE) {
            return Result.failure(
                BusinessException("Cannot delete active user. Please deactivate first.")
            )
        }

        // Delete biometric data if exists
        if (user.hasBiometric) {
            val bioDeleteResult = biometricRepository.deleteBiometricData(userId)
            if (bioDeleteResult.isFailure) {
                return Result.failure(
                    bioDeleteResult.exceptionOrNull()
                        ?: Exception("Failed to delete biometric data")
                )
            }
        }

        // Delete user
        return userRepository.deleteUser(userId)
    }
}
