package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.QrLoginApi
import com.fivucsas.shared.data.remote.dto.QrLoginApproveRequestDto
import com.fivucsas.shared.data.remote.dto.QrLoginCreateSessionRequestDto
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.QrLoginSession
import com.fivucsas.shared.domain.repository.QrLoginRepository

class QrLoginRepositoryImpl(
    private val qrLoginApi: QrLoginApi
) : QrLoginRepository {
    override suspend fun createSession(platform: String): Result<QrLoginSession> {
        return try {
            val response = qrLoginApi.createSession(
                QrLoginCreateSessionRequestDto(platform = platform)
            )
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSession(sessionId: String): Result<QrLoginSession> {
        return try {
            val response = qrLoginApi.getSession(sessionId)
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approveSession(sessionId: String, approverPlatform: String): Result<Unit> {
        return try {
            qrLoginApi.approveSession(
                sessionId = sessionId,
                request = QrLoginApproveRequestDto(approverPlatform = approverPlatform)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
