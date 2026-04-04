package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthSessionDetailDto
import com.fivucsas.shared.data.remote.dto.StartSessionCommand
import com.fivucsas.shared.data.remote.dto.StepResultDto

/**
 * Auth Session API interface for multi-step authentication flow.
 *
 * Endpoints:
 * - POST   /auth/sessions                                → startSession()
 * - GET    /auth/sessions/{sessionId}                     → getSession()
 * - POST   /auth/sessions/{sessionId}/steps/{stepOrder}   → completeStep()
 * - POST   /auth/sessions/{sessionId}/steps/{stepOrder}/skip → skipStep()
 * - POST   /auth/sessions/{sessionId}/cancel              → cancelSession()
 */
interface AuthSessionApi {
    suspend fun startSession(command: StartSessionCommand): AuthSessionDetailDto
    suspend fun getSession(sessionId: String): AuthSessionDetailDto
    suspend fun completeStep(sessionId: String, stepOrder: Int, data: Map<String, Any?>): StepResultDto
    suspend fun skipStep(sessionId: String, stepOrder: Int): StepResultDto
    suspend fun cancelSession(sessionId: String)
}
