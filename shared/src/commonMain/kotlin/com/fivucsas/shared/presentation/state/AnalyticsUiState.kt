package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.Statistics

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val statistics: Statistics? = null,
    val errorMessage: String? = null,

    // CSV export state
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null
)
