package com.fivucsas.shared.data.local

/**
 * Stores short-lived biometric step-up token in memory.
 */
class StepUpTokenManager {
    private var token: String? = null

    fun saveToken(stepUpToken: String) {
        token = stepUpToken
    }

    fun getToken(): String? = token

    fun clear() {
        token = null
    }
}

