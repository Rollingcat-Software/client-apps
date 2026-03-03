package com.fivucsas.shared.domain.usecase.tenant

import com.fivucsas.shared.domain.exception.ValidationException
import com.fivucsas.shared.domain.model.TenantSettings
import com.fivucsas.shared.domain.repository.TenantSettingsRepository

class UpdateTenantSettingsUseCase(
    private val tenantSettingsRepository: TenantSettingsRepository
) {
    suspend operator fun invoke(settings: TenantSettings): Result<TenantSettings> {
        if (settings.confidenceThreshold !in 0.5f..1.0f) {
            return Result.failure(
                ValidationException("Confidence threshold must be between 0.5 and 1.0")
            )
        }

        if (settings.maxEnrollmentAttempts !in 1..10) {
            return Result.failure(
                ValidationException("Max enrollment attempts must be between 1 and 10")
            )
        }

        if (settings.sessionTimeoutMinutes !in 5..120) {
            return Result.failure(
                ValidationException("Session timeout must be between 5 and 120 minutes")
            )
        }

        if (settings.inviteExpiryDays !in 1..90) {
            return Result.failure(
                ValidationException("Invite expiry must be between 1 and 90 days")
            )
        }

        return tenantSettingsRepository.updateSettings(settings)
    }
}
