package com.fivucsas.shared.platform

import platform.Foundation.*
import platform.Security.*
import kotlinx.cinterop.*

/**
 * iOS Secure Storage Implementation
 *
 * Uses iOS Keychain Services for secure data storage.
 * Follows Hexagonal Architecture by implementing ISecureStorage interface.
 *
 * Design Principles Applied:
 * - Dependency Inversion Principle: Implements platform abstraction
 * - Single Responsibility: Handles only secure storage operations
 * - Encapsulation: Hides Keychain complexity behind simple interface
 *
 * iOS Keychain Features:
 * - Hardware-backed encryption
 * - Secure enclave storage for sensitive data
 * - Automatic iCloud sync support (optional)
 * - Access control with biometric authentication
 *
 * Security Properties:
 * - kSecAttrAccessibleWhenUnlocked: Data accessible only when device unlocked
 * - kSecClassGenericPassword: Generic password storage class
 */
@OptIn(ExperimentalForeignApi::class)
class IosSecureStorage : ISecureStorage {

    private val serviceName = "com.fivucsas.mobile"

    /**
     * Save a string value to Keychain
     */
    override fun saveString(key: String, value: String) {
        // First, try to delete existing value
        deleteValue(key)

        // Create query dictionary
        val query = createKeychainQuery(key).apply {
            this[kSecValueData] = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            this[kSecAttrAccessible] = kSecAttrAccessibleWhenUnlocked
        }

        // Add to keychain
        val status = SecItemAdd(query as CFDictionaryRef, null)

        if (status != errSecSuccess) {
            throw Exception("Failed to save to Keychain: $status")
        }
    }

    /**
     * Get a string value from Keychain
     */
    override fun getString(key: String): String? {
        val query = createKeychainQuery(key).apply {
            this[kSecReturnData] = kCFBooleanTrue
            this[kSecMatchLimit] = kSecMatchLimitOne
        }

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as? NSData
                return data?.let {
                    NSString.create(it, NSUTF8StringEncoding) as String
                }
            }
        }

        return null
    }

    /**
     * Save a boolean value
     */
    override fun saveBoolean(key: String, value: Boolean) {
        saveString(key, value.toString())
    }

    /**
     * Get a boolean value
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    /**
     * Save an integer value
     */
    override fun saveInt(key: String, value: Int) {
        saveString(key, value.toString())
    }

    /**
     * Get an integer value
     */
    override fun getInt(key: String, defaultValue: Int): Int {
        return getString(key)?.toIntOrNull() ?: defaultValue
    }

    /**
     * Save a long value
     */
    override fun saveLong(key: String, value: Long) {
        saveString(key, value.toString())
    }

    /**
     * Get a long value
     */
    override fun getLong(key: String, defaultValue: Long): Long {
        return getString(key)?.toLongOrNull() ?: defaultValue
    }

    /**
     * Remove a value from Keychain
     */
    override fun remove(key: String) {
        deleteValue(key)
    }

    /**
     * Clear all stored values
     * WARNING: This clears all items for this service
     */
    override fun clear() {
        val query = mutableMapOf<Any?, Any?>().apply {
            this[kSecClass] = kSecClassGenericPassword
            this[kSecAttrService] = serviceName
        }

        SecItemDelete(query as CFDictionaryRef)
    }

    /**
     * Check if a key exists
     */
    override fun contains(key: String): Boolean {
        val query = createKeychainQuery(key).apply {
            this[kSecReturnData] = kCFBooleanFalse
            this[kSecMatchLimit] = kSecMatchLimitOne
        }

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)
            return status == errSecSuccess
        }
    }

    /**
     * Create base Keychain query dictionary
     */
    private fun createKeychainQuery(key: String): MutableMap<Any?, Any?> {
        return mutableMapOf<Any?, Any?>().apply {
            this[kSecClass] = kSecClassGenericPassword
            this[kSecAttrService] = serviceName
            this[kSecAttrAccount] = key
        }
    }

    /**
     * Delete value from Keychain
     */
    private fun deleteValue(key: String) {
        val query = createKeychainQuery(key)
        SecItemDelete(query as CFDictionaryRef)
    }
}

/**
 * iOS Keychain Storage Keys
 * Extends common StorageKeys with iOS-specific keys
 */
object IosStorageKeys {
    // Biometric authentication
    const val BIOMETRIC_PUBLIC_KEY = "biometric_public_key"
    const val BIOMETRIC_PRIVATE_KEY = "biometric_private_key"
    const val FACE_ID_ENABLED = "face_id_enabled"
    const val TOUCH_ID_ENABLED = "touch_id_enabled"

    // Device specific
    const val DEVICE_ID = "device_id"
    const val INSTALLATION_ID = "installation_id"

    // Security
    const val ENCRYPTION_KEY = "encryption_key"
    const val SALT = "salt"
}
