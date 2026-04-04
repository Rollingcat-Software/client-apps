package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuditLogPageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class AuditLogApiImpl(
    private val client: HttpClient
) : AuditLogApi {

    override suspend fun getAuditLogs(
        action: String?,
        userId: String?,
        page: Int,
        size: Int
    ): AuditLogPageDto {
        return client.get("audit-logs") {
            parameter("page", page)
            parameter("size", size)
            action?.let { parameter("action", it) }
            userId?.let { parameter("userId", it) }
        }.body()
    }
}
