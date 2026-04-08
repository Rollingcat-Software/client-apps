package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.data.remote.dto.AvailableMethodDto
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.StringResources
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowUiState
import com.fivucsas.shared.presentation.viewmodel.auth.MfaFlowViewModel
import kotlinx.coroutines.launch

/**
 * MFA Flow Screen
 *
 * Manages the full N-step MFA verification flow:
 * - Method selection for each step
 * - Step-specific input UIs (OTP, TOTP, etc.)
 * - Progress indicator (Step X of Y)
 * - Navigation back to method picker
 */
@Composable
fun MfaFlowScreen(
    viewModel: MfaFlowViewModel,
    onAuthenticated: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to dashboard when authenticated
    LaunchedEffect(uiState) {
        if (uiState is MfaFlowUiState.Authenticated) {
            onAuthenticated()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = s(StringKey.MFA_TITLE),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is MfaFlowUiState.Idle -> {
                    CircularProgressIndicator()
                }

                is MfaFlowUiState.MethodSelection -> {
                    MfaMethodSelectionContent(
                        availableMethods = state.availableMethods,
                        currentStep = state.currentStep,
                        totalSteps = state.totalSteps,
                        onMethodSelected = { viewModel.selectMethod(it) },
                        onCancel = onCancel
                    )
                }

                is MfaFlowUiState.StepInput -> {
                    MfaStepInputContent(
                        method = state.method,
                        currentStep = state.currentStep,
                        totalSteps = state.totalSteps,
                        viewModel = viewModel,
                        onBack = { viewModel.backToMethodSelection() }
                    )
                }

                is MfaFlowUiState.Verifying -> {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s(StringKey.MFA_VERIFYING),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is MfaFlowUiState.Authenticated -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = s(StringKey.MFA_AUTHENTICATED),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                is MfaFlowUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.canRetry) {
                        Button(onClick = { viewModel.retry() }) {
                            Text(s(StringKey.MFA_ERROR_RETRY))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onCancel) {
                        Text(s(StringKey.CANCEL))
                    }
                }
            }
        }
    }
}

/**
 * Step progress indicator + method selection cards.
 */
