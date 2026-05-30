package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.PendingApproveLoginDto

/**
 * Approver side of the no-Firebase number-matching approve-login flow.
 *
 * Backend contract (authenticated approver):
 *   GET  /api/v1/auth/approve-login/pending
 *   POST /api/v1/auth/approve-login/session/{sessionId}/decide  { decision, matchNumber }
 *
 * The initiator side (POST /session, GET /session/{id} poll) lives on the
 * PC/web client, not here.
 */
interface ApproveLoginApi {
    suspend fun listPending(): List<PendingApproveLoginDto>
    suspend fun decide(sessionId: String, decision: String, matchNumber: String?)
}
