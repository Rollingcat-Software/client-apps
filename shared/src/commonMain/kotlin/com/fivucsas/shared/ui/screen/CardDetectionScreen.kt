package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.CardDetectionResult
import com.fivucsas.shared.presentation.viewmodel.CardDetectionUiState
import com.fivucsas.shared.presentation.viewmodel.CardDetectionViewModel
import com.fivucsas.shared.presentation.viewmodel.CardTypeLabels

private const val STEP_CAPTURE = "CAPTURE"
private const val STEP_RESULT = "RESULT"

/**
 * Card Detection Screen (Shared / Cross-platform)
 *
 * Provides the complete UI for scanning bank cards and ID cards:
 * - Camera preview with card outline overlay (ISO/IEC 7810 aspect ratio)
 * - Auto-detect / manual capture button
 * - Detection result display with card type, confidence, bounding box
 * - Retry / done actions
 *
 * The actual camera rendering and image capture are handled by the platform
 * layer via [cameraContent] and [onCapture]. On Android this uses CameraX;
 * on desktop it uses JavaCV.
 *
 * Platform callers should:
 * 1. Handle camera permission before showing this screen.
 * 2. Provide a composable for [cameraContent] that renders the back camera feed.
 * 3. Implement [onCapture] to take a photo and call
 *    [CardDetectionViewModel.detectCard] with the JPEG bytes.
 */
@Composable
fun CardDetectionScreen(
    viewModel: CardDetectionViewModel,
    onBack: () -> Unit,
    cameraContent: @Composable () -> Unit = {},
    onCapture: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by rememberSaveable { mutableStateOf(STEP_CAPTURE) }

    LaunchedEffect(Unit) { viewModel.reset() }
    LaunchedEffect(uiState.result) {
        if (uiState.result != null) currentStep = STEP_RESULT
    }

    when {
        currentStep == STEP_RESULT && uiState.result != null -> {
            CardResultView(
                result = uiState.result!!,
                onRetry = {
                    currentStep = STEP_CAPTURE
                    viewModel.reset()
                },
                onDone = onBack
            )
        }
        uiState.isProcessing -> {
            CardProcessingView()
        }
        else -> {
            CardCaptureView(
                uiState = uiState,
                onBack = onBack,
                cameraContent = cameraContent,
                onCapture = onCapture
            )
        }
    }
}

@Composable
private fun CardCaptureView(
    uiState: CardDetectionUiState,
    onBack: () -> Unit,
    cameraContent: @Composable () -> Unit,
    onCapture: () -> Unit
) {
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

        // Header card
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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = s(StringKey.CARD_DETECTION_TITLE),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = s(StringKey.CARD_DETECTION_SUBTITLE),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Card outline guide (credit card aspect ratio: 85.60 x 53.98 mm ~ 1.586:1)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f)
                .aspectRatio(1.586f)
                .border(
                    BorderStroke(
                        3.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    ),
                    RoundedCornerShape(12.dp)
                )
        )

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error display
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Capture button
            Button(
                onClick = onCapture,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s(StringKey.CARD_DETECTION_CAPTURE),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CardProcessingView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = s(StringKey.LOADING),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.7f))
    }
}

@Composable
private fun CardResultView(
    result: CardDetectionResult,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Result card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = s(StringKey.CARD_DETECTION_RESULT),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card details
                CardDetailRow(
                    label = "${s(StringKey.CARD_TYPE)} (EN)",
                    value = result.cardTypeLabel
                )
                CardDetailRow(
                    label = "${s(StringKey.CARD_TYPE)} (TR)",
                    value = CardTypeLabels.getLabel(result.cardType, turkish = true)
                )
                CardDetailRow(
                    label = s(StringKey.CARD_CONFIDENCE),
                    value = "${(result.confidence * 100).toInt()}%"
                )

                // Confidence bar
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = result.confidence.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (result.confidence >= 0.7f)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                if (result.boundingBox.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CardDetailRow(
                        label = "Bounding Box",
                        value = result.boundingBox.joinToString(", ") { (kotlin.math.round(it * 10) / 10.0).toString() }
                    )
                }

                if (result.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CardDetailRow(label = "Message", value = result.message)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s(StringKey.RETRY),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = onDone,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s(StringKey.CLOSE),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun CardDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
