package com.fivucsas.shared.test

import com.fivucsas.shared.domain.repository.AuthRepository
import com.fivucsas.shared.domain.repository.AuthTokens

class FakeAuthRepository : AuthRepository {

    var shouldSucceed = true
    var errorMessage = "Test error"
    var loginCalled = false
    var lastLoginEmail: String? = null
    var lastLoginPassword: String? = null
    var mockAccessToken = "fake-access-token"
    var mockRefreshToken = "fake-refresh-token"
    var mockRole = "USER"

    override suspend fun login(email: String, password: String): Result<AuthTokens> {
        loginCalled = true
        lastLoginEmail = email
        lastLoginPassword = password
        return if (shouldSucceed) {
            Result.success(
                AuthTokens(
                    accessToken = mockAccessToken,
                    refreshToken = mockRefreshToken,
                    expiresIn = 3600,
                    role = mockRole
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthTokens> {
        return if (shouldSucceed) {
            Result.success(
                AuthTokens(
                    accessToken = mockAccessToken,
                    refreshToken = mockRefreshToken,
                    expiresIn = 3600,
                    role = mockRole
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun refreshToken(refreshToken: String): Result<AuthTokens> {
        return if (shouldSucceed) {
            Result.success(
                AuthTokens(
                    accessToken = "refreshed-token",
                    refreshToken = refreshToken,
                    expiresIn = 3600,
                    role = mockRole
                )
            )
        } else {
            Result.failure(RuntimeException(errorMessage))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return if (shouldSucceed) Result.success(Unit) else Result.failure(RuntimeException(errorMessage))
    }

    override suspend fun isAuthenticated(): Boolean = shouldSucceed

    override suspend fun getAccessToken(): String? = if (shouldSucceed) mockAccessToken else null
}
