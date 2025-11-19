package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Biometric API implementation
 * Handles face enrollment, verification, and liveness detection
 */
class BiometricApiImpl(
    private val client: HttpClient
) : BiometricApi {

    companion object {
        private const val BASE_PATH = "biometric"
    }

    override suspend fun enrollFace(userId: String, imageData: String): BiometricDto {
        return client.post("$BASE_PATH/enroll") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "userId" to userId,
                    "imageData" to imageData
                )
            )
        }.body()
    }

    override suspend fun verifyFace(imageData: String): VerificationResponseDto {
        return client.post("$BASE_PATH/verify") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("imageData" to imageData))
        }.body()
    }

    override suspend fun checkLiveness(actions: List<String>): LivenessResponseDto {
        return client.post("$BASE_PATH/liveness") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("actions" to actions))
        }.body()
    }

    override suspend fun getBiometricData(userId: String): BiometricDto {
        return client.get("$BASE_PATH/$userId").body()
    }

    override suspend fun deleteBiometricData(userId: String) {
        client.delete("$BASE_PATH/$userId")
    }
}
