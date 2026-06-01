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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.ReceivedInvite
import com.fivucsas.shared.domain.model.ReceivedInviteStatus
import com.fivucsas.shared.domain.usecase.invite.GetReceivedInvitesUseCase
import com.fivucsas.shared.domain.usecase.invite.InviteResponse
import com.fivucsas.shared.domain.usecase.invite.RespondToInviteUseCase
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyInvitationsScreen(
    onNavigateBack: () -> Unit,
    getReceivedInvitesUseCase: GetReceivedInvitesUseCase = koinInject(),
    respondToInviteUseCase: RespondToInviteUseCase = koinInject()
) {
    // Received invitations are loaded from GET /invites/received on entry.
    var invites by remember { mutableStateOf<List<ReceivedInvite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var actionInProgressId by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        getReceivedInvitesUseCase().fold(
            onSuccess = {
                invites = it
                errorMessage = null
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = error.message ?: s(StringKey.MYINV_LOAD_FAILED)
                isLoading = false
            }
        )
    }

    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        s(StringKey.MYINV_TITLE),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.A11Y_NAVIGATE_BACK))
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

            errorMessage?.let { msg ->
                ErrorMessage(
                    message = msg,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            val pending = invites.filter { it.status == ReceivedInviteStatus.PENDING }
            val past = invites.filter { it.status != ReceivedInviteStatus.PENDING }

            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (invites.isEmpty()) {
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
                                text = s(StringKey.MYINV_PENDING_SECTION),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.OnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(pending, key = { it.id }) { invite ->
                            ReceivedInviteCard(
                                invite = invite,
                                actionInProgress = actionInProgressId == invite.id,
                                onAccept = {
                                    // Real accept: PUT /invites/received/{id}/accept.
                                    errorMessage = null
                                    successMessage = null
                                    actionInProgressId = invite.id
                                    scope.launch {
                                        respondToInviteUseCase(invite.id, InviteResponse.ACCEPT).fold(
                                            onSuccess = {
                                                successMessage = s(StringKey.MYINV_JOINED, invite.tenantName)
                                                actionInProgressId = null
                                                reload()
                                            },
                                            onFailure = { error ->
                                                actionInProgressId = null
                                                errorMessage = error.message ?: s(StringKey.MYINV_ACCEPT_FAILED)
                                            }
                                        )
                                    }
                                },
                                onDecline = {
                                    // Real decline: PUT /invites/received/{id}/decline.
                                    errorMessage = null
                                    successMessage = null
                                    actionInProgressId = invite.id
                                    scope.launch {
                                        respondToInviteUseCase(invite.id, InviteResponse.DECLINE).fold(
                                            onSuccess = {
                                                successMessage = s(StringKey.MYINV_DECLINED)
                                                actionInProgressId = null
                                                reload()
                                            },
                                            onFailure = { error ->
                                                actionInProgressId = null
                                                errorMessage = error.message ?: s(StringKey.MYINV_DECLINE_FAILED)
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    }

                    if (past.isNotEmpty()) {
                        item {
                            Text(
                                text = s(StringKey.MYINV_PAST_SECTION),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.OnSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(past, key = { it.id }) { invite ->
                            ReceivedInviteCard(
                                invite = invite,
                                actionInProgress = false,
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
    actionInProgress: Boolean,
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
                        text = s(StringKey.MYINV_FROM_LABEL, invite.invitedBy),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
                StatusBadge(
                    text = when (invite.status) {
                        ReceivedInviteStatus.PENDING -> s(StringKey.MYINV_STATUS_PENDING)
                        ReceivedInviteStatus.ACCEPTED -> s(StringKey.MYINV_STATUS_ACCEPTED)
                        ReceivedInviteStatus.DECLINED -> s(StringKey.MYINV_STATUS_DECLINED)
                        ReceivedInviteStatus.EXPIRED -> s(StringKey.MYINV_STATUS_EXPIRED)
                    },
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
                        text = s(StringKey.MYINV_EXPIRES_LABEL, invite.expiresAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
                Text(
                    text = s(StringKey.MYINV_ROLE_LABEL, invite.role.replace("TENANT_", "")),
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
                        enabled = !actionInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s(StringKey.MYINV_DECLINE))
                    }
                    Button(
                        onClick = onAccept,
                        enabled = !actionInProgress,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s(StringKey.MYINV_ACCEPT))
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
            text = s(StringKey.MYINV_EMPTY_TITLE),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = s(StringKey.MYINV_EMPTY_BODY),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
