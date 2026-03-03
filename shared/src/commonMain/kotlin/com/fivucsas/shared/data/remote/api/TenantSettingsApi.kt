package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.TenantSettingsDto

/**
 * Tenant Settings API interface
 *
 * Endpoints:
 * - GET  /tenants/settings     → getSettings()
 * - PUT  /tenants/settings     → updateSettings()
 */
interface TenantSettingsApi {
    suspend fun getSettings(): TenantSettingsDto
    suspend fun updateSettings(settings: TenantSettingsDto): TenantSettingsDto
}
