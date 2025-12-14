package com.fivucsas.shared.data.remote.config

/**
 * API Configuration
 * Centralized configuration for API endpoints and network settings
 */
object ApiConfig {

    /**
     * Environment types
     */
    enum class Environment {
        DEVELOPMENT,
        STAGING,
        PRODUCTION
    }

    /**
     * Current environment (can be changed at runtime)
     */
    var currentEnvironment: Environment = Environment.DEVELOPMENT

    /**
     * Base URLs per environment
     */
    private const val DEV_BASE_URL = "http://localhost:8080/api/v1"
    private const val STAGING_BASE_URL = "https://staging.fivucsas.com/api/v1"
    private const val PROD_BASE_URL = "https://api.fivucsas.com/api/v1"

    /**
     * Get base URL for current environment
     */
    val baseUrl: String
        get() = when (currentEnvironment) {
            Environment.DEVELOPMENT -> DEV_BASE_URL
            Environment.STAGING -> STAGING_BASE_URL
            Environment.PRODUCTION -> PROD_BASE_URL
        }

    /**
     * Timeout configuration (milliseconds)
     */
    const val CONNECT_TIMEOUT_MS = 30_000L
    const val REQUEST_TIMEOUT_MS = 60_000L
    const val SOCKET_TIMEOUT_MS = 30_000L

    /**
     * Retry configuration
     */
    const val MAX_RETRIES = 3
    const val RETRY_DELAY_MS = 1000L

    /**
     * Logging configuration
     */
    val isLoggingEnabled: Boolean
        get() = currentEnvironment != Environment.PRODUCTION

    /**
     * Feature flags
     */
    var useRealApi: Boolean = true // Set to false to use mock data
}
