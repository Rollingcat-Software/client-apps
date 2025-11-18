package com.fivucsas.desktop.ui.kiosk.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Verify Screen Component
 *
 * Identity verification with biometric capture and result display.
 *
 * @param viewModel Kiosk view model
 * @param onBack Callback to return to welcome screen
 */
@Composable
fun VerifyScreen(
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(UIDimens.SpacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Identity Verification",
                style = MaterialTheme.typography.displaySmall,
                color = AppColors.Primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

            // Main Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UIDimens.SpacingXLarge),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        uiState.isLoading -> {
                            // Loading State
                            CircularProgressIndicator(
                                modifier = Modifier.size(64.dp),
                                color = AppColors.Primary
                            )
                            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
                            Text(
                                text = "Verifying identity...",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.OnSurfaceVariant
                            )
                        }

                        uiState.verificationResult != null -> {
                            // Result State
                            val result = uiState.verificationResult!!
                            val isSuccess = result.isVerified

                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = if (isSuccess) AppColors.Success else AppColors.Error
                            )

                            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

                            Text(
                                text = if (isSuccess) "Verified!" else "Verification Failed",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) AppColors.Success else AppColors.Error
                            )

                            Spacer(modifier = Modifier.height(UIDimens.SpacingSmall))

                            Text(
                                text = result.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = AppColors.OnSurfaceVariant
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
                                    color = AppColors.OnSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            // Ready to Verify State
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = AppColors.Primary
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
                                color = AppColors.OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }

                Button(
                    onClick = {
                        viewModel.captureImage()
                        viewModel.verifyWithCapturedImage()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading && uiState.verificationResult == null
                ) {
                    Text("Verify Now")
                }
            }
        }
    }
}
