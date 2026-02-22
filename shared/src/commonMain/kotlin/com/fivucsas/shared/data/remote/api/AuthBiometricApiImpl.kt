package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.CreateBiometricChallengeResponseDto
import com.fivucsas.shared.data.remote.dto.RegisterBiometricDeviceRequestDto
import com.fivucsas.shared.data.remote.dto.VerifyBiometricSignatureRequestDto
import com.fivucsas.shared.data.remote.dto.VerifyBiometricSignatureResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthBiometricApiImpl(
    private val client: HttpClient
) : AuthBiometricApi {

    companion object {
        private const val BASE_PATH = "auth/biometric"
    }

    override suspend fun registerDevice(request: RegisterBiometricDeviceRequestDto) {
        client.post("$BASE_PATH/devices") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun createChallenge(): CreateBiometricChallengeResponseDto {
        return client.post("$BASE_PATH/challenge").body()
    }

    override suspend fun verifySignature(request: VerifyBiometricSignatureRequestDto): VerifyBiometricSignatureResponseDto {
        return client.post("$BASE_PATH/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

