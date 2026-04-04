package com.fivucsas.shared.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import com.fivucsas.shared.domain.model.VerificationStepResult
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.VerificationViewModel
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import kotlin.math.roundToInt

@Composable
fun VerificationSessionDetailScreen(
    viewModel: VerificationViewModel,
    sessionId: String,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSessionDetail(sessionId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = { viewModel.loadSessionDetail(sessionId) }) {
                    Text(s(StringKey.RETRY))
                }
            }
        }
        return
    }

    val session = uiState.selectedSession ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            SessionHeader(session = session)
        }

        // Divider
        item {
            HorizontalDivider()
        }

        // Steps header
        item {
            Text(
                text = s(StringKey.VERIFICATION_STEPS),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Step results
        items(session.steps.sortedBy { it.stepOrder }) { step ->
            StepResultCard(step = step, stepIndex = step.stepOrder)
        }
    }
}

@Composable
private fun SessionHeader(session: VerificationSession) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = session.flowName.ifBlank { s(StringKey.VERIFICATION_SESSION) },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            StatusBadge(
                text = session.status.uppercase().replace("_", " "),
                isPositive = session.status == "completed"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        InfoLine(label = s(StringKey.VERIFICATION_SESSION_ID), value = session.id)
        InfoLine(label = s(StringKey.VERIFICATION_USER_ID), value = session.userId)
        InfoLine(label = s(StringKey.VERIFICATION_STARTED), value = session.startedAt.ifBlank { "-" })
        if (session.completedAt != null) {
            InfoLine(label = s(StringKey.VERIFICATION_COMPLETED_AT), value = session.completedAt)
        }
        if (session.verificationLevel != null) {
            InfoLine(label = s(StringKey.VERIFICATION_LEVEL), value = session.verificationLevel)
        }
        InfoLine(
            label = s(StringKey.VERIFICATION_PROGRESS),
            value = "${session.currentStep} / ${session.totalSteps}"
        )
    }
}

@Composable
private fun StepResultCard(step: VerificationStepResult, stepIndex: Int) {
    val containerColor = when (step.status) {
        "completed" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        "failed" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        "skipped" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Step number circle
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stepIndex + 1}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = formatStepType(step.stepType),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                StepStatusBadge(status = step.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Confidence score
            if (step.confidenceScore != null) {
                val percent = (step.confidenceScore * 100).roundToInt()
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = "${s(StringKey.VERIFICATION_CONFIDENCE)}: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (percent >= 80)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Completed timestamp
            if (step.completedAt != null) {
                InfoLine(
                    label = s(StringKey.VERIFICATION_COMPLETED_AT),
                    value = step.completedAt
                )
            }

            // Failure reason
            if (step.failureReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.failureReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StepStatusBadge(status: String) {
    val text = when (status) {
        "pending" -> s(StringKey.VERIFICATION_STATUS_PENDING)
        "completed" -> s(StringKey.VERIFICATION_STATUS_COMPLETED)
        "failed" -> s(StringKey.VERIFICATION_STATUS_FAILED)
        "skipped" -> s(StringKey.VERIFICATION_STEP_SKIPPED)
        else -> status.uppercase()
    }
    StatusBadge(text = text, isPositive = status == "completed")
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatStepType(stepType: String): String {
    return stepType
        .replace("_", " ")
        .lowercase()
        .replaceFirstChar { it.uppercase() }
}
