package com.fivucsas.shared.platform.config

import com.fivucsas.shared.config.AnimationConfig
import com.fivucsas.shared.config.AppConfig
import com.fivucsas.shared.config.BiometricConfig
import com.fivucsas.shared.platform.IConfigurationProvider

/**
 * Default Configuration Provider Implementation (Configuration Adapter)
 *
 * Adapts existing configuration objects to IConfigurationProvider interface.
 * Follows Adapter pattern and Hexagonal Architecture.
 *
 * Design Principles Applied:
 * - Adapter Pattern: Adapts existing config objects to port interface
 * - Single Responsibility Principle (SRP): Only adapts configuration
 * - Dependency Inversion Principle (DIP): Implements port interface
 *
 * Hexagonal Architecture Role: ADAPTER (Secondary/Driven Adapter)
 */
class DefaultConfigurationProvider : IConfigurationProvider {
    private val customConfig = mutableMapOf<String, Any>()

    // Application Configuration
    override val appName: String get() = AppConfig.APP_NAME
    override val appVersion: String get() = AppConfig.APP_VERSION
    override val appId: String get() = AppConfig.APP_ID

    // API Configuration
    override val apiBaseUrl: String get() = AppConfig.Api.BASE_URL
    override val apiVersion: String get() = AppConfig.Api.API_VERSION
    override val apiTimeoutSeconds: Long get() = AppConfig.Api.TIMEOUT_SECONDS
    override val apiMaxRetries: Int get() = AppConfig.Api.MAX_RETRIES
    override val apiRetryDelayMs: Long get() = AppConfig.Api.RETRY_DELAY_MS

    // Cache Configuration
    override val cacheMaxAgeMinutes: Int get() = AppConfig.Cache.MAX_AGE_MINUTES
    override val cacheMaxSizeMb: Int get() = AppConfig.Cache.MAX_SIZE_MB
    override val cacheEnabled: Boolean get() = AppConfig.Cache.ENABLE_CACHE

    // Logging Configuration
    override val debugLogsEnabled: Boolean get() = AppConfig.Logging.ENABLE_DEBUG_LOGS
    override val networkLogsEnabled: Boolean get() = AppConfig.Logging.ENABLE_NETWORK_LOGS
    override val analyticsEnabled: Boolean get() = AppConfig.Logging.ENABLE_ANALYTICS

    // Session Configuration
    override val sessionTimeoutMinutes: Int get() = AppConfig.Session.TIMEOUT_MINUTES
    override val autoLogoutEnabled: Boolean get() = AppConfig.Session.AUTO_LOGOUT_ENABLED
    override val rememberMeDays: Int get() = AppConfig.Session.REMEMBER_ME_DAYS

    // Pagination Configuration
    override val defaultPageSize: Int get() = AppConfig.Pagination.DEFAULT_PAGE_SIZE
    override val maxPageSize: Int get() = AppConfig.Pagination.MAX_PAGE_SIZE

    // Biometric Configuration
    override val confidenceThreshold: Float get() = BiometricConfig.CONFIDENCE_THRESHOLD
    override val livenessThreshold: Float get() = BiometricConfig.LIVENESS_THRESHOLD
    override val minFaceSize: Int get() = BiometricConfig.MIN_FACE_SIZE
    override val maxFaceSize: Int get() = BiometricConfig.MAX_FACE_SIZE

    // UI Configuration
    override val animationDurationNormal: Int get() = AnimationConfig.DURATION_NORMAL
    override val animationDurationFast: Int get() = AnimationConfig.DURATION_FAST
    override val animationDelayVerification: Int get() = AnimationConfig.DELAY_VERIFICATION

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String, defaultValue: T): T {
        return customConfig[key] as? T ?: defaultValue
    }

    override fun <T> set(key: String, value: T) {
        customConfig[key] = value as Any
    }

    override fun has(key: String): Boolean {
        return customConfig.containsKey(key)
    }

    override suspend fun reload() {
        // In a real implementation, this would reload configuration from a file or remote source
        // For now, we're using compile-time constants, so nothing to reload
    }
}
