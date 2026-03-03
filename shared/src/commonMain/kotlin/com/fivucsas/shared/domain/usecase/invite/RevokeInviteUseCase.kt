package com.fivucsas.shared.domain.usecase.invite

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.repository.InviteRepository

/**
 * Use case for revoking a pending invitation.
 *
 * Business logic:
 * 1. Validate invite ID is not blank
 * 2. Delegate to repository
 */
class RevokeInviteUseCase(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(inviteId: String): Result<Invite> {
        if (inviteId.isBlank()) {
            return Result.failure(ValidationException("Invite ID is required"))
        }

        return inviteRepository.revokeInvite(inviteId)
    }
}
