package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User

/**
 * User repository interface - defines contract for data access
 *
 * Implementations can be for API, local cache, or mock data.
 * This follows the Repository Pattern and Dependency Inversion Principle.
 */
interface UserRepository {
    /**
     * Get all users
     * @return Result with list of users or error
     */
    suspend fun getUsers(): Result<List<User>>

    /**
     * Get user by ID
     * @param id User ID
     * @return Result with user or error
     */
    suspend fun getUserById(id: String): Result<User>

    /**
     * Create new user
     * @param user User to create
     * @return Result with created user or error
     */
    suspend fun createUser(user: User): Result<User>

    /**
     * Update existing user
     * @param id User ID
     * @param user Updated user data
     * @return Result with updated user or error
     */
    suspend fun updateUser(id: String, user: User): Result<User>

    /**
     * Delete user
     * @param id User ID
     * @return Result with success or error
     */
    suspend fun deleteUser(id: String): Result<Unit>

    /**
     * Search users by query
     * @param query Search query
     * @return Result with matching users or error
     */
    suspend fun searchUsers(query: String): Result<List<User>>

    /**
     * Get user statistics
     * @return Result with statistics or error
     */
    suspend fun getStatistics(): Result<Statistics>

    /**
     * Get current user's profile
     * @return Result with user or error
     */
    suspend fun getMyProfile(): Result<User>

    /**
     * Check system health
     * @return Result with true if healthy, false otherwise
     */
    suspend fun healthCheck(): Result<Boolean>
}
