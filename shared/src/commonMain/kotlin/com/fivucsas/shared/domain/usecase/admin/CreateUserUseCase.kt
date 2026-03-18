package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository
import com.fivucsas.shared.domain.validation.ValidationResult
import com.fivucsas.shared.domain.validation.ValidationRules

class CreateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<User> {
        if (user.name.isBlank()) {
            return Result.failure(ValidationException("Full name is required"))
        }

        val emailValidation = ValidationRules.validateEmail(user.email)
        if (emailValidation is ValidationResult.Error) {
            return Result.failure(ValidationException(emailValidation.message))
        }

        if (user.idNumber.isBlank()) {
            return Result.failure(ValidationException("ID number is required"))
        }

        return userRepository.createUser(user)
    }
}
