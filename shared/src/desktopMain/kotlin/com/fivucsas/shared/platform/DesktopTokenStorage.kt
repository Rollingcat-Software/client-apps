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
}
