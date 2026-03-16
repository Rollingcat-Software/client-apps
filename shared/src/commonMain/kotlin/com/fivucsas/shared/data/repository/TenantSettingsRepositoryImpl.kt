package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.TenantSettingsApi
import com.fivucsas.shared.data.remote.dto.toDto
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.TenantSettings
import com.fivucsas.shared.domain.repository.TenantSettingsRepository

class TenantSettingsRepositoryImpl(
    private val tenantSettingsApi: TenantSettingsApi
) : TenantSettingsRepository {

    override suspend fun getSettings(): Result<TenantSettings> {
        return try {
            val response = tenantSettingsApi.getSettings()
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSettings(settings: TenantSettings): Result<TenantSettings> {
        return try {
            val response = tenantSettingsApi.updateSettings(settings.toDto())
            Result.success(response.toModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
