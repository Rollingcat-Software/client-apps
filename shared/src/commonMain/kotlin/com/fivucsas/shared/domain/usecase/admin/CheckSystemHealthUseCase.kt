package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.repository.UserRepository

class CheckSystemHealthUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return userRepository.healthCheck()
    }
}
