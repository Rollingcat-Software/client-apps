package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.ui.theme.AppColors

private enum class ExamEntryPhase { IDLE, SCANNING, RESULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamEntryScreen(
    onNavigateBack: () -> Unit
) {
    var phase by remember { mutableStateOf(ExamEntryPhase.IDLE) }
    var scanSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Exam Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (phase) {
                ExamEntryPhase.IDLE -> ExamEntryIdleContent(
                    onStartScan = { phase = ExamEntryPhase.SCANNING }
                )
                ExamEntryPhase.SCANNING -> ExamEntryScanningContent(
                    onSimulateSuccess = {
                        scanSuccess = true
                        phase = ExamEntryPhase.RESULT
                    },
                    onSimulateFailure = {
                        scanSuccess = false
                        phase = ExamEntryPhase.RESULT
                    }
                )
                ExamEntryPhase.RESULT -> ExamEntryResultContent(
                    success = scanSuccess,
                    onRetry = { phase = ExamEntryPhase.IDLE },
                    onDone = onNavigateBack
                )
            }
        }
    }
}

@Composable
private fun ExamEntryIdleContent(onStartScan: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Nfc,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(96.dp)
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "NFC Exam Entry",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Tap your student card on the device to verify exam entry eligibility.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Warning.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "NFC hardware not available on this device. NFC features require compatible hardware.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.OnSurface,
            modifier = Modifier.padding(12.dp),
            textAlign = TextAlign.Center
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onStartScan,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Nfc, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Start NFC Scan", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ExamEntryScanningContent(
    onSimulateSuccess: () -> Unit,
    onSimulateFailure: () -> Unit
) {
    CircularProgressIndicator(
        modifier = Modifier.size(80.dp),
        strokeWidth = 6.dp
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "Scanning...",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Hold your card near the NFC reader.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Simulation Controls",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSimulateSuccess,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simulate OK")
        }
        OutlinedButton(
            onClick = onSimulateFailure,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simulate Fail")
        }
    }
}

@Composable
private fun ExamEntryResultContent(
    success: Boolean,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (success)
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
                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (success)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (success) "Entry Approved" else "Entry Denied",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (success)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (success)
                    "Student verified. You may enter the examination hall."
                else
                    "Card not recognized or exam not scheduled. Contact administration.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = if (success)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
                Button(
                    onClick = onDone,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Done")
                }
            }
        }
    }
}
