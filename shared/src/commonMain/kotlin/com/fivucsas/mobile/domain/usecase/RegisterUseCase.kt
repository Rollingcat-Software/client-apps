package com.fivucsas.mobile.domain.usecase

import com.fivucsas.mobile.domain.model.AuthToken
import com.fivucsas.mobile.domain.model.User
import com.fivucsas.mobile.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Pair<User, AuthToken>> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        if (password.length < 8) {
            return Result.failure(Exception("Password must be at least 8 characters"))
        }
        if (firstName.isBlank()) {
            return Result.failure(Exception("First name cannot be empty"))
        }
        if (lastName.isBlank()) {
            return Result.failure(Exception("Last name cannot be empty"))
        }

        return authRepository.register(email, password, firstName, lastName)
    }
}
