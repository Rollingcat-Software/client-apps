package com.fivucsas.shared.platform

/**
 * Configuration Provider Interface (Configuration Port)
 *
 * Platform abstraction for configuration access following Hexagonal Architecture.
 * Allows domain layer to access configuration without depending on implementation details.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle (DIP): Domain depends on abstraction
 * - Interface Segregation Principle (ISP): Focused interface for configuration
 * - Open/Closed Principle (OCP): Open for extension (new providers), closed for modification
 *
 * Hexagonal Architecture Role: PORT (Secondary/Driven Port)
 */
interface IConfigurationProvider {
    /**
     * Application Configuration
     */
    val appName: String
    val appVersion: String
    val appId: String

    /**
     * API Configuration
     */
    val apiBaseUrl: String
    val apiVersion: String
    val apiTimeoutSeconds: Long
    val apiMaxRetries: Int
    val apiRetryDelayMs: Long

    /**
     * Cache Configuration
     */
    val cacheMaxAgeMinutes: Int
    val cacheMaxSizeMb: Int
    val cacheEnabled: Boolean

    /**
     * Logging Configuration
     */
    val debugLogsEnabled: Boolean
    val networkLogsEnabled: Boolean
    val analyticsEnabled: Boolean

    /**
     * Session Configuration
     */
    val sessionTimeoutMinutes: Int
    val autoLogoutEnabled: Boolean
    val rememberMeDays: Int

    /**
     * Pagination Configuration
     */
    val defaultPageSize: Int
    val maxPageSize: Int

    /**
     * Biometric Configuration
     */
    val confidenceThreshold: Float
    val livenessThreshold: Float
    val minFaceSize: Int
    val maxFaceSize: Int

    /**
     * UI Configuration
     */
    val animationDurationNormal: Int
    val animationDurationFast: Int
    val animationDelayVerification: Int

    /**
     * Get a configuration value by key
     * @param key Configuration key
     * @param defaultValue Default value if key not found
     * @return Configuration value or default
     */
    fun <T> get(key: String, defaultValue: T): T

    /**
     * Set a configuration value
     * @param key Configuration key
     * @param value Configuration value
     */
    fun <T> set(key: String, value: T)

    /**
     * Check if a configuration key exists
     * @param key Configuration key
     * @return true if key exists
     */
    fun has(key: String): Boolean

    /**
     * Reload configuration from source
     */
    suspend fun reload()
}
