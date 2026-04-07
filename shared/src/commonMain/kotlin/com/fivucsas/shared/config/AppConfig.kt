package com.fivucsas.shared.config

/**
 * Application-wide configuration constants
 *
 * Centralizes all app-level configuration to follow DRY principle
 * and make configuration changes easier to manage.
 */
object AppConfig {
    const val APP_NAME = "FIVUCSAS"
    const val APP_VERSION = "1.0.0"
    const val APP_ID = "com.fivucsas.mobile"

    /**
     * API Configuration
     */
    object Api {
        const val BASE_URL = "https://api.fivucsas.com/api/v1"
        const val API_VERSION = "v1"
        const val TIMEOUT_SECONDS = 30L
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 1000L
    }

    /**
     * Cache Configuration
     */
    object Cache {
        const val MAX_AGE_MINUTES = 15
        const val MAX_SIZE_MB = 50
        const val ENABLE_CACHE = true
    }

    /**
     * Logging Configuration
     */
    object Logging {
        const val ENABLE_DEBUG_LOGS = false
        const val ENABLE_NETWORK_LOGS = false
        const val ENABLE_ANALYTICS = false
    }

    /**
     * Session Configuration
     */
    object Session {
        const val TIMEOUT_MINUTES = 30
        const val AUTO_LOGOUT_ENABLED = true
        const val REMEMBER_ME_DAYS = 30
    }

    /**
     * Pagination Configuration
     */
    object Pagination {
        const val DEFAULT_PAGE_SIZE = 20
        const val MAX_PAGE_SIZE = 100
    }
}
