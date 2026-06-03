package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.QrLoginApproveRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginCreateSessionRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginSessionResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

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
        // The web login poller only advances once the SERVER session flips to
        // APPROVED. Unlike createSession/getSession (which call .body() and so
        // throw on a non-2xx via the deserializer), this call read no body and
        // silently swallowed a 401/403 (e.g. an expired bearer) — the phone UI
        // flipped to "APPROVED" while the server stayed PENDING and the browser
        // waited forever. Explicitly fail on a non-success status so the
        // repository/ViewModel surface the real error (and the 401 path triggers
        // a transparent refresh+retry in NetworkModule).
        val response = client.post("$BASE_PATH/$sessionId/approve") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            // Include the numeric status in the message so the shared ErrorMapper
            // (which matches on "401"/"403"/… substrings) maps it to a useful
            // user message instead of a silent false "approved".
            throw ResponseException(
                response,
                "QR approve failed: ${response.status} ${response.bodyAsText()}",
            )
        }
    }
}
