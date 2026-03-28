package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.repository.UserRepository

open class CheckSystemHealthUseCase(
    private val userRepository: UserRepository
) {
    open suspend operator fun invoke(): Result<Boolean> {
        return userRepository.healthCheck()
    }
}
