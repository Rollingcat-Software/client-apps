package com.fivucsas.shared.domain.usecase.invite

import com.fivucsas.shared.domain.model.ReceivedInvite
import com.fivucsas.shared.domain.repository.InviteRepository

/**
 * Use case for fetching all inbound invitations (member perspective).
 *
 * Simple pass-through to repository.
 */
class GetReceivedInvitesUseCase(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(): Result<List<ReceivedInvite>> {
        return inviteRepository.getReceivedInvites()
    }
}
