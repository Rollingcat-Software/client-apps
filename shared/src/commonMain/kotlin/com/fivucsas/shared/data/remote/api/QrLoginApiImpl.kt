package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.QrLoginApproveRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginCreateSessionRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginSessionResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class QrLoginApiImpl(
    private val client: HttpClient
) : QrLoginApi {

    companion object {
        private const val BASE_PATH = "auth/qr/session"
    }

    override suspend fun createSession(request: QrLoginCreateSessionRequestDto): QrLoginSessionResponseDto {
        return client.post(BASE_PATH) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getSession(sessionId: String): QrLoginSessionResponseDto {
        return client.get("$BASE_PATH/$sessionId").body()
    }

    override suspend fun approveSession(sessionId: String, request: QrLoginApproveRequestDto) {
        client.post("$BASE_PATH/$sessionId/approve") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
