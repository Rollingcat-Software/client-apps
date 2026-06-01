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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.TenantSettingsViewModel
import com.fivucsas.shared.ui.components.molecules.ExpandableCard
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.util.disposeOnLeave
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TenantSettingsViewModel = koinInject<TenantSettingsViewModel>().disposeOnLeave()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSettings() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.tenantName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = s(StringKey.TENANTSET_TITLE),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.A11Y_NAVIGATE_BACK))
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
                .padding(UIDimens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            state.errorMessage?.let { ErrorMessage(message = it) }
            state.successMessage?.let { SuccessMessage(message = it) }

            // Biometric Policies
            ExpandableCard(
                title = s(StringKey.TENANTSET_BIOMETRIC_POLICIES),
                subtitle = s(StringKey.TENANTSET_BIOMETRIC_POLICIES_SUB)
            ) {
                SettingsToggleRow(
                    icon = Icons.Default.Face,
                    label = s(StringKey.TENANTSET_LIVENESS_CHECK),
                    description = s(StringKey.TENANTSET_LIVENESS_CHECK_DESC),
                    checked = state.livenessCheckEnabled,
                    onCheckedChange = { viewModel.setLivenessCheck(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = s(StringKey.TENANTSET_CONFIDENCE_THRESHOLD, (state.confidenceThreshold * 100).toInt()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = state.confidenceThreshold,
                    onValueChange = { viewModel.setConfidenceThreshold(it) },
                    valueRange = 0.5f..1f,
                    steps = 9
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = s(StringKey.TENANTSET_MAX_ENROLLMENT_ATTEMPTS, state.maxEnrollmentAttempts),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = state.maxEnrollmentAttempts.toFloat(),
                    onValueChange = { viewModel.setMaxEnrollmentAttempts(it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }

            // Security
            ExpandableCard(
                title = s(StringKey.TENANTSET_SECURITY),
                subtitle = s(StringKey.TENANTSET_SECURITY_SUB)
            ) {
                SettingsToggleRow(
                    icon = Icons.Default.Lock,
                    label = s(StringKey.TENANTSET_AUTO_LOCK),
                    description = s(StringKey.TENANTSET_AUTO_LOCK_DESC),
                    checked = state.autoLockEnabled,
                    onCheckedChange = { viewModel.setAutoLock(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = s(StringKey.TENANTSET_SESSION_TIMEOUT, state.sessionTimeoutMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = state.sessionTimeoutMinutes.toFloat(),
                    onValueChange = { viewModel.setSessionTimeout(it.toInt()) },
                    valueRange = 5f..120f,
                    steps = 22
                )
            }

            // NFC & Exam
            ExpandableCard(
                title = s(StringKey.TENANTSET_NFC_EXAM),
                subtitle = s(StringKey.TENANTSET_NFC_EXAM_SUB)
            ) {
                SettingsToggleRow(
                    icon = Icons.Default.Nfc,
                    label = s(StringKey.TENANTSET_NFC_EXAM_ENTRY),
                    description = s(StringKey.TENANTSET_NFC_EXAM_ENTRY_DESC),
                    checked = state.nfcExamEntryEnabled,
                    onCheckedChange = { viewModel.setNfcExamEntry(it) }
                )
            }

            // Invitations
            ExpandableCard(
                title = s(StringKey.TENANTSET_INVITATIONS),
                subtitle = s(StringKey.TENANTSET_INVITATIONS_SUB)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = AppColors.Primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s(StringKey.TENANTSET_INVITE_EXPIRY, state.inviteExpiryDays),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Slider(
                    value = state.inviteExpiryDays.toFloat(),
                    onValueChange = { viewModel.setInviteExpiryDays(it.toInt()) },
                    valueRange = 1f..90f,
                    steps = 88
                )
            }

            // Save button
            if (state.hasUnsavedChanges) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(s(StringKey.TENANTSET_SAVE_CHANGES))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.Primary
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.OnSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
