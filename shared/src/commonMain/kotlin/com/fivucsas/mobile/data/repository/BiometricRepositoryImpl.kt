package com.fivucsas.mobile.data.repository

import com.fivucsas.mobile.data.remote.ApiClient
import com.fivucsas.mobile.domain.model.BiometricResult
import com.fivucsas.mobile.domain.repository.BiometricRepository

class BiometricRepositoryImpl(
    private val apiClient: ApiClient
) : BiometricRepository {

    override suspend fun enrollFace(
        userId: String,
        imageBytes: ByteArray
    ): Result<BiometricResult> {
        return try {
            val response = apiClient.enrollFace(userId, imageBytes)

            val result = BiometricResult(
                verified = response.verified,
                confidence = response.confidence,
                message = response.message
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyFace(
        userId: String,
        imageBytes: ByteArray
    ): Result<BiometricResult> {
        return try {
            val response = apiClient.verifyFace(userId, imageBytes)

            val result = BiometricResult(
                verified = response.verified,
                confidence = response.confidence,
                message = response.message
            )

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
