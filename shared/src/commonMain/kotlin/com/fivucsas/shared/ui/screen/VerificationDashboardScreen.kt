package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.VerificationSession
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.VerificationViewModel
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.organisms.EmptyState

@Composable
fun VerificationDashboardScreen(
    viewModel: VerificationViewModel,
    onSessionClick: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = s(StringKey.VERIFICATION_DASHBOARD_TITLE),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = s(StringKey.VERIFICATION_DASHBOARD_SUBTITLE),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status filter chips
        StatusFilterRow(
            selectedStatus = uiState.statusFilter,
            onStatusSelected = { viewModel.filterByStatus(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.successMessage != null) {
            Text(
                text = uiState.successMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { viewModel.loadSessions(uiState.statusFilter) }) {
                        Text(s(StringKey.RETRY))
                    }
                }
            }
        } else if (uiState.sessions.isEmpty()) {
            EmptyState(
                title = s(StringKey.VERIFICATION_NO_SESSIONS),
                message = s(StringKey.NO_DATA)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sessions) { session ->
                    VerificationSessionCard(
                        session = session,
                        onClick = { onSessionClick(session.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterRow(
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    val statuses = listOf(
        null to s(StringKey.VERIFICATION_FILTER_ALL),
        "pending" to s(StringKey.VERIFICATION_STATUS_PENDING),
        "in_progress" to s(StringKey.VERIFICATION_STATUS_IN_PROGRESS),
        "completed" to s(StringKey.VERIFICATION_STATUS_COMPLETED),
        "failed" to s(StringKey.VERIFICATION_STATUS_FAILED)
    )

    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { (status, label) ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(status) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun VerificationSessionCard(
    session: VerificationSession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.flowName.ifBlank { session.flowId },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = session.id.take(8) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                VerificationStatusBadge(status = session.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            if (session.totalSteps > 0) {
                val progress = session.currentStep.toFloat() / session.totalSteps.toFloat()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(6.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${session.currentStep}/${session.totalSteps}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${s(StringKey.VERIFICATION_STARTED)}: ${session.startedAt.ifBlank { "-" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (session.verificationLevel != null) {
                    Text(
                        text = session.verificationLevel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun VerificationStatusBadge(status: String) {
    val isPositive = status == "completed"
    val displayText = when (status) {
        "pending" -> s(StringKey.VERIFICATION_STATUS_PENDING)
        "in_progress" -> s(StringKey.VERIFICATION_STATUS_IN_PROGRESS)
        "completed" -> s(StringKey.VERIFICATION_STATUS_COMPLETED)
        "failed" -> s(StringKey.VERIFICATION_STATUS_FAILED)
        "expired" -> s(StringKey.VERIFICATION_STATUS_EXPIRED)
        else -> status.uppercase()
    }
    StatusBadge(text = displayText, isPositive = isPositive)
}
