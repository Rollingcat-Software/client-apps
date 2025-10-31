package com.fivucsas.mobile.domain.usecase

import com.fivucsas.mobile.domain.model.AuthToken
import com.fivucsas.mobile.domain.model.User
import com.fivucsas.mobile.domain.repository.AuthRepository
import com.fivucsas.mobile.domain.validation.EmailValidator
import com.fivucsas.mobile.domain.validation.NameValidator
import com.fivucsas.mobile.domain.validation.PasswordValidator
import com.fivucsas.mobile.domain.validation.ValidationResult
import com.fivucsas.mobile.domain.validation.combineValidations

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
    ): Result<Pair<User, AuthToken>> {
        // Validate all inputs
        val validations = listOf(
            EmailValidator.validate(email),
            PasswordValidator.validate(password),
            NameValidator.validate(firstName, "First name"),
            NameValidator.validate(lastName, "Last name")
        )

        val combinedValidation = validations.combineValidations()
        if (combinedValidation is ValidationResult.Invalid) {
            return Result.failure(Exception(combinedValidation.error.message))
        }

        // Delegate to repository
        return authRepository.register(email, password, firstName, lastName)
    }
}
