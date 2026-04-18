package com.fivucsas.authenticator.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fivucsas.authenticator.totp.TotpAccount
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class TotpVault(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context.applicationContext,
        PREFS_NAME,
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val serializer = ListSerializer(TotpAccount.serializer())

    fun getAll(): List<TotpAccount> {
        val raw = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return runCatching { json.decodeFromString(serializer, raw) }.getOrElse { emptyList() }
    }

    fun getById(id: String): TotpAccount? = getAll().firstOrNull { it.id == id }

    fun add(account: TotpAccount) {
        val current = getAll().filterNot { it.id == account.id }
        write(current + account)
    }

    fun delete(id: String) {
        write(getAll().filterNot { it.id == id })
    }

    fun rename(id: String, issuer: String, accountName: String) {
        write(
            getAll().map {
                if (it.id == id) it.copy(issuer = issuer, accountName = accountName) else it
            }
        )
    }

    fun clear() {
        prefs.edit().remove(KEY_ACCOUNTS).apply()
    }

    private fun write(accounts: List<TotpAccount>) {
        val encoded = json.encodeToString(serializer, accounts)
        prefs.edit().putString(KEY_ACCOUNTS, encoded).apply()
    }

    companion object {
        private const val PREFS_NAME = "fivucsas_totp_vault"
        private const val KEY_ACCOUNTS = "accounts"
    }
}
