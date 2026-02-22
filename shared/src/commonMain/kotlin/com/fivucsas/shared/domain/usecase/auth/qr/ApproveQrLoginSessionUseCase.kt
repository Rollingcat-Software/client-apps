package com.fivucsas.shared.domain.usecase.auth.qr

import com.fivucsas.shared.domain.repository.QrLoginRepository

class ApproveQrLoginSessionUseCase(
    private val qrLoginRepository: QrLoginRepository
) {
    suspend operator fun invoke(sessionId: String, approverPlatform: String): Result<Unit> {
        if (sessionId.isBlank()) {
            return Result.failure(IllegalArgumentException("Session id cannot be blank"))
        }
        if (approverPlatform.isBlank()) {
            return Result.failure(IllegalArgumentException("Approver platform cannot be blank"))
        }
        return qrLoginRepository.approveSession(
            sessionId = sessionId,
            approverPlatform = approverPlatform
        )
    }
}
