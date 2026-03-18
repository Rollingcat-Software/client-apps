package com.fivucsas.mobile.android.ui.screen

import android.Manifest
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.VoiceMode
import com.fivucsas.shared.presentation.viewmodel.VoiceViewModel
import com.fivucsas.shared.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceEnrollScreen(
    userId: String,
    viewModel: VoiceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var amplitude by remember { mutableFloatStateOf(0f) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // Waveform amplitude polling during recording
    LaunchedEffect(uiState.isRecording) {
        if (uiState.isRecording) {
            var seconds = 0
            while (uiState.isRecording) {
                delay(100)
                amplitude = try {
                    recorder?.maxAmplitude?.toFloat()?.div(32767f) ?: 0f
                } catch (_: Exception) { 0f }
                seconds++
                if (seconds % 10 == 0) {
                    viewModel.updateRecordingSeconds(seconds / 10)
                }
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
        val file = File(context.cacheDir, "voice_record_${System.currentTimeMillis()}.m4a")
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

    fun stopRecordingAndProcess() {
        try {
            recorder?.stop()
            recorder?.release()
        } catch (_: Exception) {}
        recorder = null
        viewModel.setRecording(false)

        val file = audioFile ?: return
        if (!file.exists()) return

        val bytes = file.readBytes()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

        when (uiState.selectedMode) {
            VoiceMode.ENROLL -> viewModel.enroll(userId, base64)
            VoiceMode.VERIFY -> viewModel.verify(userId, base64)
            VoiceMode.SEARCH -> viewModel.search(base64)
        }

        file.delete()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s(StringKey.VOICE_RECOGNITION)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.BACK))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedMode == VoiceMode.ENROLL,
                    onClick = { viewModel.setMode(VoiceMode.ENROLL) },
                    label = { Text(s(StringKey.VOICE_ENROLL)) }
                )
                FilterChip(
                    selected = uiState.selectedMode == VoiceMode.VERIFY,
                    onClick = { viewModel.setMode(VoiceMode.VERIFY) },
                    label = { Text(s(StringKey.VOICE_VERIFY)) }
                )
                FilterChip(
                    selected = uiState.selectedMode == VoiceMode.SEARCH,
                    onClick = { viewModel.setMode(VoiceMode.SEARCH) },
                    label = { Text(s(StringKey.VOICE_SEARCH)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions
            Text(
                text = when (uiState.selectedMode) {
                    VoiceMode.ENROLL -> s(StringKey.VOICE_ENROLL_INSTRUCTION)
                    VoiceMode.VERIFY -> s(StringKey.VOICE_VERIFY_INSTRUCTION)
                    VoiceMode.SEARCH -> s(StringKey.VOICE_SEARCH_INSTRUCTION)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Waveform visualization
            Card(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (uiState.isRecording) {
                    WaveformVisualizer(
                        amplitude = amplitude,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (!hasPermission) s(StringKey.VOICE_PERMISSION_REQUIRED) else s(StringKey.VOICE_TAP_TO_RECORD),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recording duration
            if (uiState.isRecording) {
                Text(
                    text = "${s(StringKey.VOICE_RECORDING)}... ${uiState.recordingSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Record/Stop button
            Button(
                onClick = {
                    if (uiState.isRecording) {
                        stopRecordingAndProcess()
                    } else {
                        startRecording()
                    }
                },
                enabled = hasPermission && !uiState.isProcessing,
                modifier = Modifier.size(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isRecording)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (uiState.isRecording) "Stop" else "Record",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Processing indicator
            if (uiState.isProcessing) {
                CircularProgressIndicator()
                Text(
                    text = s(StringKey.LOADING),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Results
            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20).copy(alpha = 0.1f))
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Verify result details
            uiState.verifyResult?.let { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (result.verified) s(StringKey.VOICE_VERIFIED) else s(StringKey.VOICE_NOT_VERIFIED),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.verified) Color(0xFF1B5E20) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "${s(StringKey.VOICE_CONFIDENCE)}: ${(result.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Search result details
            uiState.searchResult?.let { result ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (result.found) s(StringKey.VOICE_USER_FOUND) else s(StringKey.VOICE_USER_NOT_FOUND),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (result.found) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (result.found && result.userId != null) {
                            Text(
                                text = "User: ${result.userId}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${s(StringKey.VOICE_CONFIDENCE)}: ${(result.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformVisualizer(amplitude: Float, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val barCount = 30
        val barWidth = size.width / (barCount * 2)
        val maxBarHeight = size.height * 0.9f

        for (i in 0 until barCount) {
            val randomFactor = (0.3f + (Math.random().toFloat() * 0.7f))
            val barHeight = maxBarHeight * amplitude * randomFactor
            val clampedHeight = barHeight.coerceIn(4f, maxBarHeight)
            val x = i * barWidth * 2 + barWidth / 2
            val top = (size.height - clampedHeight) / 2

            drawLine(
                color = barColor,
                start = Offset(x, top),
                end = Offset(x, top + clampedHeight),
                strokeWidth = barWidth * 0.8f
            )
        }
    }
}
