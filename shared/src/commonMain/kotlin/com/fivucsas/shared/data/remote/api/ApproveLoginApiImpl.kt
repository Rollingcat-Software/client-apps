package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.ApproveLoginDecisionDto
import com.fivucsas.shared.data.remote.dto.PendingApproveLoginDto
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

class ApproveLoginApiImpl(
    private val client: HttpClient
) : ApproveLoginApi {

    companion object {
        private const val BASE_PATH = "auth/approve-login"
    }

    override suspend fun listPending(): List<PendingApproveLoginDto> {
        return client.get("$BASE_PATH/pending").body()
    }

    override suspend fun decide(sessionId: String, decision: String, matchNumber: String?) {
        // Mirrors the QR-approve fix: this call read no body, so a non-2xx
        // (e.g. an expired bearer → 401/403) was silently swallowed — the phone
        // showed the login as approved while the server session stayed PENDING
        // and the web/originating login never advanced. Fail explicitly so the
        // repository/ViewModel surface the real error (the 401 path also triggers
        // a transparent refresh+retry in NetworkModule).
        val response = client.post("$BASE_PATH/session/$sessionId/decide") {
            contentType(ContentType.Application.Json)
            setBody(ApproveLoginDecisionDto(decision = decision, matchNumber = matchNumber))
        }
        if (!response.status.isSuccess()) {
            throw ResponseException(
                response,
                "Approve-login decision failed: ${response.status} ${response.bodyAsText()}",
            )
        }
    }
}
