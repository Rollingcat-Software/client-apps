package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateVerificationSessionRequest
import com.fivucsas.shared.data.remote.dto.VerificationFlowDto
import com.fivucsas.shared.data.remote.dto.VerificationSessionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class VerificationApiImpl(
    private val client: HttpClient
) : VerificationApi {

    override suspend fun getFlows(): List<VerificationFlowDto> {
        return client.get("verification/flows").body()
    }

    override suspend fun getSessions(status: String?): List<VerificationSessionDto> {
        return client.get("verification/sessions") {
            if (status != null) {
                parameter("status", status)
            }
        }.body()
    }

    override suspend fun getSession(sessionId: String): VerificationSessionDto {
        return client.get("verification/sessions/$sessionId").body()
    }

    override suspend fun startSession(request: CreateVerificationSessionRequest): VerificationSessionDto {
        return client.post("verification/sessions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
