package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.AuthSessionApi
import com.fivucsas.shared.data.remote.dto.StartSessionCommand
import com.fivucsas.shared.domain.model.AuthSessionDetail
import com.fivucsas.shared.domain.model.SessionStep
import com.fivucsas.shared.domain.model.StepResult
import com.fivucsas.shared.domain.repository.AuthSessionRepository

class AuthSessionRepositoryImpl(
    private val authSessionApi: AuthSessionApi
) : AuthSessionRepository {

    override suspend fun startSession(
        tenantId: String,
        userId: String,
        operationType: String
    ): Result<AuthSessionDetail> {
        return try {
            val dto = authSessionApi.startSession(
                StartSessionCommand(
                    tenantId = tenantId,
                    userId = userId,
                    operationType = operationType
                )
            )
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSession(sessionId: String): Result<AuthSessionDetail> {
        return try {
            val dto = authSessionApi.getSession(sessionId)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeStep(
        sessionId: String,
        stepOrder: Int,
        data: Map<String, Any?>
    ): Result<StepResult> {
        return try {
            // The wire body must be Map<String, String> (no Any serializer).
            // Drop nulls and stringify any non-String values at this boundary so
            // the upstream domain/presentation layers keep their Map<String, Any?>
            // convenience signature.
            val stringData: Map<String, String> = data.entries
                .mapNotNull { (key, value) -> value?.let { key to it.toString() } }
                .toMap()
            val dto = authSessionApi.completeStep(sessionId, stepOrder, stringData)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun skipStep(sessionId: String, stepOrder: Int): Result<StepResult> {
        return try {
            val dto = authSessionApi.skipStep(sessionId, stepOrder)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelSession(sessionId: String): Result<Unit> {
        return try {
            authSessionApi.cancelSession(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun com.fivucsas.shared.data.remote.dto.AuthSessionDetailDto.toDomain() = AuthSessionDetail(
    sessionId = sessionId,
    tenantId = tenantId,
    userId = userId,
    operationType = operationType,
    status = status,
    currentStepOrder = currentStepOrder,
    totalSteps = totalSteps,
    steps = steps.map { it.toDomain() },
    expiresAt = expiresAt,
    createdAt = createdAt,
    completedAt = completedAt
)

private fun com.fivucsas.shared.data.remote.dto.SessionStepDto.toDomain() = SessionStep(
    stepOrder = stepOrder,
    authMethodType = authMethodType,
    isRequired = isRequired,
    status = status,
    completedAt = completedAt,
    delegated = delegated
)

private fun com.fivucsas.shared.data.remote.dto.StepResultDto.toDomain() = StepResult(
    sessionId = sessionId,
    stepOrder = stepOrder,
    status = status,
    message = message,
    nextStepOrder = nextStepOrder,
    sessionCompleted = sessionCompleted,
    data = data
)
