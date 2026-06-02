package com.fivucsas.authenticator.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.authenticator.storage.TotpVault
import com.fivucsas.authenticator.totp.TotpAlgorithm
import com.fivucsas.mobile.android.ui.screen.OtpQrScannerScreen
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import kotlinx.coroutines.launch

private fun groupDigits(code: String): String =
    if (code.length == 6) "${code.substring(0, 3)} ${code.substring(3)}"
    else if (code.length == 8) "${code.substring(0, 4)} ${code.substring(4)}"
    else code

private fun copyToClipboard(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("totp", text))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val vault = remember { TotpVault(context) }
    val viewModel = remember { AuthenticatorViewModel(vault) }
    val state by viewModel.state.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var showManualForm by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Full-screen overlay: while the scanner is active, replace the
    // authenticator list entirely (same overlay pattern used by the QR scanner).
    if (showQrScanner) {
        OtpQrScannerScreen(
            onAccepted = { uri ->
                val result = viewModel.addFromUri(uri)
                showQrScanner = false
                if (result.isFailure) {
                    // TODO(i18n): promote to StringKey.OTP_SCAN_UNSUPPORTED once string table is updated.
                    // Tracked in /tmp/i18n_agent_20E.txt (agent 20E).
                    Toast.makeText(
                        context,
                        "Unsupported QR code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onBack = { showQrScanner = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s(StringKey.AUTH_TITLE)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s(StringKey.BACK))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = s(StringKey.AUTH_ADD_ACCOUNT))
            }
        }
    ) { padding ->
        if (state.accounts.isEmpty()) {
            EmptyState(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.accounts, key = { it.account.id }) { item ->
                    AccountRow(
                        item = item,
                        onTap = {
                            copyToClipboard(context, item.code)
                            Toast.makeText(context, s(StringKey.AUTH_COPIED), Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { pendingDeleteId = item.account.id }
                    )
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = s(StringKey.AUTH_ADD_ACCOUNT),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                DropdownMenuItem(
                    text = { Text(s(StringKey.AUTH_SCAN_QR)) },
                    leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showSheet = false
                            showQrScanner = true
                        }
                    }
                )
                DropdownMenuItem(
                    text = { Text(s(StringKey.AUTH_ENTER_MANUALLY)) },
                    leadingIcon = { Icon(Icons.Default.Keyboard, contentDescription = null) },
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showSheet = false
                            showManualForm = true
                        }
                    }
                )
            }
        }
    }

    if (showManualForm) {
        ManualEntryDialog(
            onDismiss = { showManualForm = false },
            onSave = { issuer, account, secret, algo, digits, period ->
                val result = viewModel.addManual(issuer, account, secret, algo, digits, period)
                if (result.isSuccess) {
                    showManualForm = false
                } else {
                    Toast.makeText(
                        context,
                        s(StringKey.AUTH_INVALID_SECRET),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    pendingDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text(s(StringKey.AUTH_DELETE_CONFIRM_TITLE)) },
            text = { Text(s(StringKey.AUTH_DELETE_CONFIRM_MESSAGE)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(id)
                    pendingDeleteId = null
                }) { Text(s(StringKey.DELETE)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text(s(StringKey.CANCEL)) }
            }
        )
    }
}

@Composable
private fun EmptyState(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = s(StringKey.AUTH_EMPTY),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = s(StringKey.AUTH_EMPTY_HINT),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AccountRow(
    item: AccountCode,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = item.remainingSeconds.toFloat() / item.account.period.toFloat(),
        label = "countdown"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (item.account.issuer.isNotBlank()) {
                    Text(
                        text = item.account.issuer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (item.account.accountName.isNotBlank()) {
                    Text(
                        text = item.account.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = groupDigits(item.code),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    text = item.remainingSeconds.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = s(StringKey.DELETE))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSave: (
        issuer: String,
        accountName: String,
        secret: String,
        algorithm: TotpAlgorithm,
        digits: Int,
        period: Int
    ) -> Unit
) {
    var issuer by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var algorithm by remember { mutableStateOf(TotpAlgorithm.SHA1) }
    var digits by remember { mutableStateOf(6) }
    var period by remember { mutableStateOf(30) }
    var algoExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s(StringKey.AUTH_ADD_ACCOUNT)) },
        text = {
            Column {
                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = { Text(s(StringKey.AUTH_ISSUER)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text(s(StringKey.AUTH_ACCOUNT_NAME)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it.uppercase() },
                    label = { Text(s(StringKey.AUTH_SECRET)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = algoExpanded,
                    onExpandedChange = { algoExpanded = it }
                ) {
                    OutlinedTextField(
                        value = algorithm.canonical,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s(StringKey.AUTH_ALGORITHM)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = algoExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = algoExpanded,
                        onDismissRequest = { algoExpanded = false }
                    ) {
                        TotpAlgorithm.entries.forEach { alg ->
                            DropdownMenuItem(
                                text = { Text(alg.canonical) },
                                onClick = {
                                    algorithm = alg
                                    algoExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { digits = 6 },
                        label = { Text("6 ${s(StringKey.AUTH_DIGITS)}") },
                        enabled = digits != 6
                    )
                    AssistChip(
                        onClick = { digits = 8 },
                        label = { Text("8 ${s(StringKey.AUTH_DIGITS)}") },
                        enabled = digits != 8
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = period.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { if (it in 1..300) period = it } },
                    label = { Text(s(StringKey.AUTH_PERIOD_SECONDS)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(issuer, accountName, secret, algorithm, digits, period) },
                enabled = secret.isNotBlank() && accountName.isNotBlank()
            ) { Text(s(StringKey.AUTH_SAVE)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s(StringKey.CANCEL)) }
        }
    )
}
