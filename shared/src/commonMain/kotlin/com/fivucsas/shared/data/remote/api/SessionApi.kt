package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthSessionDto

/**
 * Session API interface
 *
 * Endpoints:
 * - GET    /auth/sessions/my      → getSessions()
 * - DELETE /auth/sessions/my/{id} → revokeSession()
 */
interface SessionApi {
    suspend fun getSessions(): List<AuthSessionDto>
    suspend fun revokeSession(sessionId: String)
}
