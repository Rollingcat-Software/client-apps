package com.fivucsas.shared.ui.screen

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.domain.model.IdentityMembership
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.AccountLinkingViewModel

/**
 * Linked-accounts section + inline workspace switcher — the mobile port of
 * the web `LinkedAccountsSection` + `AccountSwitcher`. Lists the person's
 * verified emails and tenant memberships, links another account
 * (initiate → OTP + password → confirm), unlinks a membership, and
 * switches the active membership in-session.
 *
 * @param onSwitched invoked after a successful membership switch so the host
 *        can reset app context (re-navigate to the post-login home). The new
 *        login-shaped tokens are already persisted by the ViewModel.
 */
@Composable
fun LinkedAccountsScreen(
    viewModel: AccountLinkingViewModel,
    onSwitched: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(state.switchSucceeded) {
        if (state.switchSucceeded) {
            viewModel.consumeSwitchSucceeded()
            onSwitched()
        }
    }

    if (state.showLinkDialog) {
        LinkAccountDialog(
            otpSent = state.linkOtpSent,
            inProgress = state.linkInProgress,
            error = state.linkError,
            onSendCode = { email -> viewModel.initiateLink(email) },
            onConfirm = { email, otp, password -> viewModel.confirmLink(email, otp, password) },
            onDismiss = { viewModel.hideLinkDialog() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            s(StringKey.LINKED_ACCOUNTS_TITLE),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            s(StringKey.LINKED_ACCOUNTS_DESCRIPTION),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }
            state.errorMessage != null && state.identity == null -> {
                Text(
                    state.errorMessage ?: s(StringKey.LINKED_ACCOUNTS_LOAD_ERROR),
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {
                state.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                // Emails
                Text(
                    s(StringKey.LINKED_ACCOUNTS_EMAILS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                state.identity?.emails?.forEach { email ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(email.email, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (email.verified) s(StringKey.LINKED_ACCOUNTS_EMAIL_VERIFIED)
                                else s(StringKey.LINKED_ACCOUNTS_EMAIL_UNVERIFIED),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (email.verified) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Memberships + inline switcher
                Text(
                    s(StringKey.LINKED_ACCOUNTS_MEMBERSHIPS),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                state.memberships.forEach { membership ->
                    MembershipRow(
                        membership = membership,
                        canSwitch = state.canSwitch,
                        switching = state.switchingUserId == membership.userId,
                        unlinking = state.unlinkingUserId == membership.userId,
                        onSwitch = { viewModel.switchMembership(membership.userId) },
                        onUnlink = { viewModel.unlink(membership.userId) }
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.showLinkDialog() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(s(StringKey.LINKED_ACCOUNTS_LINK_BUTTON))
                }
            }
        }
    }
}

@Composable
private fun MembershipRow(
    membership: IdentityMembership,
    canSwitch: Boolean,
    switching: Boolean,
    unlinking: Boolean,
    onSwitch: () -> Unit,
    onUnlink: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        membership.tenantName ?: membership.tenantId ?: membership.userId,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    membership.role?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (membership.isActive) {
                    Text(
                        s(StringKey.LINKED_ACCOUNTS_ACTIVE),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canSwitch && !membership.isActive) {
                    Button(onClick = onSwitch, enabled = !switching) {
                        if (switching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(s(StringKey.ACCOUNT_SWITCHER_SWITCH))
                        }
                    }
                }
                OutlinedButton(onClick = onUnlink, enabled = !unlinking) {
                    if (unlinking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(s(StringKey.LINKED_ACCOUNTS_UNLINK))
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkAccountDialog(
    otpSent: Boolean,
    inProgress: Boolean,
    error: String?,
    onSendCode: (email: String) -> Unit,
    onConfirm: (email: String, otp: String, password: String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!inProgress) onDismiss() },
        title = { Text(s(StringKey.LINK_DIALOG_TITLE)) },
        text = {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(s(StringKey.LINK_DIALOG_EMAIL_LABEL)) },
                    enabled = !otpSent && !inProgress,
                    modifier = Modifier.fillMaxWidth()
                )
                if (otpSent) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text(s(StringKey.LINK_DIALOG_OTP_LABEL)) },
                        enabled = !inProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(s(StringKey.LINK_DIALOG_PASSWORD_LABEL)) },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !inProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (otpSent) {
                Button(
                    onClick = { onConfirm(email, otp, password) },
                    enabled = !inProgress && otp.isNotBlank() && password.isNotBlank()
                ) { Text(s(StringKey.LINK_DIALOG_CONFIRM)) }
            } else {
                Button(
                    onClick = { onSendCode(email) },
                    enabled = !inProgress && email.isNotBlank()
                ) { Text(s(StringKey.LINK_DIALOG_SEND_CODE)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inProgress) {
                Text(s(StringKey.LINK_DIALOG_CANCEL))
            }
        }
    )
}
