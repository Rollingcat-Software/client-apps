package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.SessionApi
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.AuthSession
import com.fivucsas.shared.domain.repository.SessionRepository

class SessionRepositoryImpl(
    private val sessionApi: SessionApi
) : SessionRepository {

    override suspend fun getSessions(): Result<List<AuthSession>> {
        return try {
            val sessions = sessionApi.getSessions().map { it.toDomain() }
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun revokeSession(sessionId: String): Result<Unit> {
        return try {
            sessionApi.revokeSession(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
