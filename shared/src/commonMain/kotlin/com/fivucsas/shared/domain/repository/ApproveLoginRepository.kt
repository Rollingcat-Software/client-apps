package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.PendingApproveLogin
import com.fivucsas.shared.presentation.state.ApprovalDecision

/**
 * Approver-side repository for the number-matching approve-login flow.
 *
 * - [listPending] backs the "Login requests" screen
 *   (`GET /api/v1/auth/approve-login/pending`).
 * - [submitDecision] allows/denies a request
 *   (`POST .../session/{sessionId}/decide`). On allow the [matchNumber] the
 *   approver tapped is echoed for the backend's number-matching check.
 */
interface ApproveLoginRepository {
    suspend fun listPending(): Result<List<PendingApproveLogin>>
    suspend fun submitDecision(
        sessionId: String,
        decision: ApprovalDecision,
        matchNumber: String
    ): Result<Unit>
}
