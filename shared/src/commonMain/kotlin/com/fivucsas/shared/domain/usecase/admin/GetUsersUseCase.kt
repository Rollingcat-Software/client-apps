package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

/**
 * Use case for getting all users
 *
 * Simple pass-through to repository.
 * Can add business logic here if needed (e.g., filtering, sorting).
 */
class GetUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        return userRepository.getUsers()
    }
}
