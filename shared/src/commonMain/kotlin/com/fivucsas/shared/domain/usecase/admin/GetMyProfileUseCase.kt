package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

class GetMyProfileUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<User> {
        return userRepository.getMyProfile()
    }
}
