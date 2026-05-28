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
     * Current environment (can be changed at runtime).
     *
     * Defaults to PRODUCTION so RELEASE builds never emit request/response
     * logging (see [isLoggingEnabled]) — previously this defaulted to
     * DEVELOPMENT, which leaked full prod traffic into the Ktor logger on
     * shipped APKs. All three environments currently resolve to the same prod
     * hosts (there is no separate staging/dev backend), so PRODUCTION is the
     * correct safe default. A debug entry point may still flip this to
     * DEVELOPMENT at runtime when verbose logging is wanted locally.
     */
    var currentEnvironment: Environment = Environment.PRODUCTION

    /**
     * Identity Core API URLs per environment (Auth, Users, RBAC)
     */
    private const val DEV_IDENTITY_URL = "https://api.fivucsas.com/api/v1"
    private const val STAGING_IDENTITY_URL = "https://api.fivucsas.com/api/v1"
    private const val PROD_IDENTITY_URL = "https://api.fivucsas.com/api/v1"

    /**
     * Biometric API URLs per environment (Face/Voice enroll, verify, search).
     *
     * IMPORTANT: biometrics are served by the Identity Core API at
     * `api.fivucsas.com`, which proxies to the internal biometric-processor.
     * The processor host `bio.fivucsas.com` has NO public DNS (Docker-internal
     * only), so pointing clients at it caused every biometric call to fail with
     * UnresolvedAddressException and broke any login flow using a FACE/VOICE
     * step. These URLs therefore match the Identity base URL above.
     */
    private const val DEV_BIOMETRIC_URL = "https://api.fivucsas.com/api/v1"
    private const val STAGING_BIOMETRIC_URL = "https://api.fivucsas.com/api/v1"
    private const val PROD_BIOMETRIC_URL = "https://api.fivucsas.com/api/v1"

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
