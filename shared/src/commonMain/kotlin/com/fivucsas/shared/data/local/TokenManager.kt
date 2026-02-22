package com.fivucsas.shared.data.local

import com.fivucsas.shared.domain.repository.AuthTokens

/**
 * Token Manager - Centralized token management
 *
 * Handles access token and refresh token storage/retrieval.
 * Used by NetworkModule for automatic JWT injection.
 */
class TokenManager(
    private val tokenStorage: TokenStorage
) {
    private var cachedTokens: AuthTokens? = null
    private var cachedRole: String? = null

    /**
     * Save authentication tokens
     */
    fun saveTokens(tokens: AuthTokens) {
        cachedTokens = tokens
        tokenStorage.saveToken(tokens.accessToken)
        tokenStorage.saveRole(tokens.role)
        cachedRole = tokens.role
    }

    /**
     * Get current access token
     */
    fun getAccessToken(): String? {
        return cachedTokens?.accessToken ?: tokenStorage.getToken()
    }

    /**
     * Get current refresh token
     */
    fun getRefreshToken(): String? {
        return cachedTokens?.refreshToken
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null
    }

    /**
     * Get current user role
     */
    fun getRole(): String? {
        return cachedRole ?: tokenStorage.getRole()
    }

    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        cachedTokens = null
        cachedRole = null
        tokenStorage.clearToken()
        tokenStorage.clearRole()
    }

    /**
     * Update tokens after refresh
     */
    fun updateTokens(tokens: AuthTokens) {
        saveTokens(tokens)
    }
}
