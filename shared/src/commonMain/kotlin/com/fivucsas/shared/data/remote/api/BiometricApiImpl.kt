package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.BiometricEnrollmentResponseDto
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
 * Biometric API implementation
 * Connects to Biometric Processor FastAPI service (port 8001)
 *
 * All face endpoints use multipart/form-data matching the FastAPI backend:
 * - POST /enroll   - file: UploadFile, user_id: Form
 * - POST /verify   - file: UploadFile, user_id: Form
 * - POST /liveness - file: UploadFile
 * - DELETE /enroll/{user_id}
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
            url = "enroll",
            formData = formData {
                append("user_id", userId)
                if (tenantId != null) {
                    append("tenant_id", tenantId)
                }
                append("file", imageBytes, Headers.build {
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
            url = "verify",
            formData = formData {
                append("user_id", userId)
                append("file", imageBytes, Headers.build {
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
        return client.submitFormWithBinaryData(
            url = "liveness",
            formData = formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$imageName\"")
                })
            }
        ).body()
    }

    override suspend fun deleteBiometricData(userId: String) {
        client.delete("enroll/$userId")
    }
}
