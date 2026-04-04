package com.fivucsas.shared.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.shared.domain.model.OAuth2Client
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.presentation.viewmodel.DeveloperPortalViewModel
import com.fivucsas.shared.ui.components.atoms.StatusBadge
import com.fivucsas.shared.ui.components.organisms.EmptyState

@Composable
fun DeveloperPortalScreen(
    viewModel: DeveloperPortalViewModel,
    onCopyToClipboard: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadApps()
    }

    // Auto-clear copied label after 2 seconds
    LaunchedEffect(uiState.copiedLabel) {
        if (uiState.copiedLabel != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.setCopiedLabel(null)
        }
    }

    // --- Register Dialog ---
    if (uiState.showRegisterDialog) {
        RegisterAppDialog(
            appName = uiState.registerAppName,
            redirectUris = uiState.registerRedirectUris,
            selectedScopes = uiState.registerScopes,
            isLoading = uiState.isRegistering,
            onAppNameChange = { viewModel.updateAppName(it) },
            onRedirectUrisChange = { viewModel.updateRedirectUris(it) },
            onToggleScope = { viewModel.toggleScope(it) },
            onConfirm = { viewModel.registerApp() },
            onDismiss = { viewModel.hideRegisterDialog() }
        )
    }

    // --- Credentials Reveal Dialog ---
    if (uiState.createdApp != null) {
        CredentialsDialog(
            app = uiState.createdApp!!,
            copiedLabel = uiState.copiedLabel,
            onCopy = { text, label ->
                onCopyToClipboard(text)
                viewModel.setCopiedLabel(label)
            },
            onDismiss = { viewModel.dismissCredentials() }
        )
    }

    // --- Delete Dialog ---
    if (uiState.showDeleteDialog && uiState.appToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text(s(StringKey.DEV_PORTAL_DELETE_APP)) },
            text = {
                Text(s(StringKey.DEV_PORTAL_DELETE_CONFIRM, uiState.appToDelete!!.appName))
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDelete() },
                    enabled = !uiState.isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text(s(StringKey.DELETE))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text(s(StringKey.CANCEL))
                }
            }
        )
    }

    // --- Main Content ---
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = s(StringKey.DEV_PORTAL_TITLE),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s(StringKey.DEV_PORTAL_SUBTITLE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { viewModel.showRegisterDialog() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(s(StringKey.DEV_PORTAL_REGISTER_APP))
                }
            }
        }

        // Error message
        if (uiState.errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Success message
        if (uiState.successMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Loading
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Empty state
        if (!uiState.isLoading && uiState.apps.isEmpty() && uiState.errorMessage == null) {
            item {
                EmptyState(
                    title = s(StringKey.DEV_PORTAL_NO_APPS),
                    message = s(StringKey.DEV_PORTAL_NO_APPS_HINT),
                    icon = Icons.Filled.Code,
                    actionText = s(StringKey.DEV_PORTAL_REGISTER_APP),
                    onActionClick = { viewModel.showRegisterDialog() }
                )
            }
        }

        // App cards
        if (!uiState.isLoading && uiState.apps.isNotEmpty()) {
            items(uiState.apps) { app ->
                OAuth2AppCard(
                    app = app,
                    copiedLabel = uiState.copiedLabel,
                    onCopyClientId = {
                        onCopyToClipboard(app.clientId)
                        viewModel.setCopiedLabel(app.clientId)
                    },
                    onDelete = { viewModel.showDeleteDialog(app) }
                )
            }
        }

        // Quick Start Guide
        item {
            QuickStartGuide(
                copiedLabel = uiState.copiedLabel,
                onCopy = { text, label ->
                    onCopyToClipboard(text)
                    viewModel.setCopiedLabel(label)
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// OAuth2 App Card
// ---------------------------------------------------------------------------

@Composable
private fun OAuth2AppCard(
    app: OAuth2Client,
    copiedLabel: String?,
    onCopyClientId: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = app.redirectUris.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                AppStatusBadge(status = app.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Client ID row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${s(StringKey.DEV_PORTAL_CLIENT_ID)}: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = maskClientId(app.clientId),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onCopyClientId,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = s(StringKey.DEV_PORTAL_COPY_CLIENT_ID),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (copiedLabel == app.clientId) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = s(StringKey.DEV_PORTAL_COPIED),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scopes
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                app.scopes.forEach { scope ->
                    ScopeChip(scope)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: created date + delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${s(StringKey.DEV_PORTAL_CREATED)}: ${formatDate(app.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = s(StringKey.DELETE),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Register Dialog
// ---------------------------------------------------------------------------

@Composable
private fun RegisterAppDialog(
    appName: String,
    redirectUris: String,
    selectedScopes: List<String>,
    isLoading: Boolean,
    onAppNameChange: (String) -> Unit,
    onRedirectUrisChange: (String) -> Unit,
    onToggleScope: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s(StringKey.DEV_PORTAL_REGISTER_APP)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = appName,
                    onValueChange = onAppNameChange,
                    label = { Text(s(StringKey.DEV_PORTAL_APP_NAME)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = redirectUris,
                    onValueChange = onRedirectUrisChange,
                    label = { Text(s(StringKey.DEV_PORTAL_REDIRECT_URIS)) },
                    placeholder = { Text("https://yourapp.com/callback") },
                    supportingText = { Text(s(StringKey.DEV_PORTAL_REDIRECT_URIS_HELPER)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = s(StringKey.DEV_PORTAL_ALLOWED_SCOPES),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                DeveloperPortalViewModel.AVAILABLE_SCOPES.forEach { scope ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = scope in selectedScopes,
                            onCheckedChange = { onToggleScope(scope) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(scope, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = appName.isNotBlank() && redirectUris.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(s(StringKey.DEV_PORTAL_REGISTER))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(s(StringKey.CANCEL))
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Credentials Dialog
// ---------------------------------------------------------------------------

@Composable
private fun CredentialsDialog(
    app: OAuth2Client,
    copiedLabel: String?,
    onCopy: (text: String, label: String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = { Text(s(StringKey.DEV_PORTAL_APP_CREATED)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Warning
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = s(StringKey.DEV_PORTAL_SECRET_WARNING),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // Client ID
                Text(
                    text = s(StringKey.DEV_PORTAL_CLIENT_ID),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CredentialRow(
                    value = app.clientId,
                    isCopied = copiedLabel == "cred-id",
                    onCopy = { onCopy(app.clientId, "cred-id") }
                )

                // Client Secret
                if (app.clientSecret != null) {
                    Text(
                        text = s(StringKey.DEV_PORTAL_CLIENT_SECRET),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CredentialRow(
                        value = app.clientSecret,
                        isCopied = copiedLabel == "cred-secret",
                        onCopy = { onCopy(app.clientSecret, "cred-secret") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(s(StringKey.DEV_PORTAL_DONE))
            }
        }
    )
}

@Composable
private fun CredentialRow(
    value: String,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = s(StringKey.DEV_PORTAL_COPY),
                modifier = Modifier.size(14.dp)
            )
        }
        if (isCopied) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = s(StringKey.DEV_PORTAL_COPIED),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Start Guide
// ---------------------------------------------------------------------------

@Composable
private fun QuickStartGuide(
    copiedLabel: String?,
    onCopy: (text: String, label: String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = s(StringKey.DEV_PORTAL_QUICK_START),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = s(StringKey.DEV_PORTAL_QUICK_START_DESC),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1
            QuickStartStep(
                stepNumber = "1",
                title = s(StringKey.DEV_PORTAL_STEP1_TITLE),
                description = s(StringKey.DEV_PORTAL_STEP1_DESC)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2
            QuickStartStep(
                stepNumber = "2",
                title = s(StringKey.DEV_PORTAL_STEP2_TITLE),
                description = s(StringKey.DEV_PORTAL_STEP2_DESC)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippetBlock(
                code = SCRIPT_SNIPPET,
                label = "script",
                copiedLabel = copiedLabel,
                onCopy = onCopy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3
            QuickStartStep(
                stepNumber = "3",
                title = s(StringKey.DEV_PORTAL_STEP3_TITLE),
                description = s(StringKey.DEV_PORTAL_STEP3_DESC)
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippetBlock(
                code = CALLBACK_SNIPPET,
                label = "callback",
                copiedLabel = copiedLabel,
                onCopy = onCopy
            )
        }
    }
}

@Composable
private fun QuickStartStep(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CodeSnippetBlock(
    code: String,
    label: String,
    copiedLabel: String?,
    onCopy: (text: String, label: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { onCopy(code, label) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = s(StringKey.DEV_PORTAL_COPY),
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            SelectionContainer {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.inverseOnSurface
                )
            }
            if (copiedLabel == label) {
                Text(
                    text = s(StringKey.DEV_PORTAL_COPIED_TO_CLIPBOARD),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

@Composable
private fun ScopeChip(scope: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = scope,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun AppStatusBadge(status: String) {
    val isPositive = status == "ACTIVE"
    val displayText = if (isPositive) s(StringKey.DEV_PORTAL_STATUS_ACTIVE) else s(StringKey.DEV_PORTAL_STATUS_INACTIVE)
    StatusBadge(text = displayText, isPositive = isPositive)
}

private fun maskClientId(clientId: String): String {
    if (clientId.length <= 12) return clientId
    return clientId.substring(0, 8) + "..." + clientId.substring(clientId.length - 4)
}

private fun formatDate(iso: String): String {
    if (iso.isBlank()) return "-"
    // Simple date formatting: take the date portion (YYYY-MM-DD)
    return if (iso.length >= 10) iso.substring(0, 10) else iso
}

// ---------------------------------------------------------------------------
// Code Snippets
// ---------------------------------------------------------------------------

private const val SCRIPT_SNIPPET = """<script src="https://cdn.fivucsas.com/auth-widget.js"></script>
<script>
  FivucsasAuth.init({
    clientId: 'YOUR_CLIENT_ID',
    redirectUri: 'https://yourapp.com/callback',
    scopes: ['openid', 'profile', 'email'],
  });
</script>"""

private const val CALLBACK_SNIPPET = """// Handle the OAuth2 callback
const params = new URLSearchParams(window.location.search);
const code = params.get('code');

if (code) {
  const response = await fetch(
    'https://auth.rollingcatsoftware.com/api/v1/oauth2/token',
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        code,
        client_id: 'YOUR_CLIENT_ID',
        client_secret: 'YOUR_CLIENT_SECRET',
        redirect_uri: 'https://yourapp.com/callback',
      }),
    }
  );
  const { access_token, id_token } = await response.json();
}"""
