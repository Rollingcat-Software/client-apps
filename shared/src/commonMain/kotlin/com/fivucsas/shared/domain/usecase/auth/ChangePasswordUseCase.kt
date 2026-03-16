package com.fivucsas.shared.domain.usecase.auth

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> {
        if (currentPassword.isBlank()) {
            return Result.failure(ValidationException("Current password is required"))
        }

        val passwordValidation = ValidationRules.validatePassword(newPassword)
        if (passwordValidation is ValidationResult.Error) {
            return Result.failure(ValidationException(passwordValidation.message))
        }

        if (newPassword != confirmPassword) {
            return Result.failure(ValidationException("New passwords do not match"))
        }

        if (currentPassword == newPassword) {
            return Result.failure(ValidationException("New password must be different from current password"))
        }

        return authRepository.changePassword(currentPassword, newPassword)
    }
}
