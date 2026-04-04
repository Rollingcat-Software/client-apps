package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.AuditLog

data class AuditLogDashboardUiState(
    val logs: List<AuditLog> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,

    // Filter state
    val filterAction: String = "",
    val filterUserId: String = "",

    // Pagination
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true
)
