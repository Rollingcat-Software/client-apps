package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.TenantSettings

interface TenantSettingsRepository {
    suspend fun getSettings(): Result<TenantSettings>
    suspend fun updateSettings(settings: TenantSettings): Result<TenantSettings>
}
