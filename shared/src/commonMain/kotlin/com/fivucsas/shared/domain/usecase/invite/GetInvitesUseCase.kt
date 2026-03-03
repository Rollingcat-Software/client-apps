package com.fivucsas.shared.domain.usecase.invite

import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.repository.InviteRepository

/**
 * Use case for fetching all outbound invitations (admin perspective).
 *
 * Simple pass-through to repository.
 */
class GetInvitesUseCase(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(): Result<List<Invite>> {
        return inviteRepository.getInvites()
    }
}
