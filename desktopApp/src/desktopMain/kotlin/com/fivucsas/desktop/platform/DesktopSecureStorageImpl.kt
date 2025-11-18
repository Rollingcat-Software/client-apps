package com.fivucsas.desktop.platform

import com.fivucsas.shared.platform.ISecureStorage
import java.util.prefs.Preferences

/**
 * Desktop Secure Storage Implementation
 *
 * Desktop implementation of ISecureStorage using Java Preferences API.
 * In production, consider using encryption for sensitive data.
 */
class DesktopSecureStorageImpl : ISecureStorage {

    private val preferences: Preferences = Preferences.userNodeForPackage(
        DesktopSecureStorageImpl::class.java
    )

    override fun saveString(key: String, value: String) {
        preferences.put(key, value)
        preferences.flush()
    }

    override fun getString(key: String): String? {
        return preferences.get(key, null)
    }

    override fun saveBoolean(key: String, value: Boolean) {
        preferences.putBoolean(key, value)
        preferences.flush()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    override fun saveInt(key: String, value: Int) {
        preferences.putInt(key, value)
        preferences.flush()
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    override fun saveLong(key: String, value: Long) {
        preferences.putLong(key, value)
        preferences.flush()
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }

    override fun remove(key: String) {
        preferences.remove(key)
        preferences.flush()
    }

    override fun clear() {
        preferences.clear()
        preferences.flush()
    }

    override fun contains(key: String): Boolean {
        return preferences.get(key, null) != null
    }
}
