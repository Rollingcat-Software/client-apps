package com.fivucsas.desktop.ui.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.AppMode
import com.fivucsas.desktop.ui.components.DesktopAppShell
import com.fivucsas.desktop.ui.components.DesktopDashboardActionCard
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.desktop.ui.components.DesktopTable
import com.fivucsas.shared.data.repository.MockRootAdminRepository
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.presentation.viewmodel.InviteStatus
import com.fivucsas.shared.presentation.viewmodel.InviteViewModel
import com.fivucsas.shared.presentation.state.RootConsoleUiEvent
import com.fivucsas.shared.presentation.viewmodel.RootConsoleViewModel

private data class RootRailItem(val label: String, val mode: AppMode)

@Composable
private fun RootDesktopShell(
    title: String,
    selected: AppMode,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val railItems = listOf(
        RootRailItem("Console", AppMode.ROOT_HOME),
        RootRailItem("Tenants", AppMode.ROOT_TENANT_MANAGEMENT),
        RootRailItem("Users", AppMode.ROOT_GLOBAL_USER_DIRECTORY),
        RootRailItem("Admins", AppMode.ROOT_TENANT_ADMINS),
        RootRailItem("Invites", AppMode.ROOT_INVITE_MANAGEMENT),
        RootRailItem("Audit", AppMode.ROOT_AUDIT_EXPLORER),
        RootRailItem("Security", AppMode.ROOT_SECURITY_EVENTS),
        RootRailItem("Settings", AppMode.ROOT_SYSTEM_SETTINGS)
    )

    DesktopAppShell(
        title = title,
        onBack = onBack,
        onSettings = { onNavigate(AppMode.ROOT_SYSTEM_SETTINGS) },
        onLogout = onLogout,
        railContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                railItems.forEach { item ->
                    if (item.mode == selected) {
                        Button(
                            onClick = { onNavigate(item.mode) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(item.label) }
                    } else {
                        OutlinedButton(
                            onClick = { onNavigate(item.mode) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(item.label) }
                    }
                }
            }
        }
    ) {
        content()
    }
}

