package com.fivucsas.mobile.android.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.fivucsas.shared.domain.repository.DataExportRateLimitedException
import com.fivucsas.shared.domain.repository.DataExportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android-side ViewModel for GDPR Art. 20 / KVKK "Export my data".
 *
 * Thin Android wrapper around [DataExportRepository] that handles the
 * platform-specific side-effects:
 *   1. Calls the repository to fetch the JSON bundle from
 *      `GET /api/v1/users/{id}/export`.
 *   2. Persists the bundle to the public Downloads folder (MediaStore on
 *      Android 10+, direct file write on older devices).
 *   3. Exposes a file path via [State.Success] so the screen can launch an
 *      `ACTION_SEND` share-sheet for the user to forward to Drive, email, …
 *
 * States: [State.Idle] → [State.Exporting] → [State.Success] |
 *         [State.RateLimited] | [State.Error].
 *
 * Rate-limit (HTTP 429) is surfaced as a distinct state so the UI can show
 * tailored "try again in N seconds" copy.
 *
 * Hexagonal role: presentation adapter — depends only on the
 * [DataExportRepository] port + Android [Context]. Testable on the JVM by
 * passing a fake repository and a test [CoroutineDispatcher].
 */
class DataExportViewModel(
    private val repository: DataExportRepository,
    private val appContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {

    sealed class State {
        data object Idle : State()
        data object Exporting : State()
        data class Success(val filePath: String, val fileName: String) : State()
        data class RateLimited(val retryAfterSeconds: Long?) : State()
        data class Error(val message: String) : State()
    }

    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Triggers the GDPR export flow. Ignored if an export is already running
     * to prevent overlapping downloads + duplicate rate-limit hits.
     */
    fun exportData(userId: String) {
        if (_state.value is State.Exporting) return
        _state.value = State.Exporting
        scope.launch {
            repository.exportUserData(userId).fold(
                onSuccess = { json ->
                    val fileName = buildFileName()
                    val saveResult = withContext(ioDispatcher) {
                        saveJsonToDownloads(json, fileName)
                    }
                    _state.value = saveResult.fold(
                        onSuccess = { path -> State.Success(filePath = path, fileName = fileName) },
                        onFailure = { e ->
                            // TODO(i18n): DATA_EXPORT_* keys in /tmp/i18n_agent_20B.txt
                            State.Error(e.message ?: "Failed to save export file")
                        },
                    )
                },
                onFailure = { err ->
                    _state.value = when (err) {
                        is DataExportRateLimitedException ->
                            State.RateLimited(retryAfterSeconds = err.retryAfterSeconds)
                        else ->
                            // TODO(i18n): DATA_EXPORT_* keys in /tmp/i18n_agent_20B.txt
                            State.Error(err.message ?: "Data export failed")
                    }
                },
            )
        }
    }

    /** Reset back to Idle after the user dismisses the snackbar / toast. */
    fun reset() {
        _state.value = State.Idle
    }

    fun dispose() {
        scope.coroutineContext[Job]?.cancel()
    }

    private fun buildFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return "fivucsas_user_export_$stamp.json"
    }

    /**
     * Writes [content] to the public Downloads directory.
     *
     * Android 10+ (API 29) uses MediaStore.Downloads to avoid needing
     * WRITE_EXTERNAL_STORAGE. Older devices use a direct File write; the
     * legacy storage permission is already part of the app manifest for
     * pre-Q devices.
     *
     * Returns the absolute path (API < 29) or `content://…` URI (API ≥ 29).
     */
    private fun saveJsonToDownloads(content: String, fileName: String): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = appContext.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, values)
                    ?: return Result.failure(IllegalStateException("Could not create Downloads entry"))
                resolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                    ?: return Result.failure(IllegalStateException("Could not open output stream"))
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                Result.success(uri.toString())
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                var file = File(dir, fileName)
                var counter = 1
                while (file.exists()) {
                    val base = fileName.substringBeforeLast(".")
                    val ext = fileName.substringAfterLast(".", "json")
                    file = File(dir, "${base}_$counter.$ext")
                    counter++
                }
                file.writeText(content, Charsets.UTF_8)
                Result.success(file.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
