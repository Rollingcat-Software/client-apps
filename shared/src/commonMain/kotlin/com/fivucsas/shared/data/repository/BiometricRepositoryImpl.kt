package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.BiometricApi
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.BiometricData
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
 * Handles image encoding and API communication.
 */
class BiometricRepositoryImpl(
    private val biometricApi: BiometricApi
) : BiometricRepository {

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun enrollFace(userId: String, imageData: ByteArray): Result<BiometricData> {
        return try {
            // Convert image to Base64 for API transmission
            val base64Image = Base64.encode(imageData)

            // Call real API
            val response = biometricApi.enrollFace(userId, base64Image)

            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun verifyFace(imageData: ByteArray): Result<VerificationResult> {
        return try {
            // Convert image to Base64 for API transmission
            val base64Image = Base64.encode(imageData)

            // Call real API
            val response = biometricApi.verifyFace(base64Image)

            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkLiveness(actions: List<FacialAction>): Result<LivenessResult> {
        return try {
            // Convert FacialAction enum to string list for API
            val actionNames = actions.map { it.name }

            // Call real API
            val response = biometricApi.checkLiveness(actionNames)

            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBiometricData(userId: String): Result<BiometricData> {
        return try {
            val response = biometricApi.getBiometricData(userId)
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
