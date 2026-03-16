package com.fivucsas.shared.domain.usecase.invite

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.Invite
import com.fivucsas.shared.domain.repository.InviteRepository
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules

/**
 * Use case for creating and sending a new invitation.
 *
 * Business logic:
 * 1. Validate email format
 * 2. Validate role is not blank
 * 3. Delegate to repository
 */
class CreateInviteUseCase(
    private val inviteRepository: InviteRepository
) {
    suspend operator fun invoke(
        email: String,
        role: String,
        tenantId: String? = null
    ): Result<Invite> {
        val emailValidation = ValidationRules.validateEmail(email)
        if (emailValidation is ValidationResult.Error) {
            return Result.failure(ValidationException(emailValidation.message))
        }

        if (role.isBlank()) {
            return Result.failure(ValidationException("Role is required"))
        }

        return inviteRepository.createInvite(
            email = email.trim(),
            role = role.trim(),
            tenantId = tenantId
        )
    }
}
