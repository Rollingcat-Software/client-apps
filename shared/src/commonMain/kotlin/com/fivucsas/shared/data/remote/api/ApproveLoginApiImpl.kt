package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.ApproveLoginDecisionDto
import com.fivucsas.shared.data.remote.dto.PendingApproveLoginDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

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
        client.post("$BASE_PATH/session/$sessionId/decide") {
            contentType(ContentType.Application.Json)
            setBody(ApproveLoginDecisionDto(decision = decision, matchNumber = matchNumber))
        }
    }
}
