package com.fivucsas.shared.domain.usecase.admin

import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

/**
 * Use case for searching users
 *
 * Business logic:
 * - If query is blank, return all users
 * - Otherwise, search by name, email, ID number, phone
 * - Results are filtered in repository
 */
class SearchUsersUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(query: String): Result<List<User>> {
        // Trim query
        val trimmedQuery = query.trim()

        // If empty, return all users
        if (trimmedQuery.isBlank()) {
            return userRepository.getUsers()
        }

        // Search
        return userRepository.searchUsers(trimmedQuery)
    }
}
