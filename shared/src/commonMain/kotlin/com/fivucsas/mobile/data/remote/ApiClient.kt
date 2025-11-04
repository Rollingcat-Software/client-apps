package com.fivucsas.mobile.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiClient(
    private val baseUrl: String = "http://localhost:8080/api/v1", // Desktop/iOS: localhost, Android: 10.0.2.2
    private val tokenProvider: () -> String?
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }

        defaultRequest {
            url(baseUrl)
            contentType(ContentType.Application.Json)

            // Add auth token if available
            tokenProvider()?.let { token ->
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return httpClient.post("/auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return httpClient.post("/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun enrollFace(userId: String, imageBytes: ByteArray): BiometricVerificationResponse {
        return httpClient.submitFormWithBinaryData(
            url = "/biometric/enroll/$userId",
            formData = formData {
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=face.jpg")
                })
            }
        ).body()
    }

    suspend fun verifyFace(userId: String, imageBytes: ByteArray): BiometricVerificationResponse {
        return httpClient.submitFormWithBinaryData(
            url = "/biometric/verify/$userId",
            formData = formData {
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=face.jpg")
                })
            }
        ).body()
    }

    fun close() {
        httpClient.close()
    }
}
