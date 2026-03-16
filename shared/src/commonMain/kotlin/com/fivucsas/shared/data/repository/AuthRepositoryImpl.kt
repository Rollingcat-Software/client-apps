package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.local.StepUpTokenManager
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.data.remote.api.AuthApi
import com.fivucsas.shared.data.remote.dto.ChangePasswordRequestDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import com.fivucsas.shared.data.remote.dto.RegisterRequestDto
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens

/**
 * Real implementation of AuthRepository
 *
 * Connects to Identity Core API via AuthApi.
 * Manages tokens via TokenManager.
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val stepUpTokenManager: StepUpTokenManager? = null
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthTokens> {
        return try {
            val request = LoginRequestDto(email = email, password = password)
            val response = authApi.login(request)
            val tokens = response.toModel()

            // Store tokens in TokenManager
            tokenManager.saveTokens(tokens)

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens> {
        return try {
            val request = RegisterRequestDto(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
            val response = authApi.register(request)
            val tokens = response.toModel()

            // Store tokens in TokenManager
            tokenManager.saveTokens(tokens)

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            authApi.logout()

            // Clear all sensitive data
            tokenManager.clearTokens()
            stepUpTokenManager?.clear()

            Result.success(Unit)
        } catch (e: Exception) {
            // Clear sensitive data even if API call fails
            tokenManager.clearTokens()
            stepUpTokenManager?.clear()
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return try {
            val response = authApi.refreshToken(refreshToken)
            val tokens = response.toModel()

            // Update tokens in TokenManager
            tokenManager.updateTokens(tokens)

            Result.success(tokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val request = ChangePasswordRequestDto(
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            authApi.changePassword(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    override suspend fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }
}
