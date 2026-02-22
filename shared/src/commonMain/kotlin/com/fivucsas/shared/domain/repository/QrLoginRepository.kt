package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.QrLoginSession

interface QrLoginRepository {
    suspend fun createSession(platform: String): Result<QrLoginSession>
    suspend fun getSession(sessionId: String): Result<QrLoginSession>
    suspend fun approveSession(sessionId: String, approverPlatform: String): Result<Unit>
}
