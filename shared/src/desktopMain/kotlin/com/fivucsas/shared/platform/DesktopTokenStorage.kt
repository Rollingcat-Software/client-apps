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
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TENANT_ID = "tenant_id"
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

    override fun saveUserName(name: String) {
        prefs.put(KEY_USER_NAME, name)
        prefs.flush()
    }

    override fun getUserName(): String? {
        return prefs.get(KEY_USER_NAME, null)
    }

    override fun clearUserName() {
        prefs.remove(KEY_USER_NAME)
        prefs.flush()
    }

    override fun saveUserEmail(email: String) {
        prefs.put(KEY_USER_EMAIL, email)
        prefs.flush()
    }

    override fun getUserEmail(): String? {
        return prefs.get(KEY_USER_EMAIL, null)
    }

    override fun clearUserEmail() {
        prefs.remove(KEY_USER_EMAIL)
        prefs.flush()
    }

    override fun saveUserId(id: String) {
        prefs.put(KEY_USER_ID, id)
        prefs.flush()
    }

    override fun getUserId(): String? {
        return prefs.get(KEY_USER_ID, null)
    }

    override fun clearUserId() {
        prefs.remove(KEY_USER_ID)
        prefs.flush()
    }

    override fun saveTenantId(tenantId: String) {
        prefs.put(KEY_TENANT_ID, tenantId)
        prefs.flush()
    }

    override fun getTenantId(): String? {
        return prefs.get(KEY_TENANT_ID, null)
    }

    override fun clearTenantId() {
        prefs.remove(KEY_TENANT_ID)
        prefs.flush()
    }
}
