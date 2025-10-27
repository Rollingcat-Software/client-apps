package com.fivucsas.mobile.domain.repository

import com.fivucsas.mobile.domain.model.BiometricResult

interface BiometricRepository {
    suspend fun enrollFace(userId: String, imageBytes: ByteArray): Result<BiometricResult>

    suspend fun verifyFace(userId: String, imageBytes: ByteArray): Result<BiometricResult>
}
