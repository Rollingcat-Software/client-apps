package com.fivucsas.shared.data.repository

import com.fivucsas.shared.data.remote.api.DashboardApi
import com.fivucsas.shared.data.remote.dto.toModel
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.repository.DashboardRepository

class DashboardRepositoryImpl(
    private val dashboardApi: DashboardApi
) : DashboardRepository {

    override suspend fun getStatistics(): Result<Statistics> {
        return runCatching {
            dashboardApi.getStatistics().toModel()
        }
    }
}
