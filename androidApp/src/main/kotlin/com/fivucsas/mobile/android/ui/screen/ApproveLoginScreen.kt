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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.shared.domain.model.PendingApproveLogin
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.ApproveLoginViewModel
import com.fivucsas.shared.ui.theme.AppColors
import org.koin.compose.koinInject

/**
 * Approver-side screen for the no-Firebase, number-matching approve-login flow.
 *
 * The (already authenticated) user lands here to approve a sign-in started on
 * another device — e.g. logging into the web app. While visible, the screen
 * polls `GET /api/v1/auth/approve-login/pending` (via [ApproveLoginViewModel])
 * and shows each pending request's two-digit match number; the user matches it
 * with the number shown on the originating device and taps Allow (or Deny).
 *
 * Backed entirely by the shared KMP stack (PR #53): VM/state/repository/api are
 * platform-agnostic; this is the Android rendering only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproveLoginScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApproveLoginViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    // Poll only while the screen is on-screen. The VM is a Koin singleton, so we
    // start/stop polling rather than dispose() it (dispose would kill its scope
    // permanently for the next visit).
    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = s(StringKey.APPROVE_LOGIN_TITLE),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                    } else {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
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
            Text(
                text = s(StringKey.APPROVE_LOGIN_SUBTITLE),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.OnSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            state.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (state.pending.isEmpty()) {
                ApproveLoginEmptyContent()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.pending, key = { it.sessionId }) { request ->
                        ApproveLoginCard(
                            request = request,
                            inFlight = state.inFlightSessionId == request.sessionId,
                            // One decision at a time across the whole list.
                            actionsEnabled = state.inFlightSessionId == null,
                            onAllow = { viewModel.allow(request.sessionId, request.matchNumber) },
                            onDeny = { viewModel.deny(request.sessionId, request.matchNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApproveLoginCard(
    request: PendingApproveLogin,
    inFlight: Boolean,
    actionsEnabled: Boolean,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = s(StringKey.APPROVE_LOGIN_MATCH_NUMBER),
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.OnSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            // The match number is a zero-padded String (e.g. "07") — render it
            // verbatim in a monospace face so leading zeros are unambiguous.
            Text(
                text = request.matchNumber,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = AppColors.Primary
            )

            Spacer(Modifier.height(12.dp))

            request.initiatorUserAgent?.takeIf { it.isNotBlank() }?.let { ua ->
                InitiatorRow(icon = Icons.Default.PhoneAndroid, text = ua)
                Spacer(Modifier.height(4.dp))
            }
            request.initiatorIp?.takeIf { it.isNotBlank() }?.let { ip ->
                InitiatorRow(icon = Icons.Default.Info, text = ip)
            }

            Spacer(Modifier.height(16.dp))

            if (inFlight) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeny,
                        enabled = actionsEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s(StringKey.APPROVE_LOGIN_DENY))
                    }
                    Button(
                        onClick = onAllow,
                        enabled = actionsEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(s(StringKey.APPROVE_LOGIN_ALLOW))
                    }
                }
            }
        }
    }
}

@Composable
private fun InitiatorRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = AppColors.OnSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.OnSurfaceVariant,
            maxLines = 2
        )
    }
}

@Composable
private fun ApproveLoginEmptyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Login,
            contentDescription = null,
            tint = AppColors.OnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = s(StringKey.APPROVE_LOGIN_EMPTY_TITLE),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.OnSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = s(StringKey.APPROVE_LOGIN_EMPTY_BODY),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
