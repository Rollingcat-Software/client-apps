package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.StatisticsDto
import com.fivucsas.shared.data.remote.dto.UserDto

/**
 * Identity API interface
 *
 * Defines contract for backend communication.
 * Base URL: https://api.fivucsas.com/api/v1/
 *
 * Endpoints:
 * - GET    /users          → getUsers()
 * - GET    /users/{id}     → getUserById()
 * - POST   /users          → createUser()
 * - PUT    /users/{id}     → updateUser()
 * - DELETE /users/{id}     → deleteUser()
 * - GET    /users/search   → searchUsers()
 * - GET    /statistics     → getStatistics()
 */
interface IdentityApi {

    /**
     * Get all users
     * GET /users
     */
    suspend fun getUsers(): List<UserDto>

    /**
     * Get user by ID
     * GET /users/{id}
     */
    suspend fun getUserById(id: String): UserDto

    /**
     * Create new user
     * POST /users
     */
    suspend fun createUser(user: UserDto): UserDto

    /**
     * Update user
     * PUT /users/{id}
     */
    suspend fun updateUser(id: String, user: UserDto): UserDto

    /**
     * Delete user
     * DELETE /users/{id}
     */
    suspend fun deleteUser(id: String)

    /**
     * Search users
     * GET /users/search?q={query}
     */
    suspend fun searchUsers(query: String): List<UserDto>

    /**
     * Get statistics
     * GET /statistics
     */
    suspend fun getStatistics(): StatisticsDto

    /**
     * Get current user's profile
     * GET /auth/me
     */
    suspend fun getMyProfile(): UserDto

    /**
     * Health check
     * GET /health
     */
    suspend fun healthCheck(): Boolean
}
