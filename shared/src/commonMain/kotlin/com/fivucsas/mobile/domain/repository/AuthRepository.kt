package com.fivucsas.mobile.domain.repository

import com.fivucsas.mobile.domain.model.AuthToken
import com.fivucsas.mobile.domain.model.User

interface AuthRepository {
    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<Pair<User, AuthToken>>

    suspend fun login(email: String, password: String): Result<Pair<User, AuthToken>>

    suspend fun logout()

    fun isLoggedIn(): Boolean

    fun getToken(): String?

    fun saveToken(token: String)

    fun clearToken()
}
