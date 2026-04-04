package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.VerificationApi
import com.fivucsas.shared.data.remote.dto.CreateVerificationSessionRequest
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.VerificationFlow
import com.fivucsas.shared.domain.model.VerificationSession
import com.fivucsas.shared.domain.repository.VerificationRepository

class VerificationRepositoryImpl(
    private val verificationApi: VerificationApi
) : VerificationRepository {

    override suspend fun getFlows(): Result<List<VerificationFlow>> {
        return try {
            val flows = verificationApi.getFlows().map { it.toDomain() }
            Result.success(flows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessions(status: String?): Result<List<VerificationSession>> {
        return try {
            val sessions = verificationApi.getSessions(status).map { it.toDomain() }
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSession(sessionId: String): Result<VerificationSession> {
        return try {
            val session = verificationApi.getSession(sessionId).toDomain()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startSession(flowId: String, userId: String): Result<VerificationSession> {
        return try {
            val request = CreateVerificationSessionRequest(flowId = flowId, userId = userId)
            val session = verificationApi.startSession(request).toDomain()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
