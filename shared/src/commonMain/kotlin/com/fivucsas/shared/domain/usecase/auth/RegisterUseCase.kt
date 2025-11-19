package com.fivucsas.shared.domain.usecase.auth

import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules

/**
 * Register Use Case
 * Implements business logic for user registration
 * Follows Single Responsibility Principle
 */
class RegisterUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens> {
        // Validate email
        val emailValidation = ValidationRules.validateEmail(email)
        if (emailValidation is ValidationResult.Error) {
            return Result.failure(Exception(emailValidation.message))
        }

        // Validate password
        val passwordValidation = ValidationRules.validatePassword(password)
        if (passwordValidation is ValidationResult.Error) {
            return Result.failure(Exception(passwordValidation.message))
        }

        // Validate names
        if (firstName.isBlank()) {
            return Result.failure(Exception("First name is required"))
        }
        if (lastName.isBlank()) {
            return Result.failure(Exception("Last name is required"))
        }

        // Delegate to repository
        return authRepository.register(email, password, firstName, lastName)
    }
}
