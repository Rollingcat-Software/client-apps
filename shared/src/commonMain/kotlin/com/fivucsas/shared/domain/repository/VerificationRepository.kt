package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.VerificationFlow
import com.fivucsas.shared.domain.model.VerificationSession

interface VerificationRepository {
    suspend fun getFlows(): Result<List<VerificationFlow>>
    suspend fun getSessions(status: String? = null): Result<List<VerificationSession>>
    suspend fun getSession(sessionId: String): Result<VerificationSession>
    suspend fun startSession(flowId: String, userId: String): Result<VerificationSession>
}
