package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuthFlow

interface AuthFlowRepository {
    suspend fun getAuthFlows(tenantId: String): Result<List<AuthFlow>>
}
