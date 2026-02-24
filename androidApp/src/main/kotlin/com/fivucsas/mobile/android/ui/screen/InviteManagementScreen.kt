package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.viewmodel.Invite
import com.fivucsas.shared.presentation.viewmodel.InviteStatus
import com.fivucsas.shared.presentation.viewmodel.InviteViewModel
import com.fivucsas.shared.ui.components.atoms.SearchTextField
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.FilterChipItem
import com.fivucsas.shared.ui.components.molecules.FilterChipRow
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.theme.AppColors
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: InviteViewModel = koinInject()
) {
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadInvites() }

    val filters = listOf(
        FilterChipItem("All", "all"),
        FilterChipItem("Pending", "PENDING"),
        FilterChipItem("Accepted", "ACCEPTED"),
        FilterChipItem("Expired", "EXPIRED"),
        FilterChipItem("Revoked", "REVOKED")
    )
    var selectedFilterValue by remember { mutableStateOf("all") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Invite Management",
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Create Invitation")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = UIDimens.SpacingMedium)
        ) {
            uiState.errorMessage?.let { ErrorMessage(message = it) }
            uiState.successMessage?.let { SuccessMessage(message = it) }

            SearchTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearch(it) },
                placeholder = "Search by email...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            FilterChipRow(
                items = filters,
                selectedValue = selectedFilterValue,
                onSelected = { chip ->
                    selectedFilterValue = chip.value
                    viewModel.setFilter(
                        if (chip.value == "all") null
                        else InviteStatus.valueOf(chip.value)
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredInvites) { invite ->
                    InviteCard(
                        invite = invite,
                        onRevoke = { viewModel.revokeInvite(invite.id) }
                    )
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateInviteDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onCreate = { email, role -> viewModel.createInvite(email, role) }
        )
    }
}

@Composable
private fun InviteCard(invite: Invite, onRevoke: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invite.email,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Role: ${invite.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
                StatusBadge(
                    text = invite.status.name,
                    type = when (invite.status) {
                        InviteStatus.PENDING -> StatusBadgeType.Warning
                        InviteStatus.ACCEPTED -> StatusBadgeType.Success
                        InviteStatus.EXPIRED -> StatusBadgeType.Neutral
                        InviteStatus.REVOKED -> StatusBadgeType.Failure
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expires: ${invite.expiresAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
                if (invite.status == InviteStatus.PENDING) {
                    OutlinedButton(
                        onClick = onRevoke,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Revoke", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateInviteDialog(
    onDismiss: () -> Unit,
    onCreate: (email: String, role: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("TENANT_MEMBER") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Invitation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("TENANT_MEMBER", "TENANT_ADMIN").forEach { r ->
                        val selected = role == r
                        if (selected) {
                            Button(onClick = {}) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(r.replace("TENANT_", ""), style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            OutlinedButton(onClick = { role = r }) {
                                Text(r.replace("TENANT_", ""), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (email.isNotBlank()) onCreate(email, role) },
                enabled = email.isNotBlank()
            ) {
                Text("Send Invitation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