@Composable
private fun MfaMethodSelectionContent(
    availableMethods: List<AvailableMethodDto>,
    currentStep: Int,
    totalSteps: Int,
    onMethodSelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    // Progress
    StepProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = s(StringKey.MFA_SELECT_METHOD),
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = s(StringKey.MFA_SELECT_METHOD_DESC),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Method cards
    availableMethods.forEach { method ->
        MethodCard(
            method = method,
            onClick = { onMethodSelected(method.methodType) }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextButton(onClick = onCancel) {
        Text(s(StringKey.CANCEL))
    }
}

/**
 * Step input for the selected method.
 * Renders appropriate input UI based on the method type.
 */
@Composable
private fun MfaStepInputContent(
    method: String,
    currentStep: Int,
    totalSteps: Int,
    viewModel: MfaFlowViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    StepProgressIndicator(currentStep = currentStep, totalSteps = totalSteps)

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = methodDisplayName(method),
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    when (method) {
        "TOTP" -> {
            TotpStepInput(
                onVerify = { code ->
                    scope.launch {
                        viewModel.verifyStep(method, mapOf("code" to code))
                    }
                }
            )
        }

        "EMAIL_OTP", "SMS_OTP" -> {
            OtpStepInput(
                method = method,
                onSendOtp = {
                    scope.launch { viewModel.sendOtp(method) }
                },
                onVerify = { code ->
                    scope.launch {
                        viewModel.verifyStep(method, mapOf("code" to code))
                    }
                }
            )
        }

        "QR_CODE" -> {
            QrCodeStepInput(
                viewModel = viewModel,
                onVerify = {
                    scope.launch {
                        viewModel.verifyStep(method)
                    }
                }
            )
        }

        "FINGERPRINT" -> {
            // Fingerprint uses biometric prompt — auto-verify
            FingerprintStepInput(
                onVerify = { attestation ->
                    scope.launch {
                        viewModel.verifyStep(method, mapOf("attestation" to attestation))
                    }
                }
            )
        }

        "FACE", "VOICE", "NFC_DOCUMENT", "HARDWARE_KEY" -> {
            // Generic placeholder for methods that require platform-specific flows.
            // These will be wired to existing screens later.
            GenericMethodStepInput(
                method = method,
                onVerify = { data ->
                    scope.launch {
                        viewModel.verifyStep(method, data)
                    }
                }
            )
        }

        else -> {
            Text(
                text = "Unsupported method: $method",
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    TextButton(onClick = onBack) {
        Text(s(StringKey.MFA_BACK_TO_METHODS))
    }
}

// ── Step Input Composables ──────────────────────────────────────────

@Composable
private fun TotpStepInput(onVerify: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    OutlinedTextField(
        value = code,
        onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
        label = { Text(s(StringKey.MFA_ENTER_CODE)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onVerify(code) },
        modifier = Modifier.fillMaxWidth(),
        enabled = code.length == 6
    ) {
        Text(s(StringKey.MFA_VERIFY))
    }
}

@Composable
private fun OtpStepInput(
    method: String,
    onSendOtp: () -> Unit,
    onVerify: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (!otpSent) {
        Button(
            onClick = {
                onSendOtp()
                otpSent = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(s(StringKey.MFA_SEND_OTP))
        }
    } else {
        Text(
            text = s(StringKey.MFA_OTP_SENT),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it.filter { c -> c.isDigit() } },
            label = { Text(s(StringKey.MFA_ENTER_CODE)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onVerify(code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = code.length == 6
        ) {
            Text(s(StringKey.MFA_VERIFY))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSendOtp) {
            Text(s(StringKey.MFA_RESEND_OTP))
        }
    }
}

@Composable
private fun QrCodeStepInput(
    viewModel: MfaFlowViewModel,
    onVerify: () -> Unit
) {
    var qrToken by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.generateQr().fold(
            onSuccess = { response ->
                qrToken = response.qrToken
                loading = false
            },
            onFailure = {
                loading = false
            }
        )
    }

    if (loading) {
        CircularProgressIndicator()
    } else if (qrToken != null) {
        Text(
            text = s(StringKey.MFA_SCAN_QR),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Display the QR token as text (in a real app, render as QR image)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = qrToken ?: "",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onVerify, modifier = Modifier.fillMaxWidth()) {
            Text(s(StringKey.MFA_VERIFY))
        }
    } else {
        Text(
            text = s(StringKey.ERROR),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun FingerprintStepInput(onVerify: (String) -> Unit) {
    // Placeholder for biometric prompt integration.
    // The actual BiometricPrompt call will be wired via the platform.
    Icon(
        imageVector = Icons.Default.Fingerprint,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = s(StringKey.MFA_METHOD_FINGERPRINT),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onVerify("biometric_prompt") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(s(StringKey.MFA_VERIFY))
    }
}

@Composable
private fun GenericMethodStepInput(
    method: String,
    onVerify: (Map<String, String>) -> Unit
) {
    Icon(
        imageVector = methodIcon(method),
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = methodDisplayName(method),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onVerify(emptyMap()) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(s(StringKey.MFA_VERIFY))
    }
}

// ── Shared UI Components ──────────────────────────────────────────

@Composable
private fun StepProgressIndicator(currentStep: Int, totalSteps: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = StringResources.get(StringKey.MFA_STEP_COUNTER, currentStep, totalSteps),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentStep.toFloat() / totalSteps.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MethodCard(
    method: AvailableMethodDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (method.preferred)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = methodIcon(method.methodType),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = methodDisplayName(method.methodType),
                    style = MaterialTheme.typography.titleSmall
                )
                if (method.name.isNotBlank() && method.name != method.methodType) {
                    Text(
                        text = method.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (method.preferred) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────

private fun methodIcon(methodType: String): ImageVector = when (methodType) {
    "TOTP" -> Icons.Default.Security
    "EMAIL_OTP" -> Icons.Default.Email
    "SMS_OTP" -> Icons.Default.PhoneAndroid
    "FACE" -> Icons.Default.Face
    "VOICE" -> Icons.Default.RecordVoiceOver
    "FINGERPRINT" -> Icons.Default.Fingerprint
    "QR_CODE" -> Icons.Default.QrCode
    "HARDWARE_KEY" -> Icons.Default.Key
    "NFC_DOCUMENT" -> Icons.Default.Nfc
    else -> Icons.Default.Security
}

private fun methodDisplayName(methodType: String): String = when (methodType) {
    "TOTP" -> s(StringKey.MFA_METHOD_TOTP)
    "EMAIL_OTP" -> s(StringKey.MFA_METHOD_EMAIL_OTP)
    "SMS_OTP" -> s(StringKey.MFA_METHOD_SMS_OTP)
    "FACE" -> s(StringKey.MFA_METHOD_FACE)
    "VOICE" -> s(StringKey.MFA_METHOD_VOICE)
    "FINGERPRINT" -> s(StringKey.MFA_METHOD_FINGERPRINT)
    "QR_CODE" -> s(StringKey.MFA_METHOD_QR_CODE)
    "HARDWARE_KEY" -> s(StringKey.MFA_METHOD_HARDWARE_KEY)
    "NFC_DOCUMENT" -> s(StringKey.MFA_METHOD_NFC)
    else -> methodType
}
