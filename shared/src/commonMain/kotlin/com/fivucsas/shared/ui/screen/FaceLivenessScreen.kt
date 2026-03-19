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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.LivenessChallengeStep
import com.fivucsas.shared.presentation.viewmodel.LivenessUiState
import com.fivucsas.shared.presentation.viewmodel.LivenessViewModel

/**
 * Face Liveness Screen (Shared / Cross-platform)
 *
 * Provides the complete liveness challenge UI:
 * - Challenge step list with progress tracking
 * - Animated face guide circle
 * - Current instruction prompt
 * - Server verification status
 * - Result display with client/server scores
 *
 * The actual camera preview and ML-based face detection must be provided
 * by the platform layer via [cameraContent]. On Android this would be
 * CameraX + ML Kit; on desktop this would be JavaCV + OpenCV.
 *
 * Platform callers should:
 * 1. Handle camera permission before showing this screen.
 * 2. Provide a composable for [cameraContent] that renders the live camera feed.
 * 3. Use ML Kit / OpenCV to detect facial actions and call
 *    [LivenessViewModel.completeCurrentStep] when a challenge is detected.
 * 4. Capture a frame and call [LivenessViewModel.verifyWithServer] when
 *    all steps are completed.
 */
@Composable
fun FaceLivenessScreen(
    viewModel: LivenessViewModel,
    onBack: () -> Unit,
    cameraContent: @Composable () -> Unit = {},
    onCaptureAndVerify: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.challengeSteps.isEmpty()) {
            viewModel.startChallenge()
        }
    }

    when {
        uiState.isComplete -> {
            LivenessResultView(
                uiState = uiState,
                onDone = onBack,
                onRetry = {
                    viewModel.reset()
                    viewModel.startChallenge()
                }
            )
        }
        else -> {
            LivenessChallengeView(
                uiState = uiState,
                onBack = onBack,
                cameraContent = cameraContent,
                onCaptureAndVerify = onCaptureAndVerify
            )
        }
    }
}

@Composable
private fun LivenessChallengeView(
    uiState: LivenessUiState,
    onBack: () -> Unit,
    cameraContent: @Composable () -> Unit,
    onCaptureAndVerify: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "facePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview (provided by platform)
        cameraContent()

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = s(StringKey.BACK),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // Face guide circle (animated)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp)
                .scale(if (!uiState.allStepsCompleted) pulse else 1f)
                .border(
                    BorderStroke(
                        3.dp,
                        if (uiState.allStepsCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    ),
                    shape = CircleShape
                )
        )

        // Challenge progress card at top
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${s(StringKey.LIVENESS_TITLE)} (${uiState.completedSteps}/${uiState.totalSteps})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = if (uiState.totalSteps > 0)
                        uiState.completedSteps.toFloat() / uiState.totalSteps
                    else 0f,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Step list
                uiState.challengeSteps.forEachIndexed { index, step ->
                    ChallengeStepRow(
                        step = step,
                        isCurrent = index == uiState.currentStepIndex
                    )
                }
            }
        }

        // Current instruction at bottom
        AnimatedVisibility(
            visible = !uiState.allStepsCompleted && uiState.currentStep != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = uiState.currentStep?.label ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                )
            }
        }

        // All steps done -- prompt capture
        AnimatedVisibility(
            visible = uiState.allStepsCompleted && !uiState.isVerifying,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Button(
                onClick = onCaptureAndVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s(StringKey.LIVENESS_TITLE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Verifying overlay
        if (uiState.isVerifying) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s(StringKey.LOADING),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Error display
        if (uiState.errorMessage != null && !uiState.isComplete) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeStepRow(
    step: LivenessChallengeStep,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (step.completed)
                Icons.Default.CheckCircle
            else
                Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = when {
                step.completed -> MaterialTheme.colorScheme.primary
                isCurrent -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = step.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent && !step.completed)
                FontWeight.Bold
            else
                FontWeight.Normal,
            color = if (step.completed)
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun LivenessResultView(
    uiState: LivenessUiState,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.serverLive)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (uiState.serverLive)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.Error,
                    contentDescription = null,
                    tint = if (uiState.serverLive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (uiState.serverLive)
                        s(StringKey.LIVENESS_VERIFIED)
                    else
                        s(StringKey.LIVENESS_FAILED),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Client score
                ScoreRow(
                    label = s(StringKey.LIVENESS_CLIENT_SCORE),
                    score = uiState.clientScore
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Server score
                ScoreRow(
                    label = s(StringKey.LIVENESS_SERVER_SCORE),
                    score = uiState.serverScore
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s(StringKey.RETRY))
                    }
                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(s(StringKey.CLOSE))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(label: String, score: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "${(score * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (score >= 0.7f)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
    LinearProgressIndicator(
        progress = score.coerceIn(0f, 1f),
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = if (score >= 0.7f)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.error
    )
}
