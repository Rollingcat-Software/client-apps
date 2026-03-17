package com.fivucsas.desktop.ui.admin

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.data.DesktopCameraService
import com.fivucsas.desktop.ui.components.CameraPreview
import com.fivucsas.desktop.ui.admin.tabs.AnalyticsTab
import com.fivucsas.desktop.ui.admin.tabs.SettingsTab
import com.fivucsas.desktop.ui.admin.tabs.UsersTab
import com.fivucsas.desktop.ui.components.DesktopAppShell
import com.fivucsas.desktop.ui.components.DesktopBannerType
import com.fivucsas.desktop.ui.components.DesktopInfoBanner
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.desktop.ui.components.DesktopTable
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.presentation.viewmodel.IdentifyViewModel
import com.fivucsas.shared.domain.model.InviteStatus
import com.fivucsas.shared.domain.model.TenantHistoryEntry
import com.fivucsas.shared.presentation.viewmodel.InviteViewModel
import org.koin.compose.koinInject

// ─── Standalone Users Management Screen ──────────────────────────────────────

@Composable
fun AdminDesktopUsersScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: AdminViewModel = koinInject()
    DesktopAppShell(
        title = "Users Management",
        onBack = onBack,
        onLogout = onLogout
    ) {
        UsersTab(viewModel = viewModel)
    }
}

// ─── Standalone Analytics Screen ─────────────────────────────────────────────

@Composable
fun AdminDesktopAnalyticsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: AdminViewModel = koinInject()
    DesktopAppShell(
        title = "Analytics",
        onBack = onBack,
        onLogout = onLogout
    ) {
        AnalyticsTab(viewModel = viewModel)
    }
}

// ─── Standalone Settings Screen ──────────────────────────────────────────────

@Composable
fun AdminDesktopSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: AdminViewModel = koinInject()
    DesktopAppShell(
        title = "Tenant Settings",
        onBack = onBack,
        onLogout = onLogout
    ) {
        SettingsTab(viewModel = viewModel)
    }
}

// ─── Tenant History Screen ──────────────────────────────────────────────────

