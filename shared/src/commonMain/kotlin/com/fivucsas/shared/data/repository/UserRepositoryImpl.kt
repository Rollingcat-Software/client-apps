package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.IdentityApi
import com.fivucsas.shared.data.remote.dto.toDto
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.data.remote.dto.toModels
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.User
import com.fivucsas.shared.domain.repository.UserRepository

/**
 * Real implementation of UserRepository
 *
 * Connects to Identity Core API via IdentityApi.
 * Handles user CRUD operations and statistics.
 */
class UserRepositoryImpl(
    private val identityApi: IdentityApi
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = identityApi.getUsers()
            Result.success(response.toModels())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(id: String): Result<User> {
        return try {
            val response = identityApi.getUserById(id)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(user: User): Result<User> {
        return try {
            val response = identityApi.createUser(user.toDto())
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(id: String, user: User): Result<User> {
        return try {
            val response = identityApi.updateUser(id, user.toDto())
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            identityApi.deleteUser(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val response = identityApi.searchUsers(query)
            Result.success(response.toModels())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStatistics(): Result<Statistics> {
        return try {
            val response = identityApi.getStatistics()
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
