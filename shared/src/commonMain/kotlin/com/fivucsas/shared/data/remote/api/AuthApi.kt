package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto

/**
 * Auth API interface
 * 
 * Defines contract for authentication service.
 * TODO: Implement with Ktor client (Week 2, Day 6)
 * 
 * Base URL: http://localhost:8080/api/v1/
 * 
 * Endpoints:
 * - POST /auth/login    → login()
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
     * Logout
     * POST /auth/logout
     */
    suspend fun logout()
    
    /**
     * Refresh token
     * POST /auth/refresh
     */
    suspend fun refreshToken(refreshToken: String): AuthResponseDto
}