@Composable
fun AdminDesktopTenantHistoryScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val filters = listOf("all" to "All", "verification" to "Verifications", "enrollment" to "Enrollments", "admin" to "Admin Actions")
    var selectedFilter by remember { mutableStateOf("all") }

    val entries = listOf(
        TenantHistoryEntry("Verification", "Jane Doe", "Confidence: 94%", "2026-02-25 10:30", true),
        TenantHistoryEntry("Verification", "John Smith", "Confidence: 91%", "2026-02-25 09:15", true),
        TenantHistoryEntry("Enrollment", "Alice Johnson", "Quality: 88%", "2026-02-24 14:00", true),
        TenantHistoryEntry("Verification", "Bob Wilson", "Low confidence: 62%", "2026-02-24 15:14", false),
        TenantHistoryEntry("Admin Action", "Admin User", "User role updated", "2026-02-23 11:00", true),
        TenantHistoryEntry("Enrollment", "Charlie Brown", "Quality: 92%", "2026-02-22 09:30", true),
        TenantHistoryEntry("Admin Action", "Admin User", "Tenant settings updated", "2026-02-21 16:45", true)
    )

    val filteredEntries = if (selectedFilter == "all") entries
    else entries.filter {
        when (selectedFilter) {
            "verification" -> it.action == "Verification"
            "enrollment" -> it.action == "Enrollment"
            "admin" -> it.action == "Admin Action"
            else -> true
        }
    }

    DesktopAppShell(
        title = "Tenant History",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = "Tenant History",
                subtitle = "All tenant activity and audit logs"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filters.forEach { (value, label) ->
                        if (selectedFilter == value) {
                            Button(onClick = { selectedFilter = value }) { Text(label) }
                        } else {
                            OutlinedButton(onClick = { selectedFilter = value }) { Text(label) }
                        }
                    }
                }
                OutlinedButton(onClick = { /* Export placeholder */ }) {
                    Text("Export CSV")
                }
            }

            DesktopTable(
                title = "Activity Log",
                subtitle = "${filteredEntries.size} entries"
            ) {
                // Table header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Action", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.labelLarge)
                    Text("User", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.labelLarge)
                    Text("Detail", modifier = Modifier.weight(0.3f), style = MaterialTheme.typography.labelLarge)
                    Text("Timestamp", modifier = Modifier.weight(0.2f), style = MaterialTheme.typography.labelLarge)
                    Text("Status", modifier = Modifier.weight(0.1f), style = MaterialTheme.typography.labelLarge)
                }

                filteredEntries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(entry.action, modifier = Modifier.weight(0.2f))
                        Text(
                            entry.user,
                            modifier = Modifier.weight(0.2f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            entry.detail,
                            modifier = Modifier.weight(0.3f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            entry.timestamp,
                            modifier = Modifier.weight(0.2f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (entry.isSuccess) "OK" else "FAIL",
                            modifier = Modifier.weight(0.1f),
                            color = if (entry.isSuccess) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── Identify Tenant Screen ─────────────────────────────────────────────────

@Composable
fun AdminDesktopIdentifyTenantScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: IdentifyViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val cameraService = remember { DesktopCameraService() }
    var showCamera by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.clearState() }

    DesktopAppShell(
        title = "Identify (1:N)",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = "1:N Face Identification",
                subtitle = "Capture a face to search all enrolled users in this tenant"
            )

            if (!showCamera && !state.isSuccess && state.errorMessage == null) {
                // Idle state: show instructions and start button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Position the subject's face in front of the camera and click Start Identification.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showCamera = true }) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Identification")
                }
            }

            if (showCamera && !state.isSuccess && state.errorMessage == null) {
                CameraPreview(
                    cameraService = cameraService,
                    onCapture = { imageBytes ->
                        showCamera = false
                        viewModel.identifyFace(imageBytes)
                    },
                    onClose = { showCamera = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Loading state
            if (state.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Identifying...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // Error state
            if (state.errorMessage != null) {
                DesktopInfoBanner(
                    type = DesktopBannerType.Error,
                    text = state.errorMessage!!
                )
                OutlinedButton(onClick = {
                    viewModel.clearState()
                    showCamera = false
                }) {
                    Text("Try Again")
                }
            }

            // Success state
            if (state.isSuccess && state.result != null) {
                val result = state.result!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isMatch) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (result.isMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (result.isMatch) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (result.isMatch) "Match Found" else "No Match",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (result.isMatch) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Text(result.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Confidence", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Text("${(result.confidence * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("User ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                    Text(result.userId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = {
                                viewModel.clearState()
                                showCamera = false
                            }) {
                                Text("Scan Again")
                            }
                            Button(onClick = onBack) {
                                Text("Done")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Exam Entry Screen (NFC Simulation - desktop lacks NFC hardware) ─────────

private enum class ExamEntryPhase { IDLE, SCANNING, RESULT }

@Composable
fun AdminDesktopExamEntryScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var phase by remember { mutableStateOf(ExamEntryPhase.IDLE) }
    var scanSuccess by remember { mutableStateOf(false) }

    DesktopAppShell(
        title = "Exam Entry",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = "NFC Exam Entry",
                subtitle = "Verify student entry eligibility via NFC card scan"
            )

            when (phase) {
                ExamEntryPhase.IDLE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Ready to Scan",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the student card on the NFC reader to verify exam entry eligibility.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    DesktopInfoBanner(
                        type = DesktopBannerType.Warning,
                        text = "NFC hardware not available on desktop. NFC features require compatible hardware."
                    )

                    Button(onClick = { phase = ExamEntryPhase.SCANNING }) {
                        Text("Start NFC Scan")
                    }
                }

                ExamEntryPhase.SCANNING -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scanning...",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Hold the student card near the NFC reader.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Text("Simulation Controls", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { scanSuccess = true; phase = ExamEntryPhase.RESULT }) {
                            Text("Simulate OK")
                        }
                        OutlinedButton(onClick = { scanSuccess = false; phase = ExamEntryPhase.RESULT }) {
                            Text("Simulate Fail")
                        }
                    }
                }

                ExamEntryPhase.RESULT -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (scanSuccess) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (scanSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (scanSuccess) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (scanSuccess) "Entry Approved" else "Entry Denied",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (scanSuccess) "Student verified. You may enter the examination hall."
                                else "Card not recognized or exam not scheduled. Contact administration.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (scanSuccess) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { phase = ExamEntryPhase.IDLE }) {
                                    Text("Retry")
                                }
                                Button(onClick = onBack) {
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Admin Invite Management Screen ─────────────────────────────────────────

@Composable
fun AdminDesktopInviteManagementScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: InviteViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("TENANT_MEMBER") }
    val roleOptions = listOf("TENANT_MEMBER", "TENANT_ADMIN")

    LaunchedEffect(Unit) { viewModel.loadInvites() }

    DesktopAppShell(
        title = "Invite Management",
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DesktopSectionHeader(
                title = "Invite Management",
                subtitle = "Send invitations and manage existing invites for your tenant"
            )

            // Search + Send
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    label = { Text("Search email") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { viewModel.showCreateDialog() }) { Text("Send Invite") }
            }

            // Status filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.setFilter(null) },
                    label = { Text("ALL") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (state.selectedFilter == null) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                InviteStatus.entries.forEach { status ->
                    AssistChip(
                        onClick = { viewModel.setFilter(status) },
                        label = { Text(status.name) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedFilter == status) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            // Success/Error messages
            state.successMessage?.let {
                DesktopInfoBanner(type = DesktopBannerType.Info, text = it)
            }
            state.errorMessage?.let {
                DesktopInfoBanner(type = DesktopBannerType.Error, text = it)
            }

            // Invitations table
            DesktopTable(
                title = "Sent Invitations",
                subtitle = "${state.filteredInvites.size} invitations"
            ) {
                state.filteredInvites.forEach { invite ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(invite.email, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Role: ${invite.role}  |  Expires: ${invite.expiresAt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(invite.status.name, modifier = Modifier.width(95.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (invite.status == InviteStatus.PENDING) {
                                OutlinedButton(onClick = { viewModel.revokeInvite(invite.id) }) { Text("Revoke") }
                            }
                            OutlinedButton(
                                onClick = {
                                    viewModel.createInvite(
                                        email = invite.email,
                                        role = invite.role,
                                        tenantId = invite.tenantId,
                                        tenantName = invite.tenantName
                                    )
                                }
                            ) { Text("Resend") }
                        }
                    }
                }
            }
        }
    }

    // Create invite dialog
    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateDialog() },
            title = { Text("Send Invitation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("Email") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roleOptions.forEach { role ->
                            AssistChip(
                                onClick = { inviteRole = role },
                                label = { Text(role) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (inviteRole == role) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inviteEmail.isNotBlank()) {
                            viewModel.createInvite(
                                email = inviteEmail.trim(),
                                role = inviteRole
                            )
                            inviteEmail = ""
                            inviteRole = "TENANT_MEMBER"
                        }
                    }
                ) { Text("Send") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCreateDialog() }) { Text("Cancel") }
            }
        )
    }
}
