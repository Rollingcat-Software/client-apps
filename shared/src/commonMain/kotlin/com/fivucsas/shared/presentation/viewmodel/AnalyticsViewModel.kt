package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.repository.DashboardRepository
import com.fivucsas.shared.platform.IFileSaver
import com.fivucsas.shared.presentation.state.AnalyticsUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import com.fivucsas.shared.util.CsvExporter
import com.fivucsas.shared.util.StatisticsExportRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val dashboardRepository: DashboardRepository,
    private val fileSaver: IFileSaver
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            dashboardRepository.getStatistics().fold(
                onSuccess = { statistics ->
                    _uiState.update {
                        it.copy(isLoading = false, statistics = statistics)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ErrorMapper.mapToUserMessage(error, "load statistics")
                        )
                    }
                }
            )
        }
    }

    /**
     * Export current statistics to a CSV file.
     * Flattens the Statistics object into key-value rows.
     */
    fun exportCsv() {
        val stats = _uiState.value.statistics
        if (stats == null) {
            _uiState.update { it.copy(errorMessage = "No statistics data to export.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccessMessage = null, errorMessage = null) }

            try {
                val rows = statisticsToRows(stats)
                val csvContent = CsvExporter.toCsv(rows, CsvExporter.statisticsColumns())
                val fileName = "analytics_export.csv"

                fileSaver.saveTextFile(csvContent, fileName).fold(
                    onSuccess = { path ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                exportSuccessMessage = "Exported statistics to $path"
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

    private fun statisticsToRows(stats: Statistics): List<StatisticsExportRow> = listOf(
        StatisticsExportRow("Total Users", stats.totalUsers.toString()),
        StatisticsExportRow("Active Users", stats.activeUsers.toString()),
        StatisticsExportRow("Verifications Today", stats.verificationsToday.toString()),
        StatisticsExportRow("Success Rate (%)", stats.successRate.toString()),
        StatisticsExportRow("Failed Attempts", stats.failedAttempts.toString()),
        StatisticsExportRow("Pending Verifications", stats.pendingVerifications.toString()),
        StatisticsExportRow("Face Enrollments", stats.faceEnrollments.toString()),
        StatisticsExportRow("Voice Enrollments", stats.voiceEnrollments.toString()),
        StatisticsExportRow("Fingerprint Enrollments", stats.fingerprintEnrollments.toString()),
        StatisticsExportRow("TOTP Enrollments", stats.totpEnrollments.toString()),
        StatisticsExportRow("NFC Enrollments", stats.nfcEnrollments.toString()),
        StatisticsExportRow("Total Auth Attempts", stats.totalAuthAttempts.toString()),
        StatisticsExportRow("Auth Success Count", stats.authSuccessCount.toString()),
        StatisticsExportRow("Auth Failure Count", stats.authFailureCount.toString()),
        StatisticsExportRow("Logins Today", stats.loginsToday.toString()),
        StatisticsExportRow("Registrations Today", stats.registrationsToday.toString()),
        StatisticsExportRow("Enrollments Today", stats.enrollmentsToday.toString())
    )
}
