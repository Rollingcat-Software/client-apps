package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.TenantSettingsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TenantSettingsApiImpl(
    private val client: HttpClient
) : TenantSettingsApi {

    companion object {
        private const val BASE_PATH = "tenants/settings"
    }

    override suspend fun getSettings(): TenantSettingsDto {
        return client.get(BASE_PATH).body()
    }

    override suspend fun updateSettings(settings: TenantSettingsDto): TenantSettingsDto {
        return client.put(BASE_PATH) {
            contentType(ContentType.Application.Json)
            setBody(settings)
        }.body()
    }
}
