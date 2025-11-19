package com.fivucsas.shared.data.repository

import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import kotlinx.coroutines.delay

/**
 * Mock implementation of AuthRepository
 *
 * Simulates authentication for development.
 * TODO: Replace with real JWT/OAuth implementation (Week 2)
 *
 * Mock credentials (for testing):
 * - admin@fivucsas.com / admin123
 * - user@fivucsas.com / user123
 */
class AuthRepositoryImpl : AuthRepository {

    // In-memory token storage
    private var currentTokens: AuthTokens? = null

    // Mock user credentials
    private val validCredentials = mapOf(
        "admin@fivucsas.com" to "admin123",
        "user@fivucsas.com" to "user123",
        "test@fivucsas.com" to "test123"
    )

    override suspend fun login(email: String, password: String): Result<AuthTokens> {
        return try {
            // Simulate network delay
            delay(1000)

            // Validate credentials
            val validPassword = validCredentials[email]
            if (validPassword == null || validPassword != password) {
                return Result.failure(
                    IllegalArgumentException("Invalid email or password")
                )
            }

            // Generate mock tokens
            val timestamp = generateTimestamp()
            val tokens = AuthTokens(
                accessToken = "mock_access_token_$timestamp",
                refreshToken = "mock_refresh_token_$timestamp",
                expiresIn = 3600 // 1 hour
            )

            // Store tokens
            currentTokens = tokens

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens> {
        return try {
            // Simulate network delay
            delay(1500)

            // Check if email already exists
            if (registeredUsers.containsKey(email)) {
                return Result.failure(
                    IllegalArgumentException("Email already registered")
                )
            }

            // Register new user
            registeredUsers[email] = password

            // Generate mock tokens
            val timestamp = generateTimestamp()
            val tokens = AuthTokens(
                accessToken = "mock_access_token_$timestamp",
                refreshToken = "mock_refresh_token_$timestamp",
                expiresIn = 3600 // 1 hour
            )

            // Store tokens
            currentTokens = tokens

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            delay(300)

            // Clear tokens
            currentTokens = null

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return try {
            delay(500)

            // Validate refresh token
            if (currentTokens?.refreshToken != refreshToken) {
                return Result.failure(
                    IllegalArgumentException("Invalid refresh token")
                )
            }

            // Generate new tokens
            val timestamp = generateTimestamp()
            val tokens = AuthTokens(
                accessToken = "mock_access_token_refreshed_$timestamp",
                refreshToken = "mock_refresh_token_refreshed_$timestamp",
                expiresIn = 3600
            )

            // Update stored tokens
            currentTokens = tokens

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        // In mock: just check if tokens exist
        // TODO: Real implementation would validate token expiry
        return currentTokens != null
    }

    override suspend fun getAccessToken(): String? {
        return currentTokens?.accessToken
    }

    /**
     * Generate timestamp for token IDs
     * TODO: Replace with kotlinx-datetime when added
     */
    private fun generateTimestamp(): String {
        // Simple counter-based timestamp for mock
        return (++tokenCounter).toString()
    }

    companion object {
        private var tokenCounter = 0

        // Track registered users (mock storage)
        private val registeredUsers = mutableMapOf<String, String>()
    }
}
