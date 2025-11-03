package com.fivucsas.shared.domain.repository

/**
 * Authentication repository interface
 * 
 * Handles user authentication and token management
 */
interface AuthRepository {
    /**
     * Login user
     * @param email User email
     * @param password User password
     * @return Result with auth tokens or error
     */
    suspend fun login(email: String, password: String): Result<AuthTokens>
    
    /**
     * Logout user
     * @return Result with success or error
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Refresh access token
     * @param refreshToken Refresh token
     * @return Result with new tokens or error
     */
    suspend fun refreshToken(refreshToken: String): Result<AuthTokens>
    
    /**
     * Check if user is authenticated
     * @return True if authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Get current access token
     * @return Access token or null
     */
    suspend fun getAccessToken(): String?
}

/**
 * Authentication tokens
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)
