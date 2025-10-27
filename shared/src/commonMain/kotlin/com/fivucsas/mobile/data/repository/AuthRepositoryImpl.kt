package com.fivucsas.mobile.data.repository

import com.fivucsas.mobile.data.local.TokenStorage
import com.fivucsas.mobile.data.remote.ApiClient
import com.fivucsas.mobile.data.remote.LoginRequest
import com.fivucsas.mobile.data.remote.RegisterRequest
import com.fivucsas.mobile.domain.model.AuthToken
import com.fivucsas.mobile.domain.model.User
import com.fivucsas.mobile.domain.repository.AuthRepository
import kotlinx.datetime.Instant

class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Pair<User, AuthToken>> {
        return try {
            val response = apiClient.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )
            )

            tokenStorage.saveToken(response.accessToken)

            val user = User(
                id = response.user.id,
                email = response.user.email,
                firstName = response.user.firstName,
                lastName = response.user.lastName,
                isBiometricEnrolled = response.user.isBiometricEnrolled,
                createdAt = Instant.parse(response.user.createdAt)
            )

            val token = AuthToken(
                accessToken = response.accessToken,
                tokenType = response.tokenType
            )

            Result.success(Pair(user, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<Pair<User, AuthToken>> {
        return try {
            val response = apiClient.login(
                LoginRequest(email, password)
            )

            tokenStorage.saveToken(response.accessToken)

            val user = User(
                id = response.user.id,
                email = response.user.email,
                firstName = response.user.firstName,
                lastName = response.user.lastName,
                isBiometricEnrolled = response.user.isBiometricEnrolled,
                createdAt = Instant.parse(response.user.createdAt)
            )

            val token = AuthToken(
                accessToken = response.accessToken,
                tokenType = response.tokenType
            )

            Result.success(Pair(user, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenStorage.clearToken()
    }

    override fun isLoggedIn(): Boolean {
        return tokenStorage.getToken() != null
    }

    override fun getToken(): String? {
        return tokenStorage.getToken()
    }

    override fun saveToken(token: String) {
        tokenStorage.saveToken(token)
    }

    override fun clearToken() {
        tokenStorage.clearToken()
    }
}
