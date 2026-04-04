package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.DeviceDto
import com.fivucsas.shared.data.remote.dto.WebAuthnCredentialDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

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

    override suspend fun registerPushToken(userId: String, token: String, platform: String) {
        client.post("devices/push-token") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "userId" to userId,
                "token" to token,
                "platform" to platform
            ))
        }
    }
}
