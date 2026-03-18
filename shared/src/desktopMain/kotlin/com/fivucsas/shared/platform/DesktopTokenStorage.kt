package com.fivucsas.shared.platform

import com.fivucsas.shared.data.local.TokenStorage
import java.util.prefs.Preferences

/**
 * Desktop Token Storage Implementation
 *
 * Uses Java Preferences API for persistent token storage.
 */
class DesktopTokenStorage : TokenStorage {

    private val prefs = Preferences.userNodeForPackage(DesktopTokenStorage::class.java)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ROLE = "user_role"
    }

    override fun saveToken(token: String) {
        prefs.put(KEY_ACCESS_TOKEN, token)
        prefs.flush()
    }

    override fun getToken(): String? {
        return prefs.get(KEY_ACCESS_TOKEN, null)
    }

    override fun clearToken() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.flush()
    }

    override fun saveRefreshToken(token: String) {
        prefs.put(KEY_REFRESH_TOKEN, token)
        prefs.flush()
    }

    override fun getRefreshToken(): String? {
        return prefs.get(KEY_REFRESH_TOKEN, null)
    }

    override fun clearRefreshToken() {
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.flush()
    }

    override fun saveRole(role: String) {
        prefs.put(KEY_ROLE, role)
        prefs.flush()
    }

    override fun getRole(): String? {
        return prefs.get(KEY_ROLE, null)
    }

    override fun clearRole() {
        prefs.remove(KEY_ROLE)
        prefs.flush()
    }
}
