package com.fivucsas.shared.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fivucsas.shared.data.local.TokenStorage

class AndroidTokenStorage(context: Context) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = runCatching {
        EncryptedSharedPreferences.create(
            context,
            "fivucsas_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse {
        // Fallback prevents startup crash on devices/emulators where crypto init fails.
        context.getSharedPreferences("fivucsas_secure_prefs_fallback", Context.MODE_PRIVATE)
    }

    override fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    override fun clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }

    override fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    override fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    override fun clearRefreshToken() {
        sharedPreferences.edit().remove(KEY_REFRESH_TOKEN).apply()
    }

    override fun saveRole(role: String) {
        sharedPreferences.edit().putString(KEY_ROLE, role).apply()
    }

    override fun getRole(): String? {
        return sharedPreferences.getString(KEY_ROLE, null)
    }

    override fun clearRole() {
        sharedPreferences.edit().remove(KEY_ROLE).apply()
    }

    override fun saveUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    override fun getUserName(): String? {
        return sharedPreferences.getString(KEY_USER_NAME, null)
    }

    override fun clearUserName() {
        sharedPreferences.edit().remove(KEY_USER_NAME).apply()
    }

    override fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    override fun getUserEmail(): String? {
        return sharedPreferences.getString(KEY_USER_EMAIL, null)
    }

    override fun clearUserEmail() {
        sharedPreferences.edit().remove(KEY_USER_EMAIL).apply()
    }

    override fun saveUserId(id: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, id).apply()
    }

    override fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }

    override fun clearUserId() {
        sharedPreferences.edit().remove(KEY_USER_ID).apply()
    }

    override fun saveTenantId(tenantId: String) {
        sharedPreferences.edit().putString(KEY_TENANT_ID, tenantId).apply()
    }

    override fun getTenantId(): String? {
        return sharedPreferences.getString(KEY_TENANT_ID, null)
    }

    override fun clearTenantId() {
        sharedPreferences.edit().remove(KEY_TENANT_ID).apply()
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ROLE = "user_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TENANT_ID = "tenant_id"
    }
}
