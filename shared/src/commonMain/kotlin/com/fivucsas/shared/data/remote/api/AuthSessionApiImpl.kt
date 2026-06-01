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
import kotlinx.serialization.Serializable

class AuthSessionApiImpl(
    private val client: HttpClient
) : AuthSessionApi {

    /**
     * Request body wrapper for completeStep. The server expects
     * `{ "data": { ... } }`; constraining the inner map to String values keeps
     * it serializable (kotlinx.serialization has no Any serializer).
     */
    @Serializable
    private data class StepBody(val data: Map<String, String>)

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
        data: Map<String, String>
    ): StepResultDto {
        // Body type MUST be a serializable concrete type. kotlinx.serialization
        // has no serializer for Any, so the step payload is constrained to
        // Map<String, String> (matching the MfaStepRequest.data convention).
        return client.post("auth/sessions/$sessionId/steps/$stepOrder") {
            contentType(ContentType.Application.Json)
            setBody(StepBody(data))
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
