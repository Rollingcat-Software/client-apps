package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricStepUpErrorDto
import com.fivucsas.shared.data.remote.dto.CreateChallengeResponseDto
import com.fivucsas.shared.data.remote.dto.RegisterDeviceRequestDto
import com.fivucsas.shared.data.remote.dto.RegisterDeviceResponseDto
import com.fivucsas.shared.data.remote.dto.VerifyChallengeRequestDto
import com.fivucsas.shared.data.remote.dto.toChallengeDto
import com.fivucsas.shared.data.remote.dto.toRequestDto
import com.fivucsas.shared.domain.model.BiometricError
import com.fivucsas.shared.domain.model.BiometricStepUpException
import com.fivucsas.shared.domain.model.ChallengeDto
import com.fivucsas.shared.domain.model.PublicKeyJwk
import com.fivucsas.shared.domain.model.StepUpDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class BiometricStepUpApiImpl(
    private val client: HttpClient,
    private val json: Json
) : BiometricStepUpApi {

    companion object {
        private const val REGISTER_ENDPOINT = "auth/biometric/devices"
        private const val CHALLENGE_ENDPOINT = "auth/biometric/challenge"
        private const val VERIFY_ENDPOINT = "auth/biometric/verify"
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
        }
        ensureSuccessOrThrow(response)

        resolveRegisteredDeviceId(response, keyId)
    }

    override suspend fun createChallenge(): ChallengeDto = runStepUpCall("create challenge") {
        val response = client.post(CHALLENGE_ENDPOINT)
        ensureSuccessOrThrow(response)
        resolveChallengeDto(response)
    }

    override suspend fun verifyChallenge(
        challengeId: String,
        keyId: String,
        signatureBase64: String
    ): StepUpDto = runStepUpCall("verify challenge") {
        val response = client.post(VERIFY_ENDPOINT) {
            contentType(ContentType.Application.Json)
            setBody(
                VerifyChallengeRequestDto(
                    challengeId = challengeId,
                    keyId = keyId,
                    signatureBase64 = signatureBase64
                )
            )
        }
        ensureSuccessOrThrow(response)
        resolveStepUpDto(response)
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

    private suspend fun resolveRegisteredDeviceId(response: HttpResponse, fallbackKeyId: String): String {
        // Some backends return 201/204 with empty body for successful registration.
        val bodyText = response.bodyAsText()
        if (bodyText.isBlank()) return fallbackKeyId

        val dtoDeviceId = runCatching {
            json.decodeFromString(RegisterDeviceResponseDto.serializer(), bodyText)
        }.getOrNull()?.let { dto -> dto.deviceId ?: dto.deviceIdSnake }
        if (!dtoDeviceId.isNullOrBlank()) return dtoDeviceId

        val jsonDeviceId = runCatching {
            val root = json.parseToJsonElement(bodyText).jsonObject
            extractDeviceIdFromJson(root)
        }.getOrNull()
        if (!jsonDeviceId.isNullOrBlank()) return jsonDeviceId

        return fallbackKeyId
    }

    private suspend fun ensureSuccessOrThrow(response: HttpResponse) {
        if (response.status.isSuccess()) return
        val body = response.bodyAsText()
        throw mapHttpException(
            status = response.status,
            body = body,
            cause = IllegalStateException("HTTP ${response.status.value}")
        )
    }

    private fun extractDeviceIdFromJson(root: JsonObject): String? {
        val topLevelId = root["device_id"]?.jsonPrimitive?.contentOrNull
            ?: root["deviceId"]?.jsonPrimitive?.contentOrNull
            ?: root["id"]?.jsonPrimitive?.contentOrNull
            ?: root["key_id"]?.jsonPrimitive?.contentOrNull
            ?: root["keyId"]?.jsonPrimitive?.contentOrNull
        if (!topLevelId.isNullOrBlank()) return topLevelId

        val dataNode = root["data"]?.jsonObject ?: return null
        return dataNode["device_id"]?.jsonPrimitive?.contentOrNull
            ?: dataNode["deviceId"]?.jsonPrimitive?.contentOrNull
            ?: dataNode["id"]?.jsonPrimitive?.contentOrNull
    }

    private suspend fun resolveChallengeDto(response: HttpResponse): ChallengeDto {
        val bodyText = response.bodyAsText()
        if (bodyText.isBlank()) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed challenge response."),
                message = "Malformed challenge response: empty body."
            )
        }

        val dtoChallenge = runCatching {
            json.decodeFromString(CreateChallengeResponseDto.serializer(), bodyText).toChallengeDto()
        }.getOrNull()
        if (dtoChallenge != null) return dtoChallenge

        val root = runCatching { json.parseToJsonElement(bodyText).jsonObject }
            .getOrElse {
                throw BiometricStepUpException(
                    error = BiometricError.Unknown("Malformed challenge response."),
                    message = "Malformed challenge response: invalid JSON. Body=${previewBody(bodyText)}"
                )
            }

        val container = root["data"]?.jsonObject ?: root["challenge"]?.jsonObject ?: root
        val challengeId = container["challenge_id"]?.jsonPrimitive?.contentOrNull
            ?: container["challengeId"]?.jsonPrimitive?.contentOrNull
            ?: container["id"]?.jsonPrimitive?.contentOrNull
            ?: root["challenge_id"]?.jsonPrimitive?.contentOrNull
            ?: root["challengeId"]?.jsonPrimitive?.contentOrNull
            ?: root["id"]?.jsonPrimitive?.contentOrNull
        val nonceBase64 = container["nonce_base64"]?.jsonPrimitive?.contentOrNull
            ?: container["nonceBase64"]?.jsonPrimitive?.contentOrNull
            ?: container["nonce_b64"]?.jsonPrimitive?.contentOrNull
            ?: container["nonceB64"]?.jsonPrimitive?.contentOrNull
            ?: container["nonce"]?.jsonPrimitive?.contentOrNull
            ?: root["nonce_base64"]?.jsonPrimitive?.contentOrNull
            ?: root["nonceBase64"]?.jsonPrimitive?.contentOrNull
            ?: root["nonce_b64"]?.jsonPrimitive?.contentOrNull
            ?: root["nonceB64"]?.jsonPrimitive?.contentOrNull
            ?: root["nonce"]?.jsonPrimitive?.contentOrNull
        val expiresAt = resolveChallengeExpiry(container, root)

        if (challengeId.isNullOrBlank()) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed challenge response."),
                message = "Malformed challenge response: missing challenge id. Body=${previewBody(bodyText)}"
            )
        }
        if (nonceBase64.isNullOrBlank()) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed challenge response."),
                message = "Malformed challenge response: missing challenge nonce. Body=${previewBody(bodyText)}"
            )
        }
        if (expiresAt == null) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed challenge response."),
                message = "Malformed challenge response: missing challenge expiry. Body=${previewBody(bodyText)}"
            )
        }

        return ChallengeDto(
            challengeId = challengeId,
            nonceBase64 = nonceBase64,
            expiresAt = expiresAt
        )
    }

    private fun resolveChallengeExpiry(container: JsonObject, root: JsonObject): Instant? {
        val absoluteRaw = container["expires_at"]?.jsonPrimitive?.contentOrNull
            ?: container["expiresAt"]?.jsonPrimitive?.contentOrNull
            ?: root["expires_at"]?.jsonPrimitive?.contentOrNull
            ?: root["expiresAt"]?.jsonPrimitive?.contentOrNull
        if (!absoluteRaw.isNullOrBlank()) {
            return runCatching { Instant.parse(absoluteRaw) }.getOrNull()
        }

        val ttlSeconds = container["expires_in"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: container["expiresIn"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: container["ttl"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expires_in"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expiresIn"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["ttl"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
        if (ttlSeconds != null) {
            return Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds() + ttlSeconds * 1000)
        }

        val ttlMillis = container["expires_in_ms"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: container["expiresInMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expires_in_ms"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expiresInMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
        return ttlMillis?.let { Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds() + it) }
    }

    private suspend fun resolveStepUpDto(response: HttpResponse): StepUpDto {
        val bodyText = response.bodyAsText()
        if (bodyText.isBlank()) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed step-up response."),
                message = "Malformed step-up response: empty body."
            )
        }

        val root = runCatching { json.parseToJsonElement(bodyText).jsonObject }
            .getOrElse {
                throw BiometricStepUpException(
                    error = BiometricError.Unknown("Malformed step-up response."),
                    message = "Malformed step-up response: invalid JSON. Body=${previewBody(bodyText)}"
                )
            }

        val container = root["data"]?.jsonObject ?: root["stepUp"]?.jsonObject ?: root["step_up"]?.jsonObject ?: root
        val token = container["stepUpToken"]?.jsonPrimitive?.contentOrNull
            ?: container["step_up_token"]?.jsonPrimitive?.contentOrNull
            ?: container["token"]?.jsonPrimitive?.contentOrNull
            ?: root["stepUpToken"]?.jsonPrimitive?.contentOrNull
            ?: root["step_up_token"]?.jsonPrimitive?.contentOrNull
            ?: root["token"]?.jsonPrimitive?.contentOrNull
        val expiresAt = resolveStepUpExpiry(container, root)

        if (token.isNullOrBlank()) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed step-up response."),
                message = "Missing step-up token in response. Body=${previewBody(bodyText)}"
            )
        }
        if (expiresAt == null) {
            throw BiometricStepUpException(
                error = BiometricError.Unknown("Malformed step-up response."),
                message = "Missing step-up expiry in response. Body=${previewBody(bodyText)}"
            )
        }

        return StepUpDto(stepUpToken = token, expiresAt = expiresAt)
    }

    private fun resolveStepUpExpiry(container: JsonObject, root: JsonObject): Instant? {
        val absoluteRaw = container["expiresAt"]?.jsonPrimitive?.contentOrNull
            ?: container["expires_at"]?.jsonPrimitive?.contentOrNull
            ?: root["expiresAt"]?.jsonPrimitive?.contentOrNull
            ?: root["expires_at"]?.jsonPrimitive?.contentOrNull
        if (!absoluteRaw.isNullOrBlank()) {
            return runCatching { Instant.parse(absoluteRaw) }.getOrNull()
        }

        val ttlSeconds = container["expiresIn"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: container["expires_in"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: container["ttl"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expiresIn"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expires_in"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["ttl"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
        if (ttlSeconds != null) {
            return Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds() + ttlSeconds * 1000)
        }

        val ttlMillis = container["expiresInMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: container["expires_in_ms"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expiresInMs"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
            ?: root["expires_in_ms"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
        return ttlMillis?.let { Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds() + it) }
    }

    private fun previewBody(body: String, max: Int = 400): String {
        val singleLine = body.replace("\n", " ").replace("\r", " ").trim()
        return if (singleLine.length <= max) singleLine else singleLine.take(max) + "..."
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
