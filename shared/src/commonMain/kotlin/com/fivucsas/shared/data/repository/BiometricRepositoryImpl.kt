package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.EnrollmentResult
import com.fivucsas.shared.domain.model.FacialAction
import com.fivucsas.shared.domain.model.IdentifyResult
import com.fivucsas.shared.domain.model.LivenessResult
import com.fivucsas.shared.domain.model.VerificationResult
import com.fivucsas.shared.domain.repository.BiometricRepository
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Real implementation of BiometricRepository
 *
 * Connects to the Biometric Processor API via BiometricApi.
 * Sends images as raw bytes using multipart form-data.
 */
class BiometricRepositoryImpl(
    private val biometricApi: BiometricApi
) : BiometricRepository {

    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<EnrollmentResult> {
        return try {
            val response = biometricApi.enrollFace(userId, imageData)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyFace(userId: String, imageData: ByteArray): Result<VerificationResult> {
        return try {
            val response = biometricApi.verifyFace(userId, imageData)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkLiveness(imageData: ByteArray): Result<LivenessResult> {
        return try {
            val response = biometricApi.checkLiveness(imageData)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBiometricData(userId: String): Result<Unit> {
        return try {
            biometricApi.deleteBiometricData(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun identifyFace(imageData: ByteArray): Result<IdentifyResult> {
        return try {
            val base64Image = Base64.encode(imageData)
            val response = biometricApi.identifyFace(base64Image)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
