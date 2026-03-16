package com.fivucsas.shared.data.local

import com.fivucsas.shared.platform.ISecureStorage

/**
 * Stores biometric step-up token with secure persistent backing.
 *
 * Uses in-memory cache for fast access with ISecureStorage as fallback
 * so the token survives app backgrounding on mobile platforms.
 * Tokens expire after a configurable duration to limit security exposure.
 */
class StepUpTokenManager(
    private val secureStorage: ISecureStorage? = null,
    private val tokenLifetimeMs: Long = DEFAULT_TOKEN_LIFETIME_MS
) {
    private var token: String? = null
    private var expiresAt: Long? = null

    companion object {
        private const val STEP_UP_TOKEN_KEY = "step_up_token"
        private const val STEP_UP_TOKEN_EXPIRY_KEY = "step_up_token_expiry"
        private const val DEFAULT_TOKEN_LIFETIME_MS = 5 * 60 * 1000L // 5 minutes
    }

    fun saveToken(stepUpToken: String) {
        val expiry = currentTimeMillis() + tokenLifetimeMs
        token = stepUpToken
        expiresAt = expiry
        secureStorage?.saveString(STEP_UP_TOKEN_KEY, stepUpToken)
        secureStorage?.saveString(STEP_UP_TOKEN_EXPIRY_KEY, expiry.toString())
    }

    fun getToken(): String? {
        // Check in-memory cache first
        if (token != null) {
            val expiry = expiresAt
            if (expiry != null && currentTimeMillis() > expiry) {
                clear()
                return null
            }
            return token
        }
        // Recover from secure storage if memory was cleared
        val storedToken = secureStorage?.getString(STEP_UP_TOKEN_KEY) ?: return null
        val storedExpiry = secureStorage?.getString(STEP_UP_TOKEN_EXPIRY_KEY)?.toLongOrNull()
        if (storedExpiry != null && currentTimeMillis() > storedExpiry) {
            clear()
            return null
        }
        token = storedToken
        expiresAt = storedExpiry
        return token
    }

    fun clear() {
        token = null
        expiresAt = null
        secureStorage?.remove(STEP_UP_TOKEN_KEY)
        secureStorage?.remove(STEP_UP_TOKEN_EXPIRY_KEY)
    }

    private fun currentTimeMillis(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}
