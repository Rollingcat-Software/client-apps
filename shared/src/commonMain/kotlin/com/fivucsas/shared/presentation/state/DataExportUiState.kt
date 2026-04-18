package com.fivucsas.shared.presentation.state

/**
 * UI state for the GDPR / KVKK data export screen row.
 *
 * Lifecycle: Idle → Exporting → (Success | Error) → Idle on reset.
 *
 * - [Success.fileUri] is the platform-specific absolute path / content-URI
 *   returned by [com.fivucsas.shared.platform.IFileSaver] so the Android
 *   screen can offer a share-intent.
 * - [Error.isRateLimit] lets the UI pick the tailored "try again in X
 *   seconds" copy vs. the generic error copy.
 * - [Error.retryAfterSeconds] mirrors the HTTP `Retry-After` header when
 *   present; null when the header was missing or the error wasn't a 429.
 */
sealed class DataExportUiState {
    data object Idle : DataExportUiState()
    data object Exporting : DataExportUiState()
    data class Success(val fileUri: String) : DataExportUiState()
    data class Error(
        val reason: String,
        val isRateLimit: Boolean = false,
        val retryAfterSeconds: Long? = null,
    ) : DataExportUiState()
}
