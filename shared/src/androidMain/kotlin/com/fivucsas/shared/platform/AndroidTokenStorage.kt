package com.fivucsas.shared.platform

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fivucsas.shared.data.local.TokenStorage

class AndroidTokenStorage(context: Context) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = createEncryptedPrefsWithRecovery(context)

    override fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    override fun clearToken() {
        sharedPreferences.edit().remove(KEY_TOKEN).apply()
    }

    private fun createEncryptedPrefsWithRecovery(context: Context): SharedPreferences {
        return runCatching { createEncryptedPrefs(context) }
            .getOrElse { firstError ->
                Log.w(TAG, "Encrypted prefs init failed, clearing secure prefs and retrying.", firstError)
                context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
                createEncryptedPrefs(context)
            }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val TAG = "AndroidTokenStorage"
        private const val PREFS_FILE_NAME = "fivucsas_secure_prefs"
        private const val KEY_TOKEN = "auth_token"
    }
}
