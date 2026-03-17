package com.fivucsas.desktop.ui.admin.tabs

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.admin.components.AdminConstants
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.AuthSession
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.viewmodel.SessionViewModel
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.organisms.EmptyState
import org.koin.compose.koinInject

/**
 * Security Tab Component
 *
 * Displays active sessions with ability to revoke them.
 *
 * @param viewModel Admin view model
 */
@Composable
fun SecurityTab(
    viewModel: AdminViewModel,
    sessionViewModel: SessionViewModel = koinInject()
) {
    val uiState by sessionViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        sessionViewModel.loadSessions()
    }

    // Revoke confirmation dialog
    if (uiState.showRevokeDialog && uiState.sessionToRevoke != null) {
        AlertDialog(
            onDismissRequest = { sessionViewModel.hideRevokeDialog() },
            title = { Text(s(StringKey.REVOKE_SESSION)) },
            text = { Text(s(StringKey.CONFIRM_REVOKE_SESSION)) },
            confirmButton = {
                Button(
                    onClick = { sessionViewModel.confirmRevoke() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(s(StringKey.CONFIRM))
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionViewModel.hideRevokeDialog() }) {
                    Text(s(StringKey.CANCEL))
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    s(StringKey.SECURITY_TITLE),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    s(StringKey.SECURITY_SUBTITLE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { sessionViewModel.loadSessions() },
                enabled = !uiState.isLoading
            ) {
                Text(s(StringKey.REFRESH))
            }
        }

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

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
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { sessionViewModel.loadSessions() }) {
                        Text(s(StringKey.RETRY))
                    }
                }
            }
        } else if (uiState.sessions.isEmpty()) {
            EmptyState(
                title = s(StringKey.NO_ACTIVE_SESSIONS),
                message = s(StringKey.NO_DATA)
            )
        } else {
            Text(
                text = "${s(StringKey.ACTIVE_SESSIONS)} (${uiState.sessions.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sessions) { session ->
                    SessionCard(
                        session = session,
                        onRevoke = { sessionViewModel.showRevokeDialog(session) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: AuthSession,
    onRevoke: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.deviceInfo.ifBlank { s(StringKey.UNKNOWN) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "${s(StringKey.SESSION_IP)}: ${session.ipAddress.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${s(StringKey.SESSION_LAST_ACTIVE)}: ${session.lastActiveAt.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = session.status,
                    isPositive = session.status == "ACTIVE"
                )
                OutlinedButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(s(StringKey.REVOKE_SESSION))
                }
            }
        }
    }
}
