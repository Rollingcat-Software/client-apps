package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.BiometricCapability
import com.fivucsas.shared.presentation.viewmodel.SecuritySettingsViewModel
import com.fivucsas.shared.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: SecuritySettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadCapability()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Biometric capability: ${capabilityLabel(state.capability)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Device registered: ${if (state.isRegistered) "Yes" else "No"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Step-up expires: ${state.lastStepUp?.expiresAt ?: "-"}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (state.isLoading) {
                CircularProgressIndicator()
            }

            Button(
                onClick = { scope.launch { viewModel.registerDevice(deviceLabel = "Android Device") } },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register this device")
            }

            Button(
                onClick = { scope.launch { viewModel.stepUp(reason = "Confirm with fingerprint") } },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Step-up")
            }

            state.lastActionMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.primary)
            }

            state.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This screen uses real step-up API (backend call).",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun capabilityLabel(capability: BiometricCapability?): String = when (capability) {
    BiometricCapability.Supported -> "Supported"
    BiometricCapability.NotEnrolled -> "Not enrolled"
    BiometricCapability.NoHardware -> "No hardware"
    BiometricCapability.Unsupported -> "Unsupported"
    BiometricCapability.UnknownError -> "Unknown error"
    null -> "Checking..."
}
