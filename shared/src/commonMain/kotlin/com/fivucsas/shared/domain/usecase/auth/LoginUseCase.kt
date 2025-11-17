package com.fivucsas.shared.domain.usecase.auth

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.validation.ValidationRules
import com.fivucsas.shared.domain.validation.ValidationResult

/**
 * Login Use Case
 * Implements business logic for user authentication
 * Follows Single Responsibility Principle
 */
class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<User> {
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
