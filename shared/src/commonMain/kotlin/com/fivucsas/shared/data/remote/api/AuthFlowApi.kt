package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthFlowDto

/**
 * Auth Flow API interface
 *
 * Endpoints:
 * - GET /tenants/{tenantId}/auth-flows → getAuthFlows()
 */
interface AuthFlowApi {
    suspend fun getAuthFlows(tenantId: String): List<AuthFlowDto>
}
