package com.fivucsas.shared.test

import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

/**
 * Fake User Repository for testing
 * Provides predictable test data without network calls
 */
class FakeUserRepository(
    private var users: MutableList<User> = mutableListOf()
) : UserRepository {
    
    var shouldThrowError = false
    var errorMessage = "Test error"
    
    override suspend fun getUsers(): Result<List<User>> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            Result.success(users.toList())
        }
    }
    
    override suspend fun getUserById(id: String): Result<User> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            users.find { it.id == id }?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("User not found"))
        }
    }
    
    override suspend fun createUser(user: User): Result<User> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            users.add(user)
            Result.success(user)
        }
    }
    
    override suspend fun updateUser(id: String, user: User): Result<User> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            val index = users.indexOfFirst { it.id == id }
            if (index != -1) {
                users[index] = user
                Result.success(user)
            } else {
                Result.failure(NoSuchElementException("User not found"))
            }
        }
    }
    
    override suspend fun deleteUser(id: String): Result<Unit> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            users.removeIf { it.id == id }
            Result.success(Unit)
        }
    }
    
    override suspend fun searchUsers(query: String): Result<List<User>> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            val filtered = users.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.email.contains(query, ignoreCase = true) ||
                it.idNumber.contains(query, ignoreCase = true)
            }
            Result.success(filtered)
        }
    }
    
    override suspend fun getStatistics(): Result<Statistics> {
        return if (shouldThrowError) {
            Result.failure(RuntimeException(errorMessage))
        } else {
            Result.success(TestData.testStatistics)
        }
    }
    
    // Helper methods for tests
    fun addUser(user: User) {
        users.add(user)
    }
    
    fun addUsers(vararg users: User) {
        this.users.addAll(users)
    }
    
    fun clear() {
        users.clear()
    }
    
    fun setUsers(userList: List<User>) {
        users.clear()
        users.addAll(userList)
    }
}
