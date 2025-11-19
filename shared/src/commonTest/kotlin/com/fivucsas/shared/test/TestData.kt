package com.fivucsas.shared.test

import com.fivucsas.shared.domain.model.EnrollmentData
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus

/**
 * Test data factory
 * Provides consistent test data across all tests
 */
object TestData {

    // Test Users
    val testUser1 = User(
        id = "user-1",
        name = "John Doe",
        email = "john.doe@test.com",
        idNumber = "ID001",
        phoneNumber = "+1234567890",
        status = UserStatus.ACTIVE,
        enrollmentDate = "2025-01-01",
        hasBiometric = true
    )

    val testUser2 = User(
        id = "user-2",
        name = "Jane Smith",
        email = "jane.smith@test.com",
        idNumber = "ID002",
        phoneNumber = "+0987654321",
        status = UserStatus.ACTIVE,
        enrollmentDate = "2025-01-02",
        hasBiometric = true
    )

    val inactiveUser = User(
        id = "user-3",
        name = "Bob Inactive",
        email = "bob@test.com",
        idNumber = "ID003",
        phoneNumber = "+1111111111",
        status = UserStatus.INACTIVE,
        enrollmentDate = "2025-01-03",
        hasBiometric = false
    )

    // Test Statistics
    val testStatistics = Statistics(
        totalUsers = 100,
        activeUsers = 85,
        verificationsToday = 250,
        successRate = 95.5,
        failedAttempts = 12
    )

    // Test Enrollment Data
    val testEnrollmentData = EnrollmentData(
        fullName = "Test User",
        email = "test@example.com",
        idNumber = "ID999",
        phoneNumber = "+1234567890"
    )

    // Helper functions
    fun createTestUser(
        id: String = "test-id",
        name: String = "Test User",
        email: String = "test@example.com",
        idNumber: String = "ID999",
        status: UserStatus = UserStatus.ACTIVE
    ) = User(
        id = id,
        name = name,
        email = email,
        idNumber = idNumber,
        phoneNumber = "+1234567890",
        status = status,
        enrollmentDate = "2025-01-01",
        hasBiometric = false
    )

    fun createTestUsers(count: Int): List<User> {
        return (1..count).map { i ->
            createTestUser(
                id = "user-$i",
                name = "User $i",
                email = "user$i@test.com",
                idNumber = "ID$i"
            )
        }
    }
}
