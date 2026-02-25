package com.fivucsas.desktop.ui.kiosk.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.components.DesktopKioskCameraOverlay
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.platform.ICameraService
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import org.koin.compose.koinInject

@Composable
fun VerifyScreen(
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cameraService: ICameraService = koinInject()
    var pendingVerify by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UIDimens.SpacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DesktopSectionHeader(
                title = "Identity Verification",
                subtitle = "Use camera capture for biometric identity verification"
            )

            Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UIDimens.SpacingXLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        uiState.isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
                            Text(
                                text = "Verifying identity...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        uiState.verificationResult != null -> {
                            val result = uiState.verificationResult!!
                            val isSuccess = result.isVerified

                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = if (isSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

                            Text(
                                text = if (isSuccess) "Verified!" else "Verification Failed",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isSuccess) {
                                Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
                                Text(
                                    text = "User: ${result.userName}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Confidence: ${result.confidence.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

                            Text(
                                text = "Position your face in the camera",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

                            Text(
                                text = "Ensure good lighting and look directly at the camera",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) { Text("Back") }

                Button(
                    onClick = {
                        if (uiState.capturedImage == null) {
                            pendingVerify = true
                            viewModel.openCamera()
                        } else {
                            viewModel.verifyWithCapturedImage()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading && uiState.verificationResult == null
                ) { Text("Verify Now") }
            }
        }

        if (uiState.showCamera) {
            DesktopKioskCameraOverlay(
                cameraService = cameraService,
                onCapture = { imageBytes ->
                    viewModel.setCapturedImage(imageBytes)
                    if (pendingVerify) {
                        pendingVerify = false
                        viewModel.verifyWithCapturedImage()
                    }
                },
                onClose = {
                    pendingVerify = false
                    viewModel.closeCamera()
                }
            )
        }
    }
}
