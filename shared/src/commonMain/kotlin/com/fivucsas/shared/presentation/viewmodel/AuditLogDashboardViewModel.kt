package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.AuditLogRepository
import com.fivucsas.shared.platform.IFileSaver
import com.fivucsas.shared.presentation.state.AuditLogDashboardUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import com.fivucsas.shared.util.AuditLogExportRow
import com.fivucsas.shared.util.CsvExporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

/**
 * ViewModel for the Audit Log Dashboard screen (Phase 2.6).
 *
 * Supports filtering by action type and user ID, plus lazy loading
 * via pagination. Phase 3.4 adds CSV export of the currently loaded logs.
 */
class AuditLogDashboardViewModel(
    private val auditLogRepository: AuditLogRepository,
    private val fileSaver: IFileSaver
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(AuditLogDashboardUiState())
    val uiState: StateFlow<AuditLogDashboardUiState> = _uiState.asStateFlow()

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, currentPage = 0) }

            val state = _uiState.value
            auditLogRepository.getAuditLogs(
                action = state.filterAction.ifBlank { null },
                userId = state.filterUserId.ifBlank { null },
                page = 0,
                size = PAGE_SIZE
            ).fold(
                onSuccess = { logs ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            logs = logs,
                            currentPage = 0,
                            hasMorePages = logs.size >= PAGE_SIZE
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load audit logs")
                        )
                    }
                }
            )
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMorePages) return

        viewModelScope.launch {
            val nextPage = _uiState.value.currentPage + 1
            _uiState.update { it.copy(isLoadingMore = true) }

            val state = _uiState.value
            auditLogRepository.getAuditLogs(
                action = state.filterAction.ifBlank { null },
                userId = state.filterUserId.ifBlank { null },
                page = nextPage,
                size = PAGE_SIZE
            ).fold(
                onSuccess = { newLogs ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            logs = it.logs + newLogs,
                            currentPage = nextPage,
                            hasMorePages = newLogs.size >= PAGE_SIZE
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load more audit logs")
                        )
                    }
                }
            )
        }
    }

    fun updateFilterAction(action: String) {
        _uiState.update { it.copy(filterAction = action) }
    }

    fun updateFilterUserId(userId: String) {
        _uiState.update { it.copy(filterUserId = userId) }
    }

    fun applyFilters() {
        loadLogs()
    }

    fun clearFilters() {
        _uiState.update { it.copy(filterAction = "", filterUserId = "") }
        loadLogs()
    }

    /**
     * Export currently loaded audit logs to a CSV file.
     * Uses the platform-specific IFileSaver to write the file.
     */
    fun exportCsv() {
        val logs = _uiState.value.logs
        if (logs.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No audit logs to export.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccessMessage = null, errorMessage = null) }

            try {
                val rows = logs.map { log ->
                    AuditLogExportRow(
                        id = log.id,
                        timestamp = log.timestamp,
                        userId = log.userId,
                        action = log.action,
                        status = log.status,
                        ipAddress = log.ipAddress,
                        details = log.details
                    )
                }

                val csvContent = CsvExporter.toCsv(rows, CsvExporter.auditLogColumns())
                val fileName = "audit_log_export.csv"

                fileSaver.saveTextFile(csvContent, fileName).fold(
                    onSuccess = { path ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportSuccessMessage = "Exported ${logs.size} entries to $path"
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                errorMessage = "Export failed: ${error.message ?: "Unknown error"}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = "Export failed: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    /**
     * Clear the export success message after it has been shown.
     */
    fun clearExportMessage() {
        _uiState.update { it.copy(exportSuccessMessage = null) }
    }
}
