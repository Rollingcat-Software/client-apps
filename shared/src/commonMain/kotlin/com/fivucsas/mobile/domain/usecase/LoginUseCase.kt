package com.fivucsas.mobile.domain.usecase

import com.fivucsas.mobile.domain.model.AuthToken
import com.fivucsas.mobile.domain.model.User
import com.fivucsas.mobile.domain.repository.AuthRepository
import com.fivucsas.mobile.domain.validation.EmailValidator
import com.fivucsas.mobile.domain.validation.ValidationResult

/**
 * Login Use Case
 * Implements business logic for user authentication
 * Follows Single Responsibility Principle
 */
class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Pair<User, AuthToken>> {
        // Validate email
        val emailValidation = EmailValidator.validate(email)
        if (emailValidation is ValidationResult.Invalid) {
            return Result.failure(Exception(emailValidation.error.message))
        }

        // Validate password not empty
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        // Delegate to repository
        return authRepository.login(email, password)
    }
}