@Composable
fun RootDesktopConsoleScreen(
    role: UserRole,
    onNavigate: (AppMode) -> Unit,
    onOpenTenant: (String) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    RootDesktopShell(
        title = "Root Dashboard",
        selected = AppMode.ROOT_HOME,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader("Root Dashboard", "Global platform control center")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DesktopDashboardActionCard(
                    icon = Icons.Default.Store,
                    title = "Tenants",
                    subtitle = "${state.tenants.size} tenants in system",
                    onClick = { onNavigate(AppMode.ROOT_TENANT_MANAGEMENT) },
                    modifier = Modifier.weight(1f)
                )
                DesktopDashboardActionCard(
                    icon = Icons.Default.People,
                    title = "Users",
                    subtitle = "${state.users.size} global users",
                    onClick = { onNavigate(AppMode.ROOT_GLOBAL_USER_DIRECTORY) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DesktopDashboardActionCard(
                    icon = Icons.Default.History,
                    title = "Audit",
                    subtitle = "Browse and export global logs",
                    onClick = { onNavigate(AppMode.ROOT_AUDIT_EXPLORER) },
                    modifier = Modifier.weight(1f)
                )
                DesktopDashboardActionCard(
                    icon = Icons.Default.Security,
                    title = "Security",
                    subtitle = "Review platform security events",
                    onClick = { onNavigate(AppMode.ROOT_SECURITY_EVENTS) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DesktopDashboardActionCard(
                    icon = Icons.Default.Mail,
                    title = "Invites",
                    subtitle = "Send and manage tenant invitations",
                    onClick = { onNavigate(AppMode.ROOT_INVITE_MANAGEMENT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            DesktopTable(
                title = "Recent Tenants",
                subtitle = "Latest tenant activity snapshot"
            ) {
                state.tenants.take(8).forEach { tenant ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(tenant.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Admins: ${tenant.adminCount}  Members: ${tenant.memberCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(onClick = { onOpenTenant(tenant.id) }) {
                            Text("Open")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RootDesktopTenantManagementScreen(
    role: UserRole,
    onNavigate: (AppMode) -> Unit,
    onOpenTenant: (String) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    RootDesktopShell(
        title = "Tenant Management",
        selected = AppMode.ROOT_TENANT_MANAGEMENT,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        DesktopTable("Tenants", "Create, edit, and inspect tenant limits") {
            state.tenants.forEach { tenant ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(tenant.name, modifier = Modifier.weight(1f))
                    Text("${tenant.quotaUsed}/${tenant.quotaLimit}", modifier = Modifier.width(100.dp))
                    Text("${tenant.adminCount}", modifier = Modifier.width(60.dp))
                    Text("${tenant.memberCount}", modifier = Modifier.width(70.dp))
                    OutlinedButton(onClick = { onOpenTenant(tenant.id) }) { Text("Detail") }
                }
            }
        }
    }
}

@Composable
fun RootDesktopTenantDetailScreen(
    role: UserRole,
    tenantId: String?,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(tenantId) { if (tenantId != null) viewModel.onEvent(RootConsoleUiEvent.Load(tenantId)) }

    RootDesktopShell(
        title = "Tenant Detail",
        selected = AppMode.ROOT_TENANT_MANAGEMENT,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        val detail = state.tenantDetail
        if (tenantId == null || detail == null) {
            DesktopSectionHeader("Tenant Detail", "No tenant selected")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(detail.summary.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Usage: ${detail.summary.quotaUsed}/${detail.summary.quotaLimit}")
                    Text("Status: ${detail.summary.status}")
                    Text("Admins: ${detail.admins.size}  Members: ${detail.members.size}")
                }
            }
        }
    }
}

@Composable
fun RootDesktopUserListScreen(
    title: String,
    selected: AppMode,
    role: UserRole,
    showTenantAdmins: Boolean,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    var selectedRole by remember { mutableStateOf(if (showTenantAdmins) "TENANT_ADMIN" else "ALL") }
    var editingUser by remember { mutableStateOf<com.fivucsas.shared.domain.model.GlobalUser?>(null) }
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    val users = if (showTenantAdmins) state.tenantAdmins else state.users
    val roleOptions = listOf("ALL", "USER", "TENANT_MEMBER", "TENANT_ADMIN", "ROOT")
    val visibleUsers = users.filter { selectedRole == "ALL" || it.role == selectedRole }

    RootDesktopShell(
        title = title,
        selected = selected,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        DesktopTable(title, "Directory view across tenants") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                roleOptions.forEach { roleOption ->
                    AssistChip(
                        onClick = { selectedRole = roleOption },
                        label = { Text(roleOption.replace('_', ' ')) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedRole == roleOption) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }
            }

            visibleUsers.forEach { user ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.fullName, fontWeight = FontWeight.SemiBold)
                        Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(user.role, modifier = Modifier.width(130.dp))
                    Text(user.tenantId ?: "GLOBAL", modifier = Modifier.width(120.dp))
                    Text(if (user.enabled) "Enabled" else "Disabled", modifier = Modifier.width(90.dp))
                    OutlinedButton(onClick = { viewModel.onEvent(RootConsoleUiEvent.ToggleUserEnabled(user.id, !user.enabled)) }) {
                        Text(if (user.enabled) "Disable" else "Enable")
                    }
                    OutlinedButton(onClick = { editingUser = user }) { Text("Edit") }
                    OutlinedButton(onClick = { viewModel.onEvent(RootConsoleUiEvent.DeleteUser(user.id)) }) { Text("Delete") }
                }
            }
        }
    }

    editingUser?.let { user ->
        var name by remember(user.id) { mutableStateOf(user.fullName) }
        var email by remember(user.id) { mutableStateOf(user.email) }
        var roleValue by remember(user.id) { mutableStateOf(user.role) }
        var tenantId by remember(user.id) { mutableStateOf(user.tenantId ?: "") }

        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("Edit User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true)
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                    OutlinedTextField(value = tenantId, onValueChange = { tenantId = it }, label = { Text("Tenant Id") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        roleOptions.filter { it != "ALL" }.forEach { roleOption ->
                            AssistChip(
                                onClick = { roleValue = roleOption },
                                label = { Text(roleOption) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (roleValue == roleOption) MaterialTheme.colorScheme.primaryContainer
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
                        viewModel.onEvent(
                            RootConsoleUiEvent.UpdateUserProfile(
                                userId = user.id,
                                fullName = name,
                                email = email,
                                role = roleValue,
                                tenantId = tenantId.takeIf { it.isNotBlank() }
                            )
                        )
                        editingUser = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun RootDesktopInviteManagementScreen(
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { InviteViewModel() }
    val state by viewModel.state.collectAsState()
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("TENANT_MEMBER") }
    var selectedInviteRole by remember { mutableStateOf<String?>(null) }
    var inviteTenantId by remember { mutableStateOf<String?>(null) }
    var inviteTenantName by remember { mutableStateOf<String?>(null) }
    val roleOptions = listOf("USER", "TENANT_MEMBER", "TENANT_ADMIN")
    val inviteFilterRoles = listOf("USER", "TENANT_MEMBER", "TENANT_ADMIN")
    val tenantOptions = remember(state.invites) {
        state.invites
            .mapNotNull { invite ->
                val tid = invite.tenantId ?: return@mapNotNull null
                val tname = invite.tenantName ?: tid
                tid to tname
            }
            .distinctBy { it.first }
            .sortedBy { it.second }
    }
    val visibleInvites = state.filteredInvites.filter { invite ->
        selectedInviteRole == null || invite.role == selectedInviteRole
    }

    LaunchedEffect(Unit) { viewModel.loadInvites() }

    RootDesktopShell(
        title = "Invite Management",
        selected = AppMode.ROOT_INVITE_MANAGEMENT,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DesktopSectionHeader(
                title = "Invite Management",
                subtitle = "Send invites and manage existing invitation lifecycle"
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearch,
                    label = { Text("Search email") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { viewModel.showCreateDialog() }) { Text("Send Invite") }
            }

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
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { viewModel.setTenantFilter(null) },
                        label = { Text("All Tenants") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedTenantId == null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                items(tenantOptions) { (tenantId, tenantName) ->
                    AssistChip(
                        onClick = { viewModel.setTenantFilter(tenantId) },
                        label = { Text(tenantName) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedTenantId == tenantId) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    AssistChip(
                        onClick = { selectedInviteRole = null },
                        label = { Text("All Roles") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedInviteRole == null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                items(inviteFilterRoles) { role ->
                    AssistChip(
                        onClick = { selectedInviteRole = role },
                        label = { Text(role) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedInviteRole == role) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            DesktopTable(
                title = "Sent Invitations",
                subtitle = "${visibleInvites.size} invitations"
            ) {
                visibleInvites.forEach { invite ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(invite.email, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Role: ${invite.role}  |  Tenant: ${invite.tenantName ?: "GLOBAL"}  |  Expires: ${invite.expiresAt}",
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

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateDialog() },
            title = { Text("Create Invite") },
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
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            AssistChip(
                                onClick = {
                                    inviteTenantId = null
                                    inviteTenantName = null
                                },
                                label = { Text("GLOBAL") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (inviteTenantId == null) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                        items(tenantOptions) { (tenantId, tenantName) ->
                            AssistChip(
                                onClick = {
                                    inviteTenantId = tenantId
                                    inviteTenantName = tenantName
                                },
                                label = { Text(tenantName) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (inviteTenantId == tenantId) MaterialTheme.colorScheme.primaryContainer
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
                                role = inviteRole,
                                tenantId = inviteTenantId,
                                tenantName = inviteTenantName
                            )
                            inviteEmail = ""
                            inviteRole = "TENANT_MEMBER"
                            inviteTenantId = null
                            inviteTenantName = null
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

@Composable
fun RootDesktopRolesPermissionsScreen(
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    RootDesktopShell(
        title = "Roles & Permissions",
        selected = AppMode.ROOT_ROLES_PERMISSIONS,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DesktopSectionHeader("Global RBAC Matrix", "Root can manage full role/permission mapping")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ROOT", fontWeight = FontWeight.Bold)
                    Text("All platform and tenant permissions")
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("TENANT_ADMIN", fontWeight = FontWeight.Bold)
                    Text("Tenant scoped management and audit permissions")
                }
            }
        }
    }
}

@Composable
fun RootDesktopAuditScreen(
    role: UserRole,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    RootDesktopShell(
        title = "Audit Explorer",
        selected = AppMode.ROOT_AUDIT_EXPLORER,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        DesktopTable("Audit Logs", "Global audit stream") {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.auditLogs) { log ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${log.actor} -> ${log.action}", modifier = Modifier.weight(1f))
                        Text(log.status, modifier = Modifier.width(90.dp))
                        Text(log.tenantId ?: "GLOBAL", modifier = Modifier.width(120.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RootDesktopSecurityScreen(
    role: UserRole,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    RootDesktopShell(
        title = "Security Events",
        selected = AppMode.ROOT_SECURITY_EVENTS,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        DesktopTable("Security Events", "Platform monitoring feed") {
            state.securityEvents.forEach { event ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${event.severity} - ${event.eventType}", modifier = Modifier.weight(1f))
                    Text(event.message, modifier = Modifier.weight(2f))
                }
            }
        }
    }
}

@Composable
fun RootDesktopSystemSettingsScreen(
    role: UserRole,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
    val state by viewModel.state.collectAsState()
    var rateLimitInput by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    RootDesktopShell(
        title = "System Settings",
        selected = AppMode.ROOT_SYSTEM_SETTINGS,
        onNavigate = onNavigate,
        onBack = onBack,
        onLogout = onLogout
    ) {
        val settings = state.settings
        if (settings == null) {
            DesktopSectionHeader("System Settings", "Loading settings...")
        } else {
            if (rateLimitInput.isBlank()) {
                rateLimitInput = settings.defaultRateLimitPerMinute.toString()
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DesktopSectionHeader(
                    title = "System Settings",
                    subtitle = "Manage platform-level defaults and security policies"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Session Policy", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Token and access-session rules are managed centrally.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                settings.jwtPolicySummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Password Policy", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Global password requirements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                settings.passwordPolicySummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Rate Limit Defaults", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = rateLimitInput,
                            onValueChange = { rateLimitInput = it },
                            label = { Text("Requests per minute") },
                            singleLine = true
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.applySystemSettings(
                                        settingsText = settings.jwtPolicySummary,
                                        rateLimit = rateLimitInput.toIntOrNull() ?: settings.defaultRateLimitPerMinute,
                                        passwordPolicy = settings.passwordPolicySummary
                                    )
                                }
                            ) { Text("Save Changes") }
                            OutlinedButton(onClick = { rateLimitInput = settings.defaultRateLimitPerMinute.toString() }) {
                                Text("Reset")
                            }
                        }
                    }
                }
            }
        }
    }
}
