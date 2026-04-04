package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuthSessionDetail
import com.fivucsas.shared.domain.model.StepResult

/**
 * Repository interface for multi-step auth session operations.
 */
interface AuthSessionRepository {
    suspend fun startSession(
        tenantId: String,
        userId: String,
        operationType: String
    ): Result<AuthSessionDetail>

    suspend fun getSession(sessionId: String): Result<AuthSessionDetail>

    suspend fun completeStep(
        sessionId: String,
        stepOrder: Int,
        data: Map<String, Any?>
    ): Result<StepResult>

    suspend fun skipStep(sessionId: String, stepOrder: Int): Result<StepResult>

    suspend fun cancelSession(sessionId: String): Result<Unit>
}
