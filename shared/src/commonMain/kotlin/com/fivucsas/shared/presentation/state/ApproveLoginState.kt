package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.PendingApproveLogin

/**
 * State of the approver's "Login requests" screen for the number-matching
 * approve-login flow.
 *
 * The screen polls `GET /api/v1/auth/approve-login/pending` while [isRefreshing]
 * and shows the [pending] list with each request's two-digit match number; the
 * approver taps Allow (echoing the number) or Deny. A per-session [inFlight]
 * marks the request currently being decided so its row can show a spinner.
 *
 * Reuses {@link ApprovalDecision} from the NFC push-approval flow.
 */
data class ApproveLoginUiState(
    val pending: List<PendingApproveLogin> = emptyList(),
    val isRefreshing: Boolean = false,
    /** sessionId currently being allowed/denied, or null. */
    val inFlightSessionId: String? = null,
    /** Non-null after a decision resolves, for a transient toast/snackbar. */
    val lastDecision: DecisionResult? = null,
    val errorMessage: String? = null
) {
    data class DecisionResult(
        val sessionId: String,
        val decision: ApprovalDecision
    )
}
