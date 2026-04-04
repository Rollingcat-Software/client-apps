package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateVerificationSessionRequest
import com.fivucsas.shared.data.remote.dto.VerificationFlowDto
import com.fivucsas.shared.data.remote.dto.VerificationSessionDto

/**
 * Verification Pipeline API interface
 *
 * Endpoints:
 * - GET  /verification/flows             → getFlows()
 * - GET  /verification/sessions          → getSessions()
 * - GET  /verification/sessions/{id}     → getSession()
 * - POST /verification/sessions          → startSession()
 */
interface VerificationApi {
    suspend fun getFlows(): List<VerificationFlowDto>
    suspend fun getSessions(status: String? = null): List<VerificationSessionDto>
    suspend fun getSession(sessionId: String): VerificationSessionDto
    suspend fun startSession(request: CreateVerificationSessionRequest): VerificationSessionDto
}
