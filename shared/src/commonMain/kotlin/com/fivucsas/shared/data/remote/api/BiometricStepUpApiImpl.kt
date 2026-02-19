package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricStepUpErrorDto
import com.fivucsas.shared.data.remote.dto.CreateChallengeResponseDto
import com.fivucsas.shared.data.remote.dto.RegisterDeviceRequestDto
import com.fivucsas.shared.data.remote.dto.RegisterDeviceResponseDto
import com.fivucsas.shared.data.remote.dto.VerifyChallengeRequestDto
import com.fivucsas.shared.data.remote.dto.VerifyChallengeResponseDto
import com.fivucsas.shared.data.remote.dto.toChallengeDto
import com.fivucsas.shared.data.remote.dto.toRequestDto
import com.fivucsas.shared.data.remote.dto.toStepUpDto
import com.fivucsas.shared.domain.model.BiometricError
import com.fivucsas.shared.domain.model.BiometricStepUpException
import com.fivucsas.shared.domain.model.ChallengeDto
import com.fivucsas.shared.domain.model.PublicKeyJwk
import com.fivucsas.shared.domain.model.StepUpDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BiometricStepUpApiImpl(
    private val client: HttpClient,
    private val json: Json
) : BiometricStepUpApi {

    companion object {
        private const val REGISTER_ENDPOINT = "step-up/register-device"
        private const val CHALLENGE_ENDPOINT = "step-up/challenge"
        private const val VERIFY_ENDPOINT = "step-up/verify-challenge"
    }

    override suspend fun registerDevice(
        keyId: String,
        platform: String,
        publicKeyJwk: PublicKeyJwk,
        deviceLabel: String?
    ): String = runStepUpCall("register device") {
        val response = client.post(REGISTER_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterDeviceRequestDto(
                    keyId = keyId,
                    platform = platform,
                    publicKeyJwk = publicKeyJwk.toRequestDto(),
                    deviceLabel = deviceLabel
                )
            )
        }.body<RegisterDeviceResponseDto>()

        response.deviceId ?: response.deviceIdSnake
        ?: throw BiometricStepUpException(
            error = BiometricError.Unknown("Malformed register response."),
            message = "Malformed register response: missing device id."
        )
    }

    override suspend fun createChallenge(): ChallengeDto = runStepUpCall("create challenge") {
        client.post(CHALLENGE_ENDPOINT)
            .body<CreateChallengeResponseDto>()
            .toChallengeDto()
    }

    override suspend fun verifyChallenge(
        challengeId: String,
        keyId: String,
        signatureBase64: String
    ): StepUpDto = runStepUpCall("verify challenge") {
        client.post(VERIFY_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(
                VerifyChallengeRequestDto(
                    challengeId = challengeId,
                    keyId = keyId,
                    signatureBase64 = signatureBase64
                )
            )
        }.body<VerifyChallengeResponseDto>()
            .toStepUpDto()
    }

    private suspend fun <T> runStepUpCall(action: String, call: suspend () -> T): T {
        return try {
            call()
        } catch (e: BiometricStepUpException) {
            throw e
        } catch (e: ClientRequestException) {
            throw mapHttpException(e.response.status, e.response.bodyAsText(), e)
        } catch (e: ServerResponseException) {
            throw mapHttpException(e.response.status, e.response.bodyAsText(), e)
        } catch (e: RedirectResponseException) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Unexpected redirect during biometric step-up."),
                message = "Unexpected redirect during biometric step-up.",
                cause = e
            )
        } catch (e: SerializationException) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed biometric response."),
                message = "Malformed biometric response while trying to $action.",
                cause = e
            )
        } catch (e: IllegalArgumentException) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown(e.message),
                message = e.message ?: "Biometric step-up failed while trying to $action.",
                cause = e
            )
        } catch (e: Exception) {
            val message = e.message ?: "Biometric step-up failed while trying to $action."
            val error = if (message.contains("timeout", ignoreCase = true)) {
                BiometricError.Lockout
            } else {
                BiometricError.Unknown(message)
            }
            throw BiometricStepUpException(error = error, message = message, cause = e)
        }
    }

    private fun mapHttpException(
        status: HttpStatusCode,
        body: String?,
        cause: Throwable
    ): BiometricStepUpException {
        val backendMessage = extractBackendMessage(body)
        val message = backendMessage ?: defaultMessageFor(status)
        val error = when (status) {
            HttpStatusCode.BadRequest,
            HttpStatusCode.UnprocessableEntity -> BiometricError.Failed
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden -> BiometricError.Unknown("Unauthorized biometric request.")
            HttpStatusCode.TooManyRequests -> BiometricError.Lockout
            HttpStatusCode.Conflict -> BiometricError.KeyInvalidated
            else -> BiometricError.Unknown(message)
        }
        return BiometricStepUpException(error = error, message = message, cause = cause)
    }

    private fun extractBackendMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null

        val dtoMessage = runCatching {
            json.decodeFromString(BiometricStepUpErrorDto.serializer(), body)
        }.getOrNull()?.let { dto ->
            dto.message ?: dto.detail ?: dto.error
        }
        if (!dtoMessage.isNullOrBlank()) return dtoMessage

        return runCatching {
            val root = json.parseToJsonElement(body).jsonObject
            root["message"]?.jsonPrimitive?.contentOrNull
                ?: root["detail"]?.jsonPrimitive?.contentOrNull
                ?: root["error"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    }

    private fun defaultMessageFor(status: HttpStatusCode): String = when (status) {
        HttpStatusCode.BadRequest -> "Invalid biometric step-up request."
        HttpStatusCode.Unauthorized -> "Session expired. Please sign in again."
        HttpStatusCode.Forbidden -> "You are not authorized for biometric step-up."
        HttpStatusCode.NotFound -> "Biometric step-up endpoint not found."
        HttpStatusCode.Conflict -> "Biometric key is no longer valid. Register device again."
        HttpStatusCode.TooManyRequests -> "Too many biometric attempts. Try again later."
        HttpStatusCode.UnprocessableEntity -> "Biometric verification failed."
        HttpStatusCode.InternalServerError,
        HttpStatusCode.BadGateway,
        HttpStatusCode.ServiceUnavailable,
        HttpStatusCode.GatewayTimeout -> "Biometric service is temporarily unavailable."
        else -> "Biometric request failed (${status.value})."
    }
}
