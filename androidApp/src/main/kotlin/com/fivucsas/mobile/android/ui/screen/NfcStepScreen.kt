package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.mobile.android.ui.component.MrzInputDialog
import com.fivucsas.mobile.android.ui.viewmodel.NfcStepViewModel
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.platform.INfcService
import org.koin.compose.koinInject

/**
 * NFC MFA step screen.
 *
 * Embeds the NFC scan UX inside an MFA flow step:
 *  1. Prompt the user to place passport / eID near the back of the phone.
 *  2. Offer camera MRZ capture via [MrzScannerScreen] (primary) and manual
 *     entry via [MrzInputDialog] (fallback).
 *  3. On successful NFC tap + BAC read, invoke [onVerify] with the payload
 *     the backend handler expects.
 *
 * The screen owns a [NfcStepViewModel] that bridges the shared
 * [INfcService]. Strings are hard-coded English placeholders pending
 * [StringResources] additions — see /tmp/i18n_agent_20A.txt.
 */
@Composable
fun NfcStepScreen(
    onVerify: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NfcStepViewModel? = null
) {
    val defaultService = koinInject<INfcService>()
    val vm = viewModel ?: remember { NfcStepViewModel(nfcService = defaultService) }
    val uiState by vm.uiState.collectAsState()

    var showMrzDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { vm.reset() }
    }

    if (showMrzDialog) {
        MrzInputDialog(
            onDismiss = { showMrzDialog = false },
            onAuthenticate = { mrz ->
                showMrzDialog = false
                vm.startScanWithMrz(mrz)
            }
        )
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is NfcStepViewModel.UiState.Idle -> {
                IdleContent(
                    onStart = { vm.beginMrzCapture() }
                )
            }

            is NfcStepViewModel.UiState.MrzCapture -> {
                MrzCaptureContent(
                    onOpenDialog = { showMrzDialog = true },
                    onCancel = { vm.cancel() }
                )
                // Auto-open the dialog the first time we land in MrzCapture.
                DisposableEffect(Unit) {
                    showMrzDialog = true
                    onDispose { }
                }
            }

            is NfcStepViewModel.UiState.Scanning -> {
                ScanningContent(cardTypeName = state.cardTypeName)
            }

            is NfcStepViewModel.UiState.Success -> {
                SuccessContent(
                    displayName = state.document.fullName.ifBlank { state.document.documentNumber },
                    onVerify = { onVerify(state.payload) }
                )
            }

            is NfcStepViewModel.UiState.Error -> {
                ErrorContent(
                    message = state.reason,
                    canRetry = state.isRecoverable,
                    onRetry = { vm.beginMrzCapture() },
                    onCancel = { vm.cancel() }
                )
            }
        }
    }
}

@Composable
private fun IdleContent(onStart: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Nfc,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = s(StringKey.NFC_STEP_IDLE_PROMPT),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onStart,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(s(StringKey.NFC_STEP_START_BUTTON))
    }
}

@Composable
private fun MrzCaptureContent(
    onOpenDialog: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = s(StringKey.NFC_STEP_MRZ_CAPTURE_HINT),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(12.dp))
    Button(
        onClick = onOpenDialog,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(s(StringKey.NFC_STEP_REOPEN_DIALOG))
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
        Text(s(StringKey.CANCEL))
    }
}

@Composable
private fun ScanningContent(cardTypeName: String) {
    CircularProgressIndicator(modifier = Modifier.size(48.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = s(StringKey.NFC_STEP_SCANNING),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
    if (cardTypeName.isNotBlank() && cardTypeName != "Unknown") {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = s(StringKey.NFC_STEP_DETECTED_PREFIX, cardTypeName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SuccessContent(displayName: String, onVerify: () -> Unit) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = s(StringKey.NFC_STEP_SUCCESS),
        style = MaterialTheme.typography.titleMedium
    )
    if (displayName.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onVerify,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(s(StringKey.NFC_STEP_SUBMIT_BUTTON))
    }
}

@Composable
private fun ErrorContent(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Icon(
        imageVector = Icons.Default.Error,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
        tint = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
    if (canRetry) {
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text(s(StringKey.NFC_STEP_RETRY_BUTTON))
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
        Text(s(StringKey.CANCEL))
    }
}

