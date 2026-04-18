package com.fivucsas.shared.presentation.state

/**
 * Represents the state of an NFC push-approval flow driven by a
 * `fivucsas://nfc-session/{sessionId}` deep-link or an FCM notification tap.
 *
 * See docs/plans/NFC_PUSH_APPROVAL_PROTOCOL.md §4 for the protocol.
 */
sealed class NfcApprovalUiState {
    object Idle : NfcApprovalUiState()
    data class AwaitingDecision(val sessionId: String) : NfcApprovalUiState()
    data class Submitting(val sessionId: String, val decision: ApprovalDecision) : NfcApprovalUiState()
    data class Approved(val sessionId: String) : NfcApprovalUiState()
    data class Denied(val sessionId: String) : NfcApprovalUiState()
    data class Error(val sessionId: String?, val message: String) : NfcApprovalUiState()
}

enum class ApprovalDecision { ALLOW, DENY }
