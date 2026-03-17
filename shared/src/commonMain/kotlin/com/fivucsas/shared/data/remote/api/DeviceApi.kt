package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.DeviceDto
import com.fivucsas.shared.data.remote.dto.WebAuthnCredentialDto

/**
 * Device API interface
 *
 * Endpoints:
 * - GET    /devices?userId={userId}                    → getDevices()
 * - DELETE /devices/{id}                               → removeDevice()
 * - GET    /devices/webauthn/credentials/{userId}      → getWebAuthnCredentials()
 */
interface DeviceApi {
    suspend fun getDevices(userId: String): List<DeviceDto>
    suspend fun removeDevice(deviceId: String)
    suspend fun getWebAuthnCredentials(userId: String): List<WebAuthnCredentialDto>
}
