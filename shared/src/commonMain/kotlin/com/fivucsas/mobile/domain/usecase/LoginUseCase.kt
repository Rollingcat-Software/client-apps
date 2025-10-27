package com.fivucsas.mobile.domain.usecase

import com.fivucsas.mobile.domain.model.AuthToken
import com.fivucsas.mobile.domain.model.User
import com.fivucsas.mobile.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Pair<User, AuthToken>> {
        if (email.isBlank()) {
            return Result.failure(Exception("Email cannot be empty"))
        }
        if (password.isBlank()) {
            return Result.failure(Exception("Password cannot be empty"))
        }

        return authRepository.login(email, password)
    }
}
