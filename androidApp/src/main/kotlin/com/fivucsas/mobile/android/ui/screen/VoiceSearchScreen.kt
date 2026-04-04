package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.fivucsas.shared.presentation.viewmodel.VoiceViewModel
import com.fivucsas.shared.ui.screen.VoiceSearchScreen
import kotlinx.coroutines.delay
import java.io.File

/**
 * Android wrapper for Voice Search (1:N Speaker Identification).
 *
 * Handles Android-specific MediaRecorder and permission management,
 * then delegates the UI to the shared VoiceSearchScreen composable.
 */
@Composable
fun AndroidVoiceSearchScreen(
    viewModel: VoiceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var hasRecordedAudio by remember { mutableStateOf(false) }
    var lastBase64 by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Timer: count seconds while recording
    LaunchedEffect(uiState.isRecording) {
        if (uiState.isRecording) {
            var ticks = 0
            while (uiState.isRecording) {
                delay(1000)
                ticks++
                viewModel.updateRecordingSeconds(ticks)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { recorder?.stop() } catch (_: Exception) {}
            try { recorder?.release() } catch (_: Exception) {}
        }
    }

    fun startRecording() {
        hasRecordedAudio = false
        lastBase64 = null
        val file = File(context.cacheDir, "voice_search_${System.currentTimeMillis()}.m4a")
        audioFile = file
        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        mr.setAudioSource(MediaRecorder.AudioSource.MIC)
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mr.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mr.setAudioSamplingRate(16000)
        mr.setAudioChannels(1)
        mr.setOutputFile(file.absolutePath)
        mr.prepare()
        mr.start()
        recorder = mr
        viewModel.setRecording(true)
        viewModel.updateRecordingSeconds(0)
    }

    fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        viewModel.setRecording(false)

        val file = audioFile ?: return
        if (!file.exists()) return

        val bytes = file.readBytes()
        lastBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        hasRecordedAudio = true
        file.delete()
    }

    fun performSearch() {
        val base64 = lastBase64 ?: return
        viewModel.search(base64)
    }

    VoiceSearchScreen(
        viewModel = viewModel,
        onBack = onNavigateBack,
        onStartRecording = {
            if (hasPermission) startRecording()
        },
        onStopRecording = { stopRecording() },
        onSearchRequested = { performSearch() },
        hasRecording = hasRecordedAudio && !uiState.isRecording
    )
}
