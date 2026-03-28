package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.StatisticsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class DashboardApiImpl(
    private val client: HttpClient
) : DashboardApi {

    companion object {
        // Backend endpoint is GET /api/v1/statistics (no "dashboard" prefix)
        private const val STATS_PATH = "statistics"
    }

    override suspend fun getStatistics(): StatisticsDto {
        return client.get(STATS_PATH).body()
    }
}
