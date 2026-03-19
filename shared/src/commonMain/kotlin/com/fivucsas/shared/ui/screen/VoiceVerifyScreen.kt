package com.fivucsas.shared.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.VoiceMode
import com.fivucsas.shared.presentation.viewmodel.VoiceViewModel

/**
 * Voice Verify Screen (Shared / Cross-platform)
 *
 * Provides the UI for voice authentication: enroll, verify, and search modes.
 * Recording must be triggered by the platform layer (Android MediaRecorder,
 * desktop audio capture, etc.). This screen drives the ViewModel and renders
 * status, waveform placeholder, and results.
 *
 * Platform callers should:
 * 1. Handle microphone permission before showing this screen.
 * 2. Call [onStartRecording] / [onStopRecording] which return the base64 audio
 *    to the ViewModel via enroll/verify/search.
 */
@Composable
fun VoiceVerifyScreen(
    userId: String,
    viewModel: VoiceViewModel,
    onBack: () -> Unit,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = s(StringKey.BACK)
                )
            }
            Text(
                text = s(StringKey.VOICE_RECOGNITION),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Mode selector chips
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

        // Waveform / recording area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (uiState.isRecording) {
                VoicePulseAnimation(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = s(StringKey.VOICE_TAP_TO_RECORD),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Recording timer
        if (uiState.isRecording) {
            Text(
                text = "${s(StringKey.VOICE_RECORDING)}... ${uiState.recordingSeconds}s",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Record / Stop button with pulse animation
        val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Button(
            onClick = {
                if (uiState.isRecording) {
                    onStopRecording()
                } else {
                    onStartRecording()
                }
            },
            enabled = !uiState.isProcessing,
            modifier = Modifier
                .size(80.dp)
                .then(
                    if (uiState.isRecording) Modifier.scale(pulse) else Modifier
                ),
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

        // Success message
        uiState.successMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B5E20).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Error message
        uiState.errorMessage?.let { message ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
                        color = if (result.verified) Color(0xFF1B5E20)
                        else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${s(StringKey.VOICE_CONFIDENCE)}: ${(result.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = result.confidence.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (result.confidence >= 0.7f)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
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
                        color = if (result.found) Color(0xFF1B5E20)
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (result.found && result.userId != null) {
                        Spacer(modifier = Modifier.height(4.dp))
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

/**
 * Animated pulse bars to indicate active recording.
 * This is a platform-independent alternative to the Android-specific
 * waveform visualizer that uses actual microphone amplitude.
 */
@Composable
private fun VoicePulseAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val barColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val barCount = 24
        val barWidth = size.width / (barCount * 2)
        val maxBarHeight = size.height * 0.85f

        for (i in 0 until barCount) {
            // Create a sine-wave-like pattern that scrolls with phase
            val normalizedPos = i.toFloat() / barCount
            val wave = kotlin.math.sin((normalizedPos + phase) * 2 * Math.PI).toFloat()
            val barHeight = (maxBarHeight * 0.3f + maxBarHeight * 0.7f * ((wave + 1f) / 2f))
                .coerceIn(4f, maxBarHeight)
            val x = i * barWidth * 2 + barWidth / 2
            val top = (size.height - barHeight) / 2

            drawLine(
                color = barColor,
                start = Offset(x, top),
                end = Offset(x, top + barHeight),
                strokeWidth = barWidth * 0.8f
            )
        }
    }
}
