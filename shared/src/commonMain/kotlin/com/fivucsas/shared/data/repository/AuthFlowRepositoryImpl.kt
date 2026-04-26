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

    override suspend fun getActiveFlow(
        operationType: String,
        tenantId: String?
    ): Result<AuthFlow?> {
        // No tenant means we cannot scope the lookup — there is no public
        // cross-tenant discovery endpoint, so we fall back to "no flow".
        if (tenantId.isNullOrBlank()) {
            return Result.success(null)
        }
        return try {
            val flows = authFlowApi.getAuthFlows(tenantId).map { it.toDomain() }
            // Pick the active flow that matches the operation type. Prefer
            // the default flow when multiple match.
            val match = flows
                .filter { it.isActive && it.operationType.equals(operationType, ignoreCase = true) }
                .sortedByDescending { it.isDefault }
                .firstOrNull()
            Result.success(match)
        } catch (_: Exception) {
            // Any failure (auth required, network, parse) collapses to "no
            // flow configured" so the LoginScreen falls back to PASSWORD.
            Result.success(null)
        }
    }
}
