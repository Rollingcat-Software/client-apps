package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

open class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    open suspend operator fun invoke(id: String, user: User): Result<User> {
        return userRepository.updateUser(id, user)
    }
}
