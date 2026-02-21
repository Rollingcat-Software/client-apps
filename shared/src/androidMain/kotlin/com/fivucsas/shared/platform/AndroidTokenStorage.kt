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

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "fivucsas_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    override fun clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
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

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_ROLE = "user_role"
    }
}
