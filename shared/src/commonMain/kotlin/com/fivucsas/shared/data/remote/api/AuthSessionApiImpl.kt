package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthSessionDetailDto
import com.fivucsas.shared.data.remote.dto.StartSessionCommand
import com.fivucsas.shared.data.remote.dto.StepResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthSessionApiImpl(
    private val client: HttpClient
) : AuthSessionApi {

    override suspend fun startSession(command: StartSessionCommand): AuthSessionDetailDto {
        return client.post("auth/sessions") {
            contentType(ContentType.Application.Json)
            setBody(command)
        }.body()
    }

    override suspend fun getSession(sessionId: String): AuthSessionDetailDto {
        return client.get("auth/sessions/$sessionId").body()
    }

    override suspend fun completeStep(
        sessionId: String,
        stepOrder: Int,
        data: Map<String, Any?>
    ): StepResultDto {
        return client.post("auth/sessions/$sessionId/steps/$stepOrder") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("data" to data))
        }.body()
    }

    override suspend fun skipStep(sessionId: String, stepOrder: Int): StepResultDto {
        return client.post("auth/sessions/$sessionId/steps/$stepOrder/skip") {
            contentType(ContentType.Application.Json)
            setBody(emptyMap<String, String>())
        }.body()
    }

    override suspend fun cancelSession(sessionId: String) {
        client.post("auth/sessions/$sessionId/cancel") {
            contentType(ContentType.Application.Json)
            setBody(emptyMap<String, String>())
        }
    }
}
