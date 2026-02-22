package com.fivucsas.shared.domain.usecase.auth.qr

import com.fivucsas.shared.domain.model.QrLoginSession
import com.fivucsas.shared.domain.repository.QrLoginRepository

class StartQrLoginSessionUseCase(
    private val qrLoginRepository: QrLoginRepository
) {
    suspend operator fun invoke(platform: String): Result<QrLoginSession> {
        if (platform.isBlank()) {
            return Result.failure(IllegalArgumentException("Platform cannot be blank"))
        }
        return qrLoginRepository.createSession(platform = platform)
    }
}
