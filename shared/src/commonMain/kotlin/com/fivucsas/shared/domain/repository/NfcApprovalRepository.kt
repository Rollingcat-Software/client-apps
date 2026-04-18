package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.presentation.state.ApprovalDecision

/**
 * Repository abstraction over `POST /api/v1/auth/approval/{sessionId}/decide`.
 *
 * Implementations call the Identity Core endpoint that marks the NFC approval
 * session as APPROVED or REJECTED. See NFC_PUSH_APPROVAL_PROTOCOL.md §6.
 */
interface NfcApprovalRepository {
    suspend fun submitDecision(sessionId: String, decision: ApprovalDecision): Result<Unit>
}
