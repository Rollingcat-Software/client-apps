package com.fivucsas.shared.presentation.viewmodel

import com.fivucsas.shared.domain.repository.DataExportRateLimitedException
import com.fivucsas.shared.domain.repository.DataExportRepository
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.IFileSaver
import com.fivucsas.shared.presentation.state.DataExportUiState
import com.fivucsas.shared.presentation.util.ErrorMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * ViewModel for the GDPR Art. 20 / KVKK data-portability flow.
 *
 * Responsibilities:
 *  1. Call [DataExportRepository.exportUserData] to fetch the JSON bundle.
 *  2. Persist it via the platform [IFileSaver] port (Android writes to
 *     Downloads, desktop opens a save dialog, iOS presents the share sheet).
 *  3. Surface a typed [DataExportUiState] so the screen can render a
 *     loading spinner, a success toast with the file path, or an error
 *     message (distinguishing 429 rate-limit from generic failures).
 *
 * Hexagonal role: presentation; depends on two ports only
 * ([DataExportRepository] + [IFileSaver]) — no HTTP or Android symbols leak
 * into this class, keeping it fully commonMain-testable.
 */
class DataExportViewModel(
    private val dataExportRepository: DataExportRepository,
    private val fileSaver: IFileSaver,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow<DataExportUiState>(DataExportUiState.Idle)
    val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()

    /**
     * Kick off the export. Transitions Idle → Exporting → Success/Error.
     * Safe to call multiple times; re-entrance while [DataExportUiState.Exporting]
     * is ignored (no overlapping downloads).
     */
    fun exportData(userId: String) {
        if (_uiState.value is DataExportUiState.Exporting) return
        _uiState.value = DataExportUiState.Exporting

        scope.launch {
            dataExportRepository.exportUserData(userId).fold(
                onSuccess = { json ->
                    val filename = "fivucsas-data-export-${timestampSuffix()}.json"
                    fileSaver.saveTextFile(
                        content = json,
                        suggestedFileName = filename,
                        mimeType = "application/json",
                    ).fold(
                        onSuccess = { path ->
                            _uiState.value = DataExportUiState.Success(fileUri = path)
                        },
                        onFailure = { saveErr ->
                            _uiState.value = DataExportUiState.Error(
                                reason = saveErr.message
                                    ?: s(StringKey.DATA_EXPORT_ERROR_GENERIC),
                                isRateLimit = false,
                            )
                        },
                    )
                },
                onFailure = { err ->
                    _uiState.value = when (err) {
                        is DataExportRateLimitedException -> DataExportUiState.Error(
                            reason = s(StringKey.DATA_EXPORT_ERROR_RATE_LIMITED),
                            isRateLimit = true,
                            retryAfterSeconds = err.retryAfterSeconds,
                        )
                        else -> DataExportUiState.Error(
                            reason = ErrorMapper.mapToUserMessage(err, "export data"),
                            isRateLimit = false,
                        )
                    }
                },
            )
        }
    }

    /** Reset back to Idle (e.g. after the user dismisses a snackbar). */
    fun reset() {
        _uiState.value = DataExportUiState.Idle
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }

    private fun timestampSuffix(): String {
        // ISO-8601 instant, ":" replaced to be filesystem-safe on all platforms.
        return Clock.System.now().toString().replace(":", "-")
    }
}
