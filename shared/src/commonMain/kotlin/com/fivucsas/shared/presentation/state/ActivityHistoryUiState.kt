package com.fivucsas.shared.presentation.state

import com.fivucsas.shared.domain.model.AuditLog

/**
 * UI state for the user-facing Activity History screen.
 *
 * Backed by the user-scoped `GET /api/v1/my/activity` endpoint (the current user's
 * OWN events) — NOT the admin audit-log dashboard.
 */
data class ActivityHistoryUiState(
    val events: List<AuditLog> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
