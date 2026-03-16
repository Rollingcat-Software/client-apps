package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.ChangePasswordRequestDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import com.fivucsas.shared.data.remote.dto.RegisterRequestDto

/**
 * Auth API interface
 *
 * Defines contract for authentication service.
 *
 * Base URL: http://localhost:8080/api/v1/
 *
 * Endpoints:
 * - POST /auth/login    → login()
 * - POST /auth/register → register()
 * - POST /auth/logout   → logout()
 * - POST /auth/refresh  → refreshToken()
 */
interface AuthApi {

    /**
     * Login
     * POST /auth/login
     */
    suspend fun login(request: LoginRequestDto): AuthResponseDto

    /**
     * Register new user
     * POST /auth/register
     */
    suspend fun register(request: RegisterRequestDto): AuthResponseDto

    /**
     * Logout
     * POST /auth/logout
     */
    suspend fun logout()

    /**
     * Refresh token
     * POST /auth/refresh
     */
    suspend fun refreshToken(refreshToken: String): AuthResponseDto

    /**
     * Change password
     * POST /auth/change-password
     */
    suspend fun changePassword(request: ChangePasswordRequestDto)
}
