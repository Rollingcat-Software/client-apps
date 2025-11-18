package com.fivucsas.shared.test.mocks

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.usecase.admin.DeleteUserUseCase
import com.fivucsas.shared.domain.usecase.admin.GetStatisticsUseCase
import com.fivucsas.shared.domain.usecase.admin.GetUsersUseCase
import com.fivucsas.shared.domain.usecase.admin.UpdateUserUseCase

/**
 * Mock implementations for Admin use cases
 *
 * These mocks allow testing ViewModels without actual backend dependencies.
 */

/**
 * Mock GetUsersUseCase
 */
class MockGetUsersUseCase : GetUsersUseCase {
    var shouldSucceed = true
    var mockUsers = listOf(
        User(
            id = "user_1",
            name = "John Doe",
            email = "john@example.com",
            idNumber = "ID123456",
            phoneNumber = "+1234567890",
            status = UserStatus.ACTIVE,
            enrollmentDate = "2024-01-01",
            hasBiometric = true
        ),
        User(
            id = "user_2",
            name = "Jane Smith",
            email = "jane@example.com",
            idNumber = "ID789012",
            phoneNumber = "+0987654321",
            status = UserStatus.ACTIVE,
            enrollmentDate = "2024-01-02",
            hasBiometric = false
        )
    )
    var errorMessage = "Failed to get users"

    override suspend fun invoke(): Result<List<User>> {
        return if (shouldSucceed) {
            Result.success(mockUsers)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

/**
 * Mock DeleteUserUseCase
 */
class MockDeleteUserUseCase : DeleteUserUseCase {
    var shouldSucceed = true
    var deletedUserId: String? = null
    var errorMessage = "Failed to delete user"

    override suspend fun invoke(userId: String): Result<Unit> {
        deletedUserId = userId
        return if (shouldSucceed) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

/**
 * Mock UpdateUserUseCase
 */
class MockUpdateUserUseCase : UpdateUserUseCase {
    var shouldSucceed = true
    var updatedUser: User? = null
    var errorMessage = "Failed to update user"

    override suspend fun invoke(userId: String, user: User): Result<User> {
        updatedUser = user
        return if (shouldSucceed) {
            Result.success(user)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}

/**
 * Mock GetStatisticsUseCase
 */
class MockGetStatisticsUseCase : GetStatisticsUseCase {
    var shouldSucceed = true
    var mockStatistics = Statistics(
        totalUsers = 100,
        activeUsers = 85,
        verificationsToday = 42,
        successRate = 95.5,
        failedAttempts = 3,
        pendingVerifications = 5
    )
    var errorMessage = "Failed to get statistics"

    override suspend fun invoke(): Result<Statistics> {
        return if (shouldSucceed) {
            Result.success(mockStatistics)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}
