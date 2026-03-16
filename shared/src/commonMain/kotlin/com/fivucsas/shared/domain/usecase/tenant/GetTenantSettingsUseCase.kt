package com.fivucsas.shared.domain.usecase.tenant

import com.fivucsas.shared.domain.model.TenantSettings
import com.fivucsas.shared.domain.repository.TenantSettingsRepository

class GetTenantSettingsUseCase(
    private val tenantSettingsRepository: TenantSettingsRepository
) {
    suspend operator fun invoke(): Result<TenantSettings> {
        return tenantSettingsRepository.getSettings()
    }
}
