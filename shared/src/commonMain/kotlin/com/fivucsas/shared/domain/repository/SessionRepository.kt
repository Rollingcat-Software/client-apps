package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.AuthSession

interface SessionRepository {
    suspend fun getSessions(): Result<List<AuthSession>>
    suspend fun revokeSession(sessionId: String): Result<Unit>
}
