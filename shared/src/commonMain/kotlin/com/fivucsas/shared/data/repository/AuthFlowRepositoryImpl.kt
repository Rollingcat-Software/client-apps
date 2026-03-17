package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.AuthFlowApi
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.AuthFlow
import com.fivucsas.shared.domain.repository.AuthFlowRepository

class AuthFlowRepositoryImpl(
    private val authFlowApi: AuthFlowApi
) : AuthFlowRepository {

    override suspend fun getAuthFlows(tenantId: String): Result<List<AuthFlow>> {
        return try {
            val flows = authFlowApi.getAuthFlows(tenantId).map { it.toDomain() }
            Result.success(flows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
