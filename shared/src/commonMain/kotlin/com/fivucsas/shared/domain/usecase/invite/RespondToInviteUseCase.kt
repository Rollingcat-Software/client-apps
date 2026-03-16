package com.fivucsas.shared.domain.usecase.invite

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.ReceivedInvite
import com.fivucsas.shared.domain.repository.InviteRepository

/**
 * Possible responses to a received invitation.
 */
enum class InviteResponse { ACCEPT, DECLINE }

/**
 * Use case for responding to a received invitation (accept or decline).
 *
 * Business logic:
 * 1. Validate invite ID is not blank
 * 2. Route to the correct repository method based on response type
 */
class RespondToInviteUseCase(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(
        inviteId: String,
        response: InviteResponse
    ): Result<ReceivedInvite> {
        if (inviteId.isBlank()) {
            return Result.failure(ValidationException("Invite ID is required"))
        }

        return when (response) {
            InviteResponse.ACCEPT -> inviteRepository.acceptInvite(inviteId)
            InviteResponse.DECLINE -> inviteRepository.declineInvite(inviteId)
        }
    }
}
