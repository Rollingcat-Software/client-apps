package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricDto
import com.fivucsas.shared.data.remote.dto.IdentificationResponseDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

/**
 * Biometric API implementation
 * Connects to Biometric Processor FastAPI service (port 8001)
 *
 * Endpoints:
 * - POST /enroll   - Face enrollment
 * - POST /verify   - Face verification
 * - POST /liveness - Liveness detection
 * - POST /search   - Face search (1:N)
 */
class BiometricApiImpl(
    private val client: HttpClient
) : BiometricApi {

    override suspend fun enrollFace(userId: String, imageData: String): BiometricDto {
        // Biometric processor expects multipart form data or JSON with user_id and image
        return client.post("enroll") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "user_id" to userId,
                    "image_data" to imageData  // Base64 encoded image
                )
            )
        }.body()
    }

    override suspend fun verifyFace(imageData: String): VerificationResponseDto {
        return client.post("verify") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "image_data" to imageData  // Base64 encoded image
                )
            )
        }.body()
    }

    override suspend fun checkLiveness(actions: List<String>): LivenessResponseDto {
        return client.post("liveness") {
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
        return client.post("search") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("user_id" to userId))
        }.body()
    }

    override suspend fun deleteBiometricData(userId: String) {
        // Biometric processor may not have a direct delete endpoint
        // This would typically go through Identity Core API
        client.delete("embeddings/$userId")
    }

    override suspend fun identifyFace(imageData: String): IdentificationResponseDto {
        return client.post("search") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "image_data" to imageData
                )
            )
        }.body()
    }
}
