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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.theme.AppColors

private data class ReceivedInvite(
    val id: String,
    val tenantName: String,
    val invitedBy: String,
    val role: String,
    val receivedAt: String,
    val expiresAt: String,
    val status: ReceivedInviteStatus
)

private enum class ReceivedInviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInvitationsScreen(
    onNavigateBack: () -> Unit
) {
    val invites = remember {
        mutableStateListOf(
            ReceivedInvite("1", "Acme Corporation", "admin@acme.com", "TENANT_MEMBER", "2026-02-20", "2026-03-20", ReceivedInviteStatus.PENDING),
            ReceivedInvite("2", "Globex Inc.", "hr@globex.com", "TENANT_MEMBER", "2026-02-18", "2026-03-18", ReceivedInviteStatus.PENDING),
            ReceivedInvite("3", "Wayne Enterprises", "ops@wayne.com", "TENANT_MEMBER", "2026-01-15", "2026-02-15", ReceivedInviteStatus.ACCEPTED),
            ReceivedInvite("4", "Stark Industries", "tony@stark.com", "TENANT_ADMIN", "2026-01-05", "2026-02-05", ReceivedInviteStatus.EXPIRED)
        )
    }

    var successMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Invitations",
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
                .padding(horizontal = 16.dp)
        ) {
            successMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Success.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Success,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            val pending = invites.filter { it.status == ReceivedInviteStatus.PENDING }
            val past = invites.filter { it.status != ReceivedInviteStatus.PENDING }

            if (invites.isEmpty()) {
                EmptyInvitationsContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.OnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(pending, key = { it.id }) { invite ->
                            ReceivedInviteCard(
                                invite = invite,
                                onAccept = {
                                    val idx = invites.indexOfFirst { it.id == invite.id }
                                    if (idx >= 0) {
                                        invites[idx] = invite.copy(status = ReceivedInviteStatus.ACCEPTED)
                                        successMessage = "Joined ${invite.tenantName}"
                                    }
                                },
                                onDecline = {
                                    val idx = invites.indexOfFirst { it.id == invite.id }
                                    if (idx >= 0) {
                                        invites[idx] = invite.copy(status = ReceivedInviteStatus.DECLINED)
                                        successMessage = "Invitation declined"
                                    }
                                }
                            )
                        }
                    }

                    if (past.isNotEmpty()) {
                        item {
                            Text(
                                text = "Past",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.OnSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(past, key = { it.id }) { invite ->
                            ReceivedInviteCard(
                                invite = invite,
                                onAccept = null,
                                onDecline = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceivedInviteCard(
    invite: ReceivedInvite,
    onAccept: (() -> Unit)?,
    onDecline: (() -> Unit)?
) {
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
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invite.tenantName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "From: ${invite.invitedBy}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
                StatusBadge(
                    text = invite.status.name,
                    type = when (invite.status) {
                        ReceivedInviteStatus.PENDING -> StatusBadgeType.Warning
                        ReceivedInviteStatus.ACCEPTED -> StatusBadgeType.Success
                        ReceivedInviteStatus.DECLINED -> StatusBadgeType.Failure
                        ReceivedInviteStatus.EXPIRED -> StatusBadgeType.Neutral
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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
                Text(
                    text = "Role: ${invite.role.replace("TENANT_", "")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.OnSurfaceVariant
                )
            }

            if (invite.status == ReceivedInviteStatus.PENDING && onAccept != null && onDecline != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline")
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyInvitationsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            tint = AppColors.OnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Invitations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "You have no pending or past invitations.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
