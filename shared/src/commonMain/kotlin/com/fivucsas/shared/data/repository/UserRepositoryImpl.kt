package com.fivucsas.shared.data.repository

import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.model.UserStatus
import com.fivucsas.shared.domain.repository.UserRepository
import kotlinx.coroutines.delay

/**
 * Mock implementation of UserRepository
 * 
 * Uses in-memory data for development and testing.
 * TODO: Replace with API implementation when backend is ready (Week 2)
 * 
 * This follows the Repository Pattern:
 * - Interface in domain/ (UserRepository)
 * - Implementation in data/ (this file)
 * - Easy to swap mock → real API later
 */
class UserRepositoryImpl : UserRepository {
    
    // In-memory data storage (simulates database)
    private val users = mutableListOf(
        User(
            id = "1",
            name = "Ahmet Abdullah Gültekin",
            email = "ahmet@example.com",
            idNumber = "12345678901",
            phoneNumber = "+90 532 123 4567",
            status = UserStatus.ACTIVE,
            enrollmentDate = "2025-01-15",
            hasBiometric = true
        ),
        User(
            id = "2",
            name = "Ayşe Yılmaz",
            email = "ayse@example.com",
            idNumber = "98765432109",
            phoneNumber = "+90 533 987 6543",
            status = UserStatus.ACTIVE,
            enrollmentDate = "2025-02-20",
            hasBiometric = true
        ),
        User(
            id = "3",
            name = "Mehmet Kaya",
            email = "mehmet@example.com",
            idNumber = "55566677788",
            phoneNumber = "+90 534 555 6666",
            status = UserStatus.PENDING,
            enrollmentDate = "2025-03-10",
            hasBiometric = false
        ),
        User(
            id = "4",
            name = "Fatma Demir",
            email = "fatma@example.com",
            idNumber = "11122233344",
            phoneNumber = "+90 535 111 2222",
            status = UserStatus.ACTIVE,
            enrollmentDate = "2025-01-25",
            hasBiometric = true
        ),
        User(
            id = "5",
            name = "Ali Çelik",
            email = "ali@example.com",
            idNumber = "99988877766",
            phoneNumber = "+90 536 999 8888",
            status = UserStatus.INACTIVE,
            enrollmentDate = "2024-12-01",
            hasBiometric = true
        )
    )
    
    override suspend fun getUsers(): Result<List<User>> {
        return try {
            // Simulate network delay
            delay(500)
            Result.success(users.toList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserById(id: String): Result<User> {
        return try {
            delay(300)
            val user = users.find { it.id == id }
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(NoSuchElementException("User with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createUser(user: User): Result<User> {
        return try {
            delay(500)
            
            // Generate new ID
            val newId = (users.size + 1).toString()
            
            // Create new user with generated ID and current date
            val newUser = user.copy(
                id = newId,
                enrollmentDate = getCurrentDate(),
                status = UserStatus.PENDING,
                hasBiometric = false
            )
            
            users.add(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(id: String, user: User): Result<User> {
        return try {
            delay(500)
            val index = users.indexOfFirst { it.id == id }
            if (index != -1) {
                users[index] = user
                Result.success(user)
            } else {
                Result.failure(NoSuchElementException("User with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            delay(500)
            val initialSize = users.size
            users.removeAll { it.id == id }
            val removed = users.size < initialSize
            if (removed) {
                Result.success(Unit)
            } else {
                Result.failure(NoSuchElementException("User with id $id not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            delay(300)
            
            if (query.isBlank()) {
                return Result.success(users.toList())
            }
            
            val results = users.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true) ||
                it.idNumber.contains(query) ||
                it.phoneNumber.contains(query)
            }
            
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStatistics(): Result<Statistics> {
        return try {
            delay(500)
            
            val stats = Statistics(
                totalUsers = users.size,
                activeUsers = users.count { it.status == UserStatus.ACTIVE },
                pendingVerifications = users.count { it.status == UserStatus.PENDING },
                verificationsToday = 89, // Mock value
                successRate = 94.2, // Mock value
                failedAttempts = 12 // Mock value
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current date
     * TODO: Replace with actual date/time library (kotlinx-datetime)
     */
    private fun getCurrentDate(): String {
        return "2025-11-03"
    }
}
