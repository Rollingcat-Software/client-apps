package com.fivucsas.shared.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.fivucsas.shared.presentation.state.VoiceSearchUiMatch
import com.fivucsas.shared.presentation.viewmodel.VoiceViewModel

private const val MAX_RECORDING_SECONDS = 10

private val PASSPHRASES = listOf(
    "The quick brown fox jumps over the lazy dog",
    "Every morning I enjoy a cup of fresh coffee",
    "Technology connects people across the world",
    "Sunlight filters through the autumn leaves",
    "A journey of a thousand miles begins with a single step"
)

/**
 * Voice Search Screen (1:N Speaker Identification)
 *
 * Dedicated screen for searching enrolled speakers by voice.
 * Provides recording controls, waveform visualization, passphrase prompt,
 * and displays matched users with similarity scores.
 *
 * Platform callers should:
 * 1. Handle microphone permission before showing this screen.
 * 2. Call [onStartRecording] to begin audio capture.
 * 3. Call [onStopRecording] when the user stops or the timer expires.
 *    The caller should encode the audio as base64 and call
 *    viewModel.search(base64) after stopping.
 */
@Composable
fun VoiceSearchScreen(
    viewModel: VoiceViewModel,
    onBack: () -> Unit,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    /** Called when user taps "Who Is This?" with the recorded audio ready */
    onSearchRequested: () -> Unit = {},
    /** Whether audio has been recorded and is ready for search */
    hasRecording: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var passphrase by remember { mutableStateOf(PASSPHRASES.random()) }

    // Auto-stop at max duration
    LaunchedEffect(uiState.recordingSeconds) {
        if (uiState.isRecording && uiState.recordingSeconds >= MAX_RECORDING_SECONDS) {
            onStopRecording()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top bar
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
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = s(StringKey.VOICE_SEARCH_TITLE),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Subtitle
        Text(
            text = s(StringKey.VOICE_SEARCH_SUBTITLE),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Passphrase card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = s(StringKey.VOICE_SEARCH_PASSPHRASE_LABEL),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$passphrase\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Waveform visualization
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    uiState.isRecording -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    hasRecording -> Color(0xFF1B5E20).copy(alpha = 0.08f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            if (uiState.isRecording) {
                VoiceSearchWaveform(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            } else if (hasRecording) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${s(StringKey.VOICE_SEARCH_RECORDING_READY)} (${uiState.recordingSeconds}s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = s(StringKey.VOICE_TAP_TO_RECORD),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Recording progress bar and timer
        AnimatedVisibility(
            visible = uiState.isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = {
                        (uiState.recordingSeconds.toFloat() / MAX_RECORDING_SECONDS)
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.errorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${s(StringKey.VOICE_RECORDING)}... ${uiState.recordingSeconds}s / ${MAX_RECORDING_SECONDS}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Recording controls
        val infiniteTransition = rememberInfiniteTransition(label = "voiceSearchPulse")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!hasRecording || uiState.isRecording) {
                // Start / Stop button
                Button(
                    onClick = {
                        if (uiState.isRecording) {
                            onStopRecording()
                        } else {
                            passphrase = PASSPHRASES.random()
                            viewModel.clearMessages()
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
            }

            if (hasRecording && !uiState.isRecording) {
                // "Who Is This?" search button
                Button(
                    onClick = onSearchRequested,
                    enabled = !uiState.isProcessing,
                    modifier = Modifier.height(48.dp)
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(s(StringKey.VOICE_SEARCH_WHO_IS_THIS))
                }

                // Reset button
                OutlinedButton(
                    onClick = {
                        viewModel.reset()
                        passphrase = PASSPHRASES.random()
                    },
                    enabled = !uiState.isProcessing,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s(StringKey.VOICE_SEARCH_RESET))
                }
            }
        }

        // Processing indicator
        AnimatedVisibility(
            visible = uiState.isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s(StringKey.LOADING),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

        // Search results
        uiState.searchResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with status chip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s(StringKey.VOICE_SEARCH_RESULTS),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = if (result.found)
                                        s(StringKey.VOICE_SEARCH_SPEAKER_IDENTIFIED)
                                    else
                                        s(StringKey.VOICE_SEARCH_NO_MATCH),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (result.found)
                                    Color(0xFF1B5E20).copy(alpha = 0.1f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (result.matches.isNotEmpty()) {
                        // Display each match
                        result.matches.forEachIndexed { index, match ->
                            if (index > 0) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                            VoiceSearchMatchItem(
                                match = match,
                                isBestMatch = index == 0
                            )
                        }
                    } else if (result.found && result.userId != null) {
                        // Fallback: single result from legacy response
                        VoiceSearchMatchItem(
                            match = VoiceSearchUiMatch(
                                userId = result.userId,
                                similarity = result.confidence
                            ),
                            isBestMatch = true
                        )
                    } else {
                        // No matches
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = s(StringKey.VOICE_SEARCH_NO_MATCH),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceSearchMatchItem(
    match: VoiceSearchUiMatch,
    isBestMatch: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User icon
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = if (isBestMatch)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Name row with best match chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = match.userName ?: match.userId,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isBestMatch) FontWeight.SemiBold else FontWeight.Normal
                )
                if (isBestMatch) {
                    Spacer(modifier = Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = s(StringKey.VOICE_SEARCH_BEST_MATCH),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        icon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.primary,
                            iconContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Email
            match.userEmail?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // User ID (if we have a display name, show ID smaller)
            if (match.userName != null) {
                Text(
                    text = "ID: ${match.userId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Similarity bar
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${s(StringKey.VOICE_SEARCH_SIMILARITY)}: ${(match.similarity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { match.similarity.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when {
                    match.similarity >= 0.8f -> Color(0xFF1B5E20)
                    match.similarity >= 0.6f -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.error
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * Animated waveform bars for the voice search recording state.
 */
@Composable
private fun VoiceSearchWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "searchWaveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val barColor = MaterialTheme.colorScheme.error
    Canvas(modifier = modifier) {
        val barCount = 30
        val barWidth = size.width / (barCount * 2)
        val maxBarHeight = size.height * 0.85f

        for (i in 0 until barCount) {
            val normalizedPos = i.toFloat() / barCount
            val wave = kotlin.math.sin((normalizedPos + phase) * 2 * kotlin.math.PI).toFloat()
            val barHeight = (maxBarHeight * 0.2f + maxBarHeight * 0.8f * ((wave + 1f) / 2f))
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
