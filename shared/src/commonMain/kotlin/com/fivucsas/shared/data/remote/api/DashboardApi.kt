package com.fivucsas.shared.data.remote.api

import com.fivucsas.shared.data.remote.dto.StatisticsDto

/**
 * Dashboard API interface
 *
 * Endpoints:
 * - GET /dashboard/statistics
 */
interface DashboardApi {
    suspend fun getStatistics(): StatisticsDto
}
