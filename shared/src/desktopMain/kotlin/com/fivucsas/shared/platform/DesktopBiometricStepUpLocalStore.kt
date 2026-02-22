package com.fivucsas.shared.platform

import com.fivucsas.shared.data.local.BiometricStepUpLocalStore
import com.fivucsas.shared.domain.model.StepUpDto
import java.util.prefs.Preferences

class DesktopBiometricStepUpLocalStore : BiometricStepUpLocalStore {

    private val prefs = Preferences.userNodeForPackage(DesktopBiometricStepUpLocalStore::class.java)
    private var inMemoryStepUp: StepUpDto? = null

    override fun getKeyId(): String? = prefs.get(KEY_KEY_ID, null)

    override fun saveKeyId(keyId: String) {
        prefs.put(KEY_KEY_ID, keyId)
        prefs.flush()
    }

    override fun isDeviceRegistered(): Boolean = prefs.getBoolean(KEY_DEVICE_REGISTERED, false)

    override fun setDeviceRegistered(registered: Boolean) {
        prefs.putBoolean(KEY_DEVICE_REGISTERED, registered)
        prefs.flush()
    }

    override fun saveStepUpTokenInMemory(stepUp: StepUpDto) {
        inMemoryStepUp = stepUp
    }

    override fun getStepUpTokenInMemory(): StepUpDto? = inMemoryStepUp

    private companion object {
        const val KEY_KEY_ID = "stepup_key_id"
        const val KEY_DEVICE_REGISTERED = "stepup_device_registered"
    }
}
