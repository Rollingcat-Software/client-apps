package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthFlowDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class AuthFlowApiImpl(
    private val client: HttpClient
) : AuthFlowApi {

    override suspend fun getAuthFlows(tenantId: String): List<AuthFlowDto> {
        return client.get("tenants/$tenantId/auth-flows").body()
    }
}
