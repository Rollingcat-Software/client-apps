package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthSessionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get

class SessionApiImpl(
    private val client: HttpClient
) : SessionApi {

    override suspend fun getSessions(): List<AuthSessionDto> {
        return client.get("sessions").body()
    }

    override suspend fun revokeSession(sessionId: String) {
        client.delete("sessions/$sessionId")
    }
}
