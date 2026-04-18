package com.fivucsas.shared.data.remote.api

/**
 * Submits the user's Allow / Deny decision for a pending NFC approval session.
 *
 * Backend contract: `POST /api/v1/auth/approval/{sessionId}/decide?decision=allow|deny`
 * See docs/plans/NFC_PUSH_APPROVAL_PROTOCOL.md.
 */
interface NfcApprovalApi {
    suspend fun decide(sessionId: String, decision: String)
}
