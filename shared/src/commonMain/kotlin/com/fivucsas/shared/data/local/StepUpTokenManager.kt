package com.fivucsas.shared.data.local

import com.fivucsas.shared.platform.ISecureStorage

/**
 * Stores biometric step-up token with secure persistent backing.
 *
 * Uses in-memory cache for fast access with ISecureStorage as fallback
 * so the token survives app backgrounding on mobile platforms.
 */
class StepUpTokenManager(
    private val secureStorage: ISecureStorage? = null
) {
    private var token: String? = null

    companion object {
        private const val STEP_UP_TOKEN_KEY = "step_up_token"
    }

    fun saveToken(stepUpToken: String) {
        token = stepUpToken
        secureStorage?.saveString(STEP_UP_TOKEN_KEY, stepUpToken)
    }

    fun getToken(): String? {
        if (token != null) return token
        // Recover from secure storage if memory was cleared
        token = secureStorage?.getString(STEP_UP_TOKEN_KEY)
        return token
    }

    fun clear() {
        token = null
        secureStorage?.remove(STEP_UP_TOKEN_KEY)
    }
}
