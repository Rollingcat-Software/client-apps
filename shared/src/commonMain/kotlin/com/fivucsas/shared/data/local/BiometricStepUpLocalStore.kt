package com.fivucsas.shared.data.local

import com.fivucsas.shared.domain.model.StepUpDto

interface BiometricStepUpLocalStore {
    fun getKeyId(): String?
    fun saveKeyId(keyId: String)
    fun isDeviceRegistered(): Boolean
    fun setDeviceRegistered(registered: Boolean)
    fun saveStepUpTokenInMemory(stepUp: StepUpDto)
    fun getStepUpTokenInMemory(): StepUpDto?
}
