package com.fivucsas.shared.domain.usecase.auth.qr

import com.fivucsas.shared.domain.model.QrLoginSession
import com.fivucsas.shared.domain.repository.QrLoginRepository

class GetQrLoginSessionUseCase(
    private val qrLoginRepository: QrLoginRepository
) {
    suspend operator fun invoke(sessionId: String): Result<QrLoginSession> {
        if (sessionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Session id cannot be blank"))
        }
        return qrLoginRepository.getSession(sessionId = sessionId)
    }
}
