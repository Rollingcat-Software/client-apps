package com.fivucsas.shared.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.parameter

class NfcApprovalApiImpl(
    private val client: HttpClient
) : NfcApprovalApi {

    companion object {
        private const val BASE_PATH = "auth/approval"
    }

    override suspend fun decide(sessionId: String, decision: String) {
        client.post("$BASE_PATH/$sessionId/decide") {
            parameter("decision", decision)
        }
    }
}
