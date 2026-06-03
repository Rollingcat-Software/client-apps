package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.AuditLogRepository
import com.fivucsas.shared.presentation.state.ActivityHistoryUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 50

/**
 * ViewModel for the user-facing Activity History screen (bottom-nav "History").
 *
 * Loads the current user's OWN activity events from `GET /api/v1/my/activity`
 * (user-scoped — does NOT require admin authority, unlike the audit-log dashboard).
 * Loads the first page on construction.
 */
class ActivityHistoryViewModel(
    private val auditLogRepository: AuditLogRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ActivityHistoryUiState())
    val uiState: StateFlow<ActivityHistoryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            auditLogRepository.getMyActivity(page = 0, size = PAGE_SIZE).fold(
                onSuccess = { events ->
                    _uiState.update {
                        it.copy(isLoading = false, events = events, errorMessage = null)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load activity history")
                        )
                    }
                }
            )
        }
    }
}
