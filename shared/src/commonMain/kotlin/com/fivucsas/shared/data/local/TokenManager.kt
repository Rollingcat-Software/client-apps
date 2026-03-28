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

    private var cachedUserName: String? = null
    private var cachedUserEmail: String? = null
    private var cachedUserId: String? = null
    private var cachedTenantId: String? = null

    /**
     * Save authentication tokens
     */
    fun saveTokens(tokens: AuthTokens) {
        cachedTokens = tokens
        tokenStorage.saveToken(tokens.accessToken)
        tokenStorage.saveRefreshToken(tokens.refreshToken)
        tokenStorage.saveRole(tokens.role)
        cachedRole = tokens.role
        if (tokens.userName.isNotBlank()) {
            tokenStorage.saveUserName(tokens.userName)
            cachedUserName = tokens.userName
        }
        if (tokens.userEmail.isNotBlank()) {
            tokenStorage.saveUserEmail(tokens.userEmail)
            cachedUserEmail = tokens.userEmail
        }
        if (tokens.userId.isNotBlank()) {
            tokenStorage.saveUserId(tokens.userId)
            cachedUserId = tokens.userId
        }
        if (tokens.tenantId.isNotBlank()) {
            tokenStorage.saveTenantId(tokens.tenantId)
            cachedTenantId = tokens.tenantId
        }
    }

    /**
     * Get current access token
     */
    fun getAccessToken(): String? {
        return cachedTokens?.accessToken ?: tokenStorage.getToken()
    }

    /**
     * Get current refresh token (from cache or persistent storage)
     */
    fun getRefreshToken(): String? {
        return cachedTokens?.refreshToken ?: tokenStorage.getRefreshToken()
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
     * Get current user name
     */
    fun getUserName(): String? {
        return cachedUserName ?: tokenStorage.getUserName()
    }

    /**
     * Get current user email
     */
    fun getUserEmail(): String? {
        return cachedUserEmail ?: tokenStorage.getUserEmail()
    }

    /**
     * Get current user id
     */
    fun getUserId(): String? {
        return cachedUserId ?: tokenStorage.getUserId()
    }

    /**
     * Get current tenant id
     */
    fun getTenantId(): String? {
        return cachedTenantId ?: tokenStorage.getTenantId()
    }

    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        cachedTokens = null
        cachedRole = null
        cachedUserName = null
        cachedUserEmail = null
        cachedUserId = null
        cachedTenantId = null
        tokenStorage.clearToken()
        tokenStorage.clearRefreshToken()
        tokenStorage.clearRole()
        tokenStorage.clearUserName()
        tokenStorage.clearUserEmail()
        tokenStorage.clearUserId()
        tokenStorage.clearTenantId()
    }

    /**
     * Update tokens after refresh
     */
    fun updateTokens(tokens: AuthTokens) {
        saveTokens(tokens)
    }
}
