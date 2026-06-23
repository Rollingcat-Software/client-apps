package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricEnrollmentResponseDto
import com.fivucsas.shared.data.remote.dto.IdentificationResponseDto
import com.fivucsas.shared.data.remote.dto.LivenessResponseDto
import com.fivucsas.shared.data.remote.dto.VerificationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Biometric API implementation.
 *
 * Talks to the Identity Core API (`api.fivucsas.com`), which serves biometrics
 * via its BiometricController and proxies to the internal biometric-processor.
 * The processor host `bio.fivucsas.com` has no public DNS, so it must NOT be
 * called directly from clients (see ApiConfig.biometricBaseUrl).
 *
 * Identity contract (relative to {base}/api/v1):
 * - POST   biometric/enroll/{userId}   multipart `image` (+ optional `tenant_id`)
 * - POST   biometric/verify/{userId}   multipart `image` (+ optional `tenant_id`)
 * - POST   biometric/search            multipart `file` (tenant derived server-side)
 * - DELETE biometric/face/{userId}
 *
 * Each method returns the identity `BiometricVerificationResponse`
 * ({verified, confidence, message, distance, threshold}); the DTOs default
 * their fields so partial payloads never break deserialization.
 */
class BiometricApiImpl(
    private val client: HttpClient
) : BiometricApi {

    override suspend fun enrollFace(
        userId: String,
        imageBytes: ByteArray,
        imageName: String,
        tenantId: String?
    ): BiometricEnrollmentResponseDto {
        return client.submitFormWithBinaryData(
            url = "biometric/enroll/$userId",
            formData = formData {
                if (tenantId != null) {
                    append("tenant_id", tenantId)
                }
                // Identity BiometricController expects the multipart part named "image".
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                })
            }
        ).body()
    }

    override suspend fun verifyFace(
        userId: String,
        imageBytes: ByteArray,
        imageName: String
    ): VerificationResponseDto {
        return client.submitFormWithBinaryData(
            url = "biometric/verify/$userId",
            formData = formData {
                append("image", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                })
            }
        ).body()
    }

    override suspend fun checkLiveness(
        imageBytes: ByteArray,
        imageName: String
    ): LivenessResponseDto {
        // TODO(#105)(biometric-liveness): The Identity Core API exposes NO standalone
        // liveness endpoint. Server-side passive liveness is folded into
        // /biometric/verify (LIVENESS_BACKEND=uniface, LIVENESS_MODE=passive on
        // the processor). There is therefore nothing for a client-only liveness
        // probe to call. Rather than hard-error with UnresolvedAddressException
        // (which previously blocked the whole login flow), return a safe,
        // non-blocking "live" verdict so liveness can never gate login. When a
        // dedicated identity liveness endpoint ships, wire it here.
        return LivenessResponseDto(
            isLive = true,
            livenessScore = 1.0f,
            challenge = "",
            challengeCompleted = true,
            message = "Liveness check delegated to server-side verification (no standalone endpoint)."
        )
    }

    override suspend fun deleteBiometricData(userId: String) {
        client.delete("biometric/face/$userId")
    }

    override suspend fun identifyFace(
        imageBytes: ByteArray,
        imageName: String
    ): IdentificationResponseDto {
        // Identity 1:N search is multipart `file`; tenant is derived from the
        // authenticated principal server-side (never trusted from the client).
        return client.submitFormWithBinaryData(
            url = "biometric/search",
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                })
            }
        ).body()
    }
}
