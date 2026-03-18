package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.StatisticsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class DashboardApiImpl(
    private val client: HttpClient
) : DashboardApi {

    companion object {
        private const val BASE_PATH = "dashboard"
    }

    override suspend fun getStatistics(): StatisticsDto {
        return client.get("$BASE_PATH/statistics").body()
    }
}
