package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.Device
import com.fivucsas.shared.domain.model.WebAuthnCredential

interface DeviceRepository {
    suspend fun getDevices(userId: String): Result<List<Device>>
    suspend fun removeDevice(deviceId: String): Result<Unit>
    suspend fun getWebAuthnCredentials(userId: String): Result<List<WebAuthnCredential>>
}
