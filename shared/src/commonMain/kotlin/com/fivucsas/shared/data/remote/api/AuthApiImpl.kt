package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Auth API implementation
 * Handles authentication endpoints
 */
class AuthApiImpl(
    private val client: HttpClient
) : AuthApi {
    
    companion object {
        private const val BASE_PATH = "auth"
    }
    
    override suspend fun login(request: LoginRequestDto): AuthResponseDto {
        return client.post("$BASE_PATH/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    override suspend fun logout() {
        client.post("$BASE_PATH/logout")
    }
    
    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        return client.post("$BASE_PATH/refresh") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("refreshToken" to refreshToken))
        }.body()
    }
}
