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
    var currentEnvironment: Environment = Environment.PRODUCTION

    /**
     * Identity Core API URLs per environment (Auth, Users, RBAC)
     */
    private const val DEV_IDENTITY_URL = "http://localhost:8080/api/v1"
    private const val STAGING_IDENTITY_URL = "http://34.116.233.134:8080/api/v1"
    private const val PROD_IDENTITY_URL = "http://34.116.233.134:8080/api/v1"

    /**
     * Biometric Processor API URLs per environment (Face detection, Verification)
     */
    private const val DEV_BIOMETRIC_URL = "http://localhost:8001/api/v1"
    private const val STAGING_BIOMETRIC_URL = "https://bpa-fivucsas.rollingcatsoftware.com/api/v1"
    private const val PROD_BIOMETRIC_URL = "https://bpa-fivucsas.rollingcatsoftware.com/api/v1"

    /**
     * Get Identity Core API base URL for current environment
     */
    val identityBaseUrl: String
        get() = when (currentEnvironment) {
            Environment.DEVELOPMENT -> DEV_IDENTITY_URL
            Environment.STAGING -> STAGING_IDENTITY_URL
            Environment.PRODUCTION -> PROD_IDENTITY_URL
        }

    /**
     * Get Biometric Processor API base URL for current environment
     */
    val biometricBaseUrl: String
        get() = when (currentEnvironment) {
            Environment.DEVELOPMENT -> DEV_BIOMETRIC_URL
            Environment.STAGING -> STAGING_BIOMETRIC_URL
            Environment.PRODUCTION -> PROD_BIOMETRIC_URL
        }

    /**
     * Legacy: Get base URL (defaults to Identity API)
     * @deprecated Use identityBaseUrl or biometricBaseUrl instead
     */
    val baseUrl: String
        get() = identityBaseUrl

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

}
