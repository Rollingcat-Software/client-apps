package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.DeviceDto
import com.fivucsas.shared.data.remote.dto.WebAuthnCredentialDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class DeviceApiImpl(
    private val client: HttpClient
) : DeviceApi {

    override suspend fun getDevices(userId: String): List<DeviceDto> {
        return client.get("devices") {
            parameter("userId", userId)
        }.body()
    }

    override suspend fun removeDevice(deviceId: String) {
        client.delete("devices/$deviceId")
    }

    override suspend fun getWebAuthnCredentials(userId: String): List<WebAuthnCredentialDto> {
        return client.get("devices/webauthn/credentials/$userId").body()
    }
}
