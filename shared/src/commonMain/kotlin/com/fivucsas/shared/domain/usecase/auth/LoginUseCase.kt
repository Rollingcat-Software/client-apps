package com.fivucsas.shared.domain.usecase.auth

import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.LoginResult
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules

/**
 * Login Use Case
 * Implements business logic for user authentication.
 * Returns LoginResult which can be either Authenticated or MfaChallenge.
 * Follows Single Responsibility Principle.
 */
class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<LoginResult> {
        // Validate email
        val emailValidation = ValidationRules.validateEmail(email)
        if (emailValidation is ValidationResult.Error) {
            return Result.failure(Exception(emailValidation.message))
        }

        // Validate password not empty
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        // Delegate to repository
        return authRepository.login(email, password)
    }
}
