package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.NfcApprovalApi
import com.fivucsas.shared.domain.repository.NfcApprovalRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision

class NfcApprovalRepositoryImpl(
    private val api: NfcApprovalApi
) : NfcApprovalRepository {

    override suspend fun submitDecision(
        sessionId: String,
        decision: ApprovalDecision
    ): Result<Unit> = runCatching {
        val decisionParam = when (decision) {
            ApprovalDecision.ALLOW -> "allow"
            ApprovalDecision.DENY -> "deny"
        }
        api.decide(sessionId, decisionParam)
    }
}
