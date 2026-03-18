package com.fivucsas.shared.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.state.FingerprintUiState
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import kotlinx.coroutines.launch

@Composable
fun FingerprintGateScreen(
    viewModel: FingerprintViewModel,
    onStart: suspend () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onFailure: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        when (state) {
            is FingerprintUiState.Success -> onSuccess()
            is FingerprintUiState.Error -> onFailure()
            else -> Unit
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "fingerprintPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = s(StringKey.FINGERPRINT),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(96.dp)
                .scale(if (state is FingerprintUiState.ScanningBiometric) pulse else 1f)
                .alpha(if (state is FingerprintUiState.ScanningBiometric) 0.8f else 1f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = s(StringKey.FINGERPRINT_SECURITY_CHECK),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = s(StringKey.FINGERPRINT_SECURITY_CHECK_DESC),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        StateText(state = state)
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { scope.launch { onStart() } },
            enabled = state == FingerprintUiState.Idle || state is FingerprintUiState.Error,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    contentDescription = s(StringKey.FINGERPRINT_VERIFY_BUTTON)
                }
        ) {
            if (state is FingerprintUiState.RegisteringDevice ||
                state is FingerprintUiState.RequestingChallenge ||
                state is FingerprintUiState.ScanningBiometric ||
                state is FingerprintUiState.VerifyingSignature
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            }
            Text(
                if (state is FingerprintUiState.Error)
                    s(StringKey.FINGERPRINT_RETRY_BUTTON)
                else
                    s(StringKey.FINGERPRINT_VERIFY_BUTTON)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    contentDescription = s(StringKey.FINGERPRINT_SKIP)
                }
        ) {
            Text(s(StringKey.FINGERPRINT_SKIP))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    contentDescription = s(StringKey.BACK)
                }
        ) {
            Text(s(StringKey.BACK))
        }
    }
}

@Composable
private fun StateText(state: FingerprintUiState) {
    val text = when (state) {
        FingerprintUiState.Idle -> s(StringKey.FINGERPRINT_READY)
        FingerprintUiState.RegisteringDevice -> s(StringKey.FINGERPRINT_REGISTERING_DEVICE)
        FingerprintUiState.RequestingChallenge -> s(StringKey.FINGERPRINT_REQUESTING_CHALLENGE)
        FingerprintUiState.ScanningBiometric -> s(StringKey.FINGERPRINT_SCAN_NOW)
        FingerprintUiState.VerifyingSignature -> s(StringKey.FINGERPRINT_VERIFYING)
        is FingerprintUiState.Success -> s(StringKey.FINGERPRINT_VERIFIED)
        is FingerprintUiState.Error -> state.message
    }
    val color = if (state is FingerprintUiState.Error) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}
