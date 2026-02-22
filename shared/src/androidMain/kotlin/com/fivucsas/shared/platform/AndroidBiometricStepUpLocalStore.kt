package com.fivucsas.shared.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.fivucsas.shared.data.local.BiometricStepUpLocalStore
import com.fivucsas.shared.domain.model.StepUpDto

class AndroidBiometricStepUpLocalStore(context: Context) : BiometricStepUpLocalStore {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "fivucsas_biometric_stepup",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private var inMemoryStepUp: StepUpDto? = null

    override fun getKeyId(): String? = prefs.getString(KEY_KEY_ID, null)

    override fun saveKeyId(keyId: String) {
        prefs.edit().putString(KEY_KEY_ID, keyId).apply()
    }

    override fun isDeviceRegistered(): Boolean = prefs.getBoolean(KEY_DEVICE_REGISTERED, false)

    override fun setDeviceRegistered(registered: Boolean) {
        prefs.edit().putBoolean(KEY_DEVICE_REGISTERED, registered).apply()
    }

    override fun saveStepUpTokenInMemory(stepUp: StepUpDto) {
        inMemoryStepUp = stepUp
    }

    override fun getStepUpTokenInMemory(): StepUpDto? = inMemoryStepUp

    private companion object {
        const val KEY_KEY_ID = "key_id"
        const val KEY_DEVICE_REGISTERED = "device_registered"
    }
}
