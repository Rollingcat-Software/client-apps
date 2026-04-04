package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.StepUpMethod
import com.fivucsas.shared.presentation.viewmodel.StepUpAuthViewModel

/**
 * Step-Up Authentication Screen (Phase 2.4)
 *
 * Presents available step-up methods when an action requires
 * elevated verification. On success, invokes [onSuccess] with
 * the short-lived step-up token.
 */
@Composable
fun StepUpAuthScreen(
    viewModel: StepUpAuthViewModel,
    reason: String = "",
    onSuccess: (token: String) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(reason) {
        if (reason.isNotBlank()) {
            viewModel.setReason(reason)
        }
    }

    // Navigate on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.stepUpToken != null) {
            onSuccess(uiState.stepUpToken!!)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = s(StringKey.STEP_UP_TITLE),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle / reason
        Text(
            text = if (uiState.reason.isNotBlank()) {
                s(StringKey.STEP_UP_REASON, uiState.reason)
            } else {
                s(StringKey.STEP_UP_SUBTITLE)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (uiState.isVerifying) {
            // Verifying state
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s(StringKey.STEP_UP_VERIFYING),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else if (uiState.isSuccess) {
            // Success state
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = s(StringKey.STEP_UP_SUCCESS),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Method selection
            Text(
                text = s(StringKey.STEP_UP_SELECT_METHOD),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.availableMethods) { method ->
                    StepUpMethodCard(
                        method = method,
                        isSelected = uiState.selectedMethod == method,
                        onClick = { viewModel.selectMethod(method) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Button(
                onClick = { viewModel.verify() },
                enabled = uiState.selectedMethod != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s(StringKey.CONFIRM))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(s(StringKey.CANCEL))
            }
        }
    }
}

@Composable
private fun StepUpMethodCard(
    method: StepUpMethod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val title = when (method) {
        StepUpMethod.FINGERPRINT -> s(StringKey.STEP_UP_METHOD_FINGERPRINT)
        StepUpMethod.FACE -> s(StringKey.STEP_UP_METHOD_FACE)
        StepUpMethod.TOTP -> s(StringKey.STEP_UP_METHOD_TOTP)
    }

    val description = when (method) {
        StepUpMethod.FINGERPRINT -> s(StringKey.MULTI_STEP_DESC_FINGERPRINT)
        StepUpMethod.FACE -> s(StringKey.MULTI_STEP_DESC_FACE)
        StepUpMethod.TOTP -> s(StringKey.MULTI_STEP_DESC_TOTP)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
