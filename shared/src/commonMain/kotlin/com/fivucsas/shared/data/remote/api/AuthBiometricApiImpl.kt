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
import kotlinx.serialization.Serializable

/**
 * Step-up biometric authentication API implementation.
 *
 * Maps to the Identity Core API step-up endpoints:
 * - POST /step-up/register-device  — register a device key
 * - POST /step-up/challenge         — request a challenge nonce
 * - POST /step-up/verify-challenge  — verify signed challenge
 */
class AuthBiometricApiImpl(
    private val client: HttpClient
) : AuthBiometricApi {

    companion object {
        private const val BASE_PATH = "step-up"
    }

    @Serializable
    private data class ChallengeRequestDto(
        val deviceKeyId: String
    )

    override suspend fun registerDevice(request: RegisterBiometricDeviceRequestDto) {
        client.post("$BASE_PATH/register-device") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun createChallenge(deviceKeyId: String): CreateBiometricChallengeResponseDto {
        return client.post("$BASE_PATH/challenge") {
            contentType(ContentType.Application.Json)
            setBody(ChallengeRequestDto(deviceKeyId = deviceKeyId))
        }.body()
    }

    override suspend fun verifySignature(request: VerifyBiometricSignatureRequestDto): VerifyBiometricSignatureResponseDto {
        return client.post("$BASE_PATH/verify-challenge") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
