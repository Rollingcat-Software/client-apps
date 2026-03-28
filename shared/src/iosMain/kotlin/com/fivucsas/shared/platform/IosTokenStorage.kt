package com.fivucsas.shared.platform

import com.fivucsas.shared.data.local.TokenStorage

/**
 * iOS Token Storage Implementation
 *
 * Adapts IosSecureStorage (Keychain) to the TokenStorage interface
 * for secure JWT and user metadata persistence on iOS.
 */
class IosTokenStorage(
    private val secureStorage: ISecureStorage
) : TokenStorage {

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ROLE = "user_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TENANT_ID = "tenant_id"
    }

    override fun saveToken(token: String) {
        secureStorage.saveString(KEY_TOKEN, token)
    }

    override fun getToken(): String? {
        return secureStorage.getString(KEY_TOKEN)
    }

    override fun clearToken() {
        secureStorage.remove(KEY_TOKEN)
    }

    override fun saveRefreshToken(token: String) {
        secureStorage.saveString(KEY_REFRESH_TOKEN, token)
    }

    override fun getRefreshToken(): String? {
        return secureStorage.getString(KEY_REFRESH_TOKEN)
    }

    override fun clearRefreshToken() {
        secureStorage.remove(KEY_REFRESH_TOKEN)
    }

    override fun saveRole(role: String) {
        secureStorage.saveString(KEY_ROLE, role)
    }

    override fun getRole(): String? {
        return secureStorage.getString(KEY_ROLE)
    }

    override fun clearRole() {
        secureStorage.remove(KEY_ROLE)
    }

    override fun saveUserName(name: String) {
        secureStorage.saveString(KEY_USER_NAME, name)
    }

    override fun getUserName(): String? {
        return secureStorage.getString(KEY_USER_NAME)
    }

    override fun clearUserName() {
        secureStorage.remove(KEY_USER_NAME)
    }

    override fun saveUserEmail(email: String) {
        secureStorage.saveString(KEY_USER_EMAIL, email)
    }

    override fun getUserEmail(): String? {
        return secureStorage.getString(KEY_USER_EMAIL)
    }

    override fun clearUserEmail() {
        secureStorage.remove(KEY_USER_EMAIL)
    }

    override fun saveUserId(id: String) {
        secureStorage.saveString(KEY_USER_ID, id)
    }

    override fun getUserId(): String? {
        return secureStorage.getString(KEY_USER_ID)
    }

    override fun clearUserId() {
        secureStorage.remove(KEY_USER_ID)
    }

    override fun saveTenantId(tenantId: String) {
        secureStorage.saveString(KEY_TENANT_ID, tenantId)
    }

    override fun getTenantId(): String? {
        return secureStorage.getString(KEY_TENANT_ID)
    }

    override fun clearTenantId() {
        secureStorage.remove(KEY_TENANT_ID)
    }
}
