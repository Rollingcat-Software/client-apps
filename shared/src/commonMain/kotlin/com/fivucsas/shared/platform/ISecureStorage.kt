package com.fivucsas.shared.platform

/**
 * Secure Storage Interface
 *
 * Platform abstraction for secure data storage.
 * Enables testability and cross-platform support for sensitive data.
 */
interface ISecureStorage {
    /**
     * Save a string value securely
     * @param key Storage key
     * @param value Value to store
     */
    fun saveString(key: String, value: String)

    /**
     * Get a string value
     * @param key Storage key
     * @return Stored value or null if not found
     */
    fun getString(key: String): String?

    /**
     * Save a boolean value
     * @param key Storage key
     * @param value Value to store
     */
    fun saveBoolean(key: String, value: Boolean)

    /**
     * Get a boolean value
     * @param key Storage key
     * @param defaultValue Default value if not found
     * @return Stored value or default
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    /**
     * Save an integer value
     * @param key Storage key
     * @param value Value to store
     */
    fun saveInt(key: String, value: Int)

    /**
     * Get an integer value
     * @param key Storage key
     * @param defaultValue Default value if not found
     * @return Stored value or default
     */
    fun getInt(key: String, defaultValue: Int = 0): Int

    /**
     * Save a long value
     * @param key Storage key
     * @param value Value to store
     */
    fun saveLong(key: String, value: Long)

    /**
     * Get a long value
     * @param key Storage key
     * @param defaultValue Default value if not found
     * @return Stored value or default
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long

    /**
     * Remove a value
     * @param key Storage key
     */
    fun remove(key: String)

    /**
     * Clear all stored values
     */
    fun clear()

    /**
     * Check if a key exists
     * @param key Storage key
     * @return true if key exists
     */
    fun contains(key: String): Boolean
}

/**
 * Storage Keys Constants
 */
object StorageKeys {
    const val AUTH_TOKEN = "auth_token"
    const val REFRESH_TOKEN = "refresh_token"
    const val USER_ID = "user_id"
    const val USER_EMAIL = "user_email"
    const val BIOMETRIC_ENABLED = "biometric_enabled"
    const val THEME_MODE = "theme_mode"
    const val LANGUAGE = "language"
    const val LAST_SYNC = "last_sync"
}
