package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.AuthResponseDto
import com.fivucsas.shared.data.remote.dto.ChangePasswordRequestDto
import com.fivucsas.shared.data.remote.dto.LoginRequestDto
import com.fivucsas.shared.data.remote.dto.MfaQrGenerateRequest
import com.fivucsas.shared.data.remote.dto.MfaQrTokenResponse
import com.fivucsas.shared.data.remote.dto.MfaSendOtpRequest
import com.fivucsas.shared.data.remote.dto.MfaStepRequest
import com.fivucsas.shared.data.remote.dto.MfaStepResponse
import com.fivucsas.shared.data.remote.dto.RefreshTokenRequestDto
import com.fivucsas.shared.data.remote.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Auth API implementation
 * Handles authentication endpoints including N-step MFA flow.
 */
class AuthApiImpl(
    private val client: HttpClient
) : AuthApi {

    companion object {
        private const val BASE_PATH = "auth"
    }

    override suspend fun login(request: LoginRequestDto): AuthResponseDto {
        val response = client.post("$BASE_PATH/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }

    override suspend fun register(request: RegisterRequestDto): AuthResponseDto {
        val response = client.post("$BASE_PATH/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }

    override suspend fun logout() {
        client.post("$BASE_PATH/logout")
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        return client.post("$BASE_PATH/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }.body()
    }

    override suspend fun changePassword(request: ChangePasswordRequestDto) {
        client.post("$BASE_PATH/change-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun verifyMfaStep(request: MfaStepRequest): MfaStepResponse {
        val response = client.post("$BASE_PATH/mfa/step") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }

    override suspend fun sendMfaOtp(request: MfaSendOtpRequest) {
        val response = client.post("$BASE_PATH/mfa/send-otp") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
    }

    override suspend fun generateMfaQr(sessionToken: String): MfaQrTokenResponse {
        val response = client.post("$BASE_PATH/mfa/qr-generate") {
            contentType(ContentType.Application.Json)
            setBody(MfaQrGenerateRequest(sessionToken = sessionToken))
        }
        if (!response.status.isSuccess()) {
            throw Exception("${response.status.value} ${response.bodyAsText()}")
        }
        return response.body()
    }
}
