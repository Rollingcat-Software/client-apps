package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.local.BiometricStepUpLocalStore
import com.fivucsas.shared.data.remote.dto.BiometricDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto
import com.fivucsas.shared.domain.model.BiometricError
import com.fivucsas.shared.domain.model.BiometricStepUpException
import com.fivucsas.shared.domain.usecase.auth.BiometricStepUpUseCase
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Biometric API implementation
 * Uses:
 * - Identity API client for protected enroll/verify operations
 * - Biometric Processor client for liveness/search operations
 *
 * Endpoints:
 * - POST /biometric/enroll/{userId} - Face enrollment (identity-core, requires step-up token)
 * - POST /biometric/verify/{userId} - Face verification (identity-core, requires step-up token)
 * - POST /liveness - Liveness detection
 * - POST /search   - Face search (1:N)
 */
class BiometricApiImpl(
    private val identityClient: HttpClient,
    private val biometricClient: HttpClient,
    private val stepUpLocalStore: BiometricStepUpLocalStore,
    private val stepUpUseCase: BiometricStepUpUseCase
) : BiometricApi {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun enrollFace(userId: String, imageData: String): BiometricDto {
        val imageBytes = Base64.decode(imageData)

        executeProtectedIdentityCall(
            path = "biometric/enroll/$userId",
            imageBytes = imageBytes,
            imageFileName = "enroll.jpg"
        )

        // Enrollment flow only needs success/failure in current domain layer.
        return BiometricDto(
            id = userId,
            userId = userId,
            faceEmbedding = emptyList(),
            enrollmentDate = Clock.System.now().toString()
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun verifyFace(userId: String, imageData: String): VerificationResponseDto {
        val imageBytes = Base64.decode(imageData)

        val response = executeProtectedIdentityCall(
            path = "biometric/verify/$userId",
            imageBytes = imageBytes,
            imageFileName = "verify.jpg"
        )

        return VerificationResponseDto(
            isVerified = response.verified,
            userId = if (response.verified) userId else null,
            confidence = response.confidence,
            message = response.message
        )
    }

    override suspend fun checkLiveness(actions: List<String>): LivenessResponseDto {
        return biometricClient.post("liveness") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "actions" to actions,
                    "challenge_type" to "facial_action"
                )
            )
        }.body()
    }

    override suspend fun getBiometricData(userId: String): BiometricDto {
        // Search for user's biometric data
        return biometricClient.post("search") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("user_id" to userId))
        }.body()
    }

    override suspend fun deleteBiometricData(userId: String) {
        // Biometric processor may not have a direct delete endpoint
        // This would typically go through Identity Core API
        biometricClient.delete("embeddings/$userId")
    }

    private suspend fun executeProtectedIdentityCall(
        path: String,
        imageBytes: ByteArray,
        imageFileName: String
    ): IdentityBiometricVerificationResponseDto {
        val response = callWithStepUpRetry(reason = STEP_UP_REASON) { token ->
            postProtectedImage(path, imageBytes, imageFileName, token)
        }

        if (response.status.isSuccess()) {
            return response.body()
        }

        val body = response.bodyAsText()
        throw IllegalStateException(identityErrorMessage(response.status, body))
    }

    private suspend fun postProtectedImage(
        path: String,
        imageBytes: ByteArray,
        imageFileName: String,
        stepUpToken: String
    ) = identityClient.submitFormWithBinaryData(path, formData {
        append(
            key = "image",
            value = imageBytes,
            headers = Headers.build {
                append(HttpHeaders.ContentType, "image/jpeg")
                append(HttpHeaders.ContentDisposition, "filename=\"$imageFileName\"")
            }
        )
    }) { header(STEP_UP_HEADER, stepUpToken) }

    private suspend fun callWithStepUpRetry(
        reason: String,
        block: suspend (stepUpToken: String) -> HttpResponse
    ): HttpResponse {
        val token = activeStepUpTokenOrNull() ?: obtainStepUpToken(reason)
        var response = block(token)
        if (response.status != HttpStatusCode.Forbidden) return response

        val firstBody = response.bodyAsText()
        if (!isStepUpRequired(firstBody)) return response

        val refreshedToken = obtainStepUpToken(reason)
        response = block(refreshedToken)
        return response
    }

    private suspend fun obtainStepUpToken(reason: String): String {
        if (!stepUpUseCase.isDeviceRegistered()) {
            stepUpUseCase.ensureRegisteredDevice(deviceLabel = null)
        }
        return try {
            stepUpUseCase.stepUp(reason = reason).stepUpToken
        } catch (e: BiometricStepUpException) {
            if (e.error != BiometricError.KeyInvalidated) {
                throw IllegalStateException(stepUpErrorMessage(e.error), e)
            }
            // Key was invalidated (e.g., enrollment changed). Re-register and retry once.
            stepUpUseCase.ensureRegisteredDevice(deviceLabel = null)
            runCatching {
                stepUpUseCase.stepUp(reason = reason).stepUpToken
            }.getOrElse { retryError ->
                if (retryError is BiometricStepUpException) {
                    throw IllegalStateException(stepUpErrorMessage(retryError.error), retryError)
                }
                throw retryError
            }
        }
    }

    private fun activeStepUpTokenOrNull(): String? {
        val stepUp = stepUpLocalStore.getStepUpTokenInMemory() ?: return null
        if (stepUp.expiresAt <= Clock.System.now()) return null
        return stepUp.stepUpToken
    }

    private fun isStepUpRequired(body: String): Boolean {
        if (body.isBlank()) return false
        val code = runCatching {
            Json.parseToJsonElement(body).jsonObject["code"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
        if (code != null) return code == STEP_UP_REQUIRED_CODE
        return body.contains(STEP_UP_REQUIRED_CODE, ignoreCase = true)
    }

    private fun identityErrorMessage(status: HttpStatusCode, body: String): String {
        if (body.isBlank()) return "Biometric operation failed (${status.value})."
        return body
    }

    private fun stepUpErrorMessage(error: BiometricError): String {
        return when (error) {
            BiometricError.Canceled -> "Fingerprint confirmation was canceled."
            BiometricError.Lockout -> "Biometric is locked. Try again later."
            BiometricError.NotEnrolled -> "No biometric enrolled on this device."
            BiometricError.NoHardware -> "Biometric hardware is unavailable."
            BiometricError.KeyInvalidated -> "Biometric key was invalidated. Please try again."
            BiometricError.Failed -> "Fingerprint confirmation failed."
            is BiometricError.Unknown -> error.message ?: "Fingerprint confirmation failed."
        }
    }

    private data class IdentityBiometricVerificationResponseDto(
        val verified: Boolean,
        val confidence: Float = 0f,
        val message: String = "Biometric operation completed."
    )

    private companion object {
        const val STEP_UP_HEADER = "X-Step-Up-Token"
        const val STEP_UP_REQUIRED_CODE = "STEP_UP_REQUIRED"
        const val STEP_UP_REASON = "Confirm with fingerprint"
    }
}
