package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.ApproveLoginApi
import com.fivucsas.shared.data.remote.dto.toDomain
import com.fivucsas.shared.domain.model.PendingApproveLogin
import com.fivucsas.shared.domain.repository.ApproveLoginRepository
import com.fivucsas.shared.presentation.state.ApprovalDecision

class ApproveLoginRepositoryImpl(
    private val api: ApproveLoginApi
) : ApproveLoginRepository {

    override suspend fun listPending(): Result<List<PendingApproveLogin>> = runCatching {
        api.listPending().map { it.toDomain() }
    }

    override suspend fun submitDecision(
        sessionId: String,
        decision: ApprovalDecision,
        matchNumber: String
    ): Result<Unit> = runCatching {
        val decisionParam = when (decision) {
            ApprovalDecision.ALLOW -> "allow"
            ApprovalDecision.DENY -> "deny"
        }
        // matchNumber is only meaningful on allow, but always sending it is
        // harmless (the backend ignores it on deny) and keeps the call simple.
        api.decide(sessionId, decisionParam, matchNumber)
    }
}
