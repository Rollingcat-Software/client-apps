package com.fivucsas.shared.domain.repository

import com.fivucsas.shared.domain.model.Statistics

interface DashboardRepository {
    suspend fun getStatistics(): Result<Statistics>
}
