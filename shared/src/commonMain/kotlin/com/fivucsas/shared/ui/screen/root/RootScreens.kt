package com.fivucsas.shared.ui.screen.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.data.repository.MockRootAdminRepository
import com.fivucsas.shared.domain.model.CapabilityPolicy
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RootPermission
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.presentation.state.RootConsoleUiEffect
import com.fivucsas.shared.presentation.state.RootConsoleUiEvent
import com.fivucsas.shared.presentation.viewmodel.RootConsoleViewModel
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.molecules.StatCard
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import com.fivucsas.shared.ui.components.organisms.QuickActionGrid
import com.fivucsas.shared.ui.components.organisms.QuickActionItem
import com.fivucsas.shared.ui.components.root.AdaptiveNavigation
import com.fivucsas.shared.ui.components.root.AppScaffold
import com.fivucsas.shared.ui.components.root.ConfirmDialog
import com.fivucsas.shared.ui.components.root.FilterChips
import com.fivucsas.shared.ui.components.root.InlineError
import com.fivucsas.shared.ui.components.root.LoadingState
import com.fivucsas.shared.ui.components.root.RootNavItem
import com.fivucsas.shared.ui.components.root.SearchBar
import com.fivucsas.shared.ui.navigation.RouteIds
import kotlinx.coroutines.launch

private enum class RootSection(val id: String, val label: String) {
    Console("console", "Console"),
    Tenants("tenants", "Tenants"),
    Users("users", "Users"),
    Audit("audit", "Audit"),
    Security("security", "Security"),
    Settings("settings", "Settings")
}

@Composable
fun RootConsoleScreen(
    role: UserRole,
    currentRoute: String = RouteIds.ROOT_CONSOLE,
    settingsRoute: String = RouteIds.SETTINGS,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateBottom: (String) -> Unit = {},
    onNavigate: (String, String?) -> Unit = { _, _ -> },
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RootConsoleUiEffect.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is RootConsoleUiEffect.OpenTenantContext -> scope.launch { snackbarHostState.showSnackbar("Impersonating ${effect.tenantId}") }
            }
        }
    }

    BoxWithConstraints {
        val isCompact = maxWidth < 840.dp
        if (isCompact) {
            RootConsoleMobileScaffold(
                baseModifier = Modifier.fillMaxSize(),
                currentRoute = currentRoute,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateBottom = onNavigateBottom,
                content = {
                    RootConsoleBody(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        isCompact = true,
                        onQuery = { viewModel.onEvent(RootConsoleUiEvent.UpdateQuery(it)) },
                        onTenantFilter = { name ->
                            val tenantId = state.tenants.firstOrNull { it.name == name }?.id
                            viewModel.onEvent(RootConsoleUiEvent.SelectTenant(tenantId))
                        },
                        settingsRoute = settingsRoute,
                        onNavigate = onNavigate
                    )
                }
            )
        } else {
            AppScaffold(title = "Root Dashboard", snackbarHostState = snackbarHostState) { baseModifier ->
                Row(modifier = baseModifier.fillMaxSize().padding(16.dp)) {
                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        RootConsoleDesktopTabs(onNavigate = onNavigate)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.fillMaxSize()) {
                        RootConsoleBody(
                            modifier = Modifier.weight(1f),
                            state = state,
                            isCompact = false,
                            onQuery = { viewModel.onEvent(RootConsoleUiEvent.UpdateQuery(it)) },
                            onTenantFilter = { name ->
                                val tenantId = state.tenants.firstOrNull { it.name == name }?.id
                                viewModel.onEvent(RootConsoleUiEvent.SelectTenant(tenantId))
                            },
                            settingsRoute = settingsRoute,
                            onNavigate = onNavigate
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootConsoleMobileScaffold(
    baseModifier: Modifier,
    currentRoute: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val navItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, RouteIds.ROOT_CONSOLE),
        BottomNavItem("History", Icons.Default.History, RouteIds.TENANT_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )
    Scaffold(
        modifier = baseModifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Root Dashboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun RootConsoleBody(
    modifier: Modifier = Modifier,
    state: com.fivucsas.shared.presentation.state.RootConsoleUiState,
    isCompact: Boolean,
    onQuery: (String) -> Unit,
    onTenantFilter: (String?) -> Unit,
    settingsRoute: String,
    onNavigate: (String, String?) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (state.errorMessage != null) InlineError(state.errorMessage!!)
        if (state.isLoading) LoadingState()

        if (state.capabilities == CapabilityPolicy.rootCapabilities || state.capabilities == CapabilityPolicy.tenantAdminCapabilities) {
            SearchBar(
                value = state.filter.query,
                placeholder = "Search tenants/users/actions",
                onValueChange = onQuery
            )
            Spacer(modifier = Modifier.height(10.dp))
            FilterChips(
                filters = state.tenants.map { it.name },
                selected = state.selectedTenantId,
                onSelect = onTenantFilter
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (isCompact) {
            RootConsoleCompact(
                modifier = Modifier.weight(1f),
                state = state,
                settingsRoute = settingsRoute,
                onOpen = onNavigate
            )
        } else {
            RootConsoleDesktop(
                modifier = Modifier.weight(1f),
                onOpen = onNavigate
            )
        }
    }
}

@Composable
private fun RootConsoleDesktopTabs(
    onNavigate: (String, String?) -> Unit
) {
    val items = listOf(
        "Console" to "root/console",
        "Tenants" to "root/tenant-management",
        "Global User Directory" to "root/global-user-directory",
        "Audit Explorer" to "root/audit-explorer",
        "Security Events" to "root/security-events",
        "Settings" to "root/system-settings"
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                rowItems.forEach { (label, route) ->
                    OutlinedButton(
                        onClick = { onNavigate(route, null) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) { Text(label) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun RootConsoleCompact(
    modifier: Modifier = Modifier,
    state: com.fivucsas.shared.presentation.state.RootConsoleUiState,
    settingsRoute: String,
    onOpen: (String, String?) -> Unit
) {
    val quickActions = listOf(
        QuickActionItem("Tenants", Icons.Default.Store) { onOpen("root/tenant-management", null) },
        QuickActionItem("Admins", Icons.Default.Group) { onOpen("root/tenant-admins", null) },
        QuickActionItem("Tenant Members", Icons.Default.VerifiedUser) { onOpen(RouteIds.ROOT_TENANT_MEMBERS, null) },
        QuickActionItem("Users", Icons.Default.People) { onOpen(RouteIds.ROOT_USERS, null) },
        QuickActionItem("Audit", Icons.Default.History) { onOpen("root/audit-explorer", null) },
        QuickActionItem("Security", Icons.Default.Security) { onOpen("root/security-events", null) },
        QuickActionItem("Settings", Icons.Default.Settings) { onOpen(settingsRoute, null) }
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "System Overview")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            StatCard(
                value = state.tenants.size.toString(),
                label = "Tenants",
                icon = Icons.Default.Store,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = state.users.size.toString(),
                label = "Global Users",
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            StatCard(
                value = state.tenantAdmins.size.toString(),
                label = "Tenant Admins",
                icon = Icons.Default.VerifiedUser,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = state.auditLogs.size.toString(),
                label = "Audit Items",
                icon = Icons.Default.Analytics,
                modifier = Modifier.weight(1f)
            )
        }

        SectionHeader(title = "Quick Actions")
        QuickActionGrid(actions = quickActions)

        SectionHeader(title = "Recent Tenants")
        state.tenants.take(5).forEach { tenant ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen("root/tenant-detail", tenant.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(tenant.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Admins: ${tenant.adminCount}  Members: ${tenant.memberCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Text(
                        text = "${tenant.quotaUsed}/${tenant.quotaLimit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RootConsoleDesktop(
    modifier: Modifier = Modifier,
    onOpen: (String, String?) -> Unit
) {
    val cards = listOf(
        Triple("Tenant Management", "Create/update/delete tenants", "root/tenant-management"),
        Triple("Tenant Admins", "Assign/unassign and disable", "root/tenant-admins"),
        Triple("Global Users", "Cross-tenant user directory", "root/global-user-directory"),
        Triple("Audit Explorer", "Filters and export", "root/audit-explorer"),
        Triple("Security Events", "Suspicious activity monitor", "root/security-events"),
        Triple("Roles & Permissions", "Global matrix editor", "root/roles-permissions")
    )
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cards) { (title, subtitle, route) ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onOpen(route, null) }
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = { onOpen(route, null) }) { Text("Open") }
                }
            }
        }
    }
}

@Composable
fun TenantManagementScreen(
    role: UserRole,
    onOpenTenant: (String) -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    AppScaffold(title = "Tenant Management", snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            SearchBar(
                value = state.filter.query,
                placeholder = "Search tenant",
                onValueChange = { viewModel.onEvent(RootConsoleUiEvent.UpdateQuery(it)) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            TenantListAdaptive(
                tenants = state.tenants,
                onOpenTenant = onOpenTenant,
                onDelete = { viewModel.onEvent(RootConsoleUiEvent.DeleteTenant(it)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onNavigateBack) { Text("Back") }
        }
    }
}

@Composable
private fun TenantListAdaptive(
    tenants: List<TenantSummary>,
    onOpenTenant: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    BoxWithConstraints {
        val compact = maxWidth < 840.dp
        if (compact) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tenants) { tenant ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenTenant(tenant.id) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(tenant.name, fontWeight = FontWeight.SemiBold)
                            Text("Quota ${tenant.quotaUsed}/${tenant.quotaLimit}")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onOpenTenant(tenant.id) }) { Text("Detail") }
                                OutlinedButton(onClick = { onDelete(tenant.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tenant", fontWeight = FontWeight.Bold)
                        Text("Usage", fontWeight = FontWeight.Bold)
                        Text("Admins", fontWeight = FontWeight.Bold)
                        Text("Members", fontWeight = FontWeight.Bold)
                        Text("Actions", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    tenants.forEach { tenant ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tenant.name)
                            Text("${tenant.quotaUsed}/${tenant.quotaLimit}")
                            Text(tenant.adminCount.toString())
                            Text(tenant.memberCount.toString())
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onOpenTenant(tenant.id) }) { Text("Open") }
                                OutlinedButton(onClick = { onDelete(tenant.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TenantDetailScreen(
    role: UserRole,
    tenantId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    var askImpersonate by remember { mutableStateOf(false) }
    LaunchedEffect(tenantId) { viewModel.onEvent(RootConsoleUiEvent.Load(tenantId)) }

    val detail = state.tenantDetail
    AppScaffold(title = "Tenant Detail", snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (detail == null) {
                LoadingState()
            } else {
                TenantDetailCard(detail = detail)
                if (state.capabilities?.can(RootPermission.USER_UPDATE) == true) {
                    OutlinedButton(onClick = { askImpersonate = true }) { Text("Impersonate Tenant Admin") }
                }
                OutlinedButton(onClick = onNavigateBack) { Text("Back") }
            }
        }
    }
    if (askImpersonate) {
        ConfirmDialog(
            title = "Impersonate Admin",
            body = "Enter tenant context for ${tenantId}?",
            onConfirm = {
                viewModel.onEvent(RootConsoleUiEvent.ConfirmImpersonation(tenantId))
                askImpersonate = false
            },
            onDismiss = { askImpersonate = false }
        )
    }
}

@Composable
private fun TenantDetailCard(detail: TenantDetail) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(detail.summary.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Status: ${detail.summary.status}")
            Text("Admins: ${detail.admins.size} | Members: ${detail.members.size}")
            Text("Usage: ${detail.summary.quotaUsed}/${detail.summary.quotaLimit}")
            Text("Settings:")
            detail.settings.forEach { (k, v) -> Text(" - $k: $v") }
        }
    }
}

@Composable
fun GlobalUserDirectoryScreen(
    role: UserRole,
    screenTitle: String = "Global User Directory",
    initialRoleFilter: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    RootUserManagementScreen(
        title = screenTitle,
        users = state.users,
        onToggle = { id, enabled -> viewModel.onEvent(RootConsoleUiEvent.ToggleUserEnabled(id, enabled)) },
        onEdit = { id, fullName, email, role, tenantId ->
            viewModel.onEvent(
                RootConsoleUiEvent.UpdateUserProfile(
                    userId = id,
                    fullName = fullName,
                    email = email,
                    role = role,
                    tenantId = tenantId
                )
            )
        },
        onDelete = { id -> viewModel.onEvent(RootConsoleUiEvent.DeleteUser(id)) },
        initialRoleFilter = initialRoleFilter,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun TenantAdminsScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    RootUserManagementScreen(
        title = "Tenant Admins",
        users = state.tenantAdmins,
        onToggle = { id, enabled -> viewModel.onEvent(RootConsoleUiEvent.ToggleUserEnabled(id, enabled)) },
        onEdit = { id, fullName, email, role, tenantId ->
            viewModel.onEvent(
                RootConsoleUiEvent.UpdateUserProfile(
                    userId = id,
                    fullName = fullName,
                    email = email,
                    role = role,
                    tenantId = tenantId
                )
            )
        },
        onDelete = { id -> viewModel.onEvent(RootConsoleUiEvent.DeleteUser(id)) },
        initialRoleFilter = "TENANT_ADMIN",
        onNavigateBack = onNavigateBack
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun RootUserManagementScreen(
    title: String,
    users: List<GlobalUser>,
    onToggle: (String, Boolean) -> Unit,
    onEdit: (String, String, String, String, String?) -> Unit,
    onDelete: (String) -> Unit,
    initialRoleFilter: String? = null,
    onNavigateBack: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(initialRoleFilter ?: "ALL") }
    var editingUser by remember { mutableStateOf<GlobalUser?>(null) }
    val roleOptions = listOf("ALL", "USER", "TENANT_MEMBER", "TENANT_ADMIN", "ROOT")
    val visibleUsers = users.filter { selectedRole == "ALL" || it.role == selectedRole }

    AppScaffold(title = title, snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "User Type Filters",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                roleOptions.forEach { role ->
                    AssistChip(
                        onClick = { selectedRole = role },
                        label = { Text(role.replace('_', ' ')) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedRole == role) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(visibleUsers) { user ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column {
                                Text(user.fullName, fontWeight = FontWeight.SemiBold)
                                Text(user.email, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "Role: ${user.role}  Tenant: ${user.tenantId ?: "GLOBAL"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onToggle(user.id, !user.enabled) }) { Text(if (user.enabled) "Disable" else "Enable") }
                                OutlinedButton(onClick = { editingUser = user }) { Text("Edit") }
                                OutlinedButton(onClick = { onDelete(user.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text("Back") }
        }
    }

    editingUser?.let { user ->
        EditRootUserDialog(
            user = user,
            onDismiss = { editingUser = null },
            onSave = { fullName, email, role, tenantId ->
                onEdit(user.id, fullName, email, role, tenantId)
                editingUser = null
            }
        )
    }
}

@Composable
private fun EditRootUserDialog(
    user: GlobalUser,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String?) -> Unit
) {
    var name by remember(user.id) { mutableStateOf(user.fullName) }
    var email by remember(user.id) { mutableStateOf(user.email) }
    var role by remember(user.id) { mutableStateOf(user.role) }
    var tenantId by remember(user.id) { mutableStateOf(user.tenantId ?: "") }
    val roles = listOf("USER", "TENANT_MEMBER", "TENANT_ADMIN", "ROOT")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = tenantId,
                    onValueChange = { tenantId = it },
                    label = { Text("Tenant Id (optional)") },
                    singleLine = true
                )
                Text("Role", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    roles.forEach { option ->
                        AssistChip(
                            onClick = { role = option },
                            label = { Text(option) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (role == option) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, email, role, tenantId.takeIf { it.isNotBlank() }) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RolesPermissionsScreen(onNavigateBack: () -> Unit = {}) {
    AppScaffold(title = "Roles & Permissions", snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Global RBAC matrix editor", style = MaterialTheme.typography.titleMedium)
            Text("Use backend as source of truth. UI matrix is a convenience.")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("TENANT_ADMIN", fontWeight = FontWeight.Bold)
                    Text("TENANT_USERS_READ, TENANT_SETTINGS_UPDATE, HISTORY_READ_TENANT")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TENANT_MEMBER", fontWeight = FontWeight.Bold)
                    Text("VERIFY_SELF, ENROLL_SELF_CREATE, HISTORY_READ_SELF")
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text("Back") }
        }
    }
}

@Composable
fun AuditExplorerScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    AppScaffold(title = "Audit Explorer", snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SearchBar(
                value = state.filter.query,
                placeholder = "Filter by actor/action/tenant",
                onValueChange = { viewModel.onEvent(RootConsoleUiEvent.UpdateQuery(it)) }
            )
            FilterChips(
                filters = listOf("SUCCESS", "FAILED", "TENANT_UPDATE", "USER_DISABLE"),
                selected = state.filter.status,
                onSelect = { }
            )
            OutlinedButton(onClick = { viewModel.onEvent(RootConsoleUiEvent.RefreshAudit) }) { Text("Refresh") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.auditLogs) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text("${log.actor} -> ${log.action}", fontWeight = FontWeight.SemiBold)
                            Text("Status: ${log.status} | Tenant: ${log.tenantId ?: "GLOBAL"}")
                            Text(log.details, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text("Back") }
        }
    }
}

@Composable
fun SecurityEventsScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    AppScaffold(title = "Security Events", snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.onEvent(RootConsoleUiEvent.RefreshSecurity) }) { Text("Refresh") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.securityEvents) { ev ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text("${ev.severity} - ${ev.eventType}", fontWeight = FontWeight.SemiBold)
                            Text(ev.message)
                        }
                    }
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text("Back") }
        }
    }
}

@Composable
fun SystemSettingsScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = remember { RootConsoleViewModel(role, MockRootAdminRepository()) }
) {
    val state by viewModel.state.collectAsState()
    var rateLimit by remember { mutableStateOf("120") }
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    val settings = state.settings
    AppScaffold(title = "System Settings", snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (settings != null) {
                Text("JWT policy (read-only): ${settings.jwtPolicySummary}")
                SearchBar(
                    value = rateLimit,
                    placeholder = "Default rate limit per minute",
                    onValueChange = { rateLimit = it }
                )
                OutlinedButton(
                    onClick = {
                        viewModel.applySystemSettings(
                            settingsText = settings.jwtPolicySummary,
                            rateLimit = rateLimit.toIntOrNull() ?: settings.defaultRateLimitPerMinute,
                            passwordPolicy = settings.passwordPolicySummary
                        )
                    }
                ) { Text("Save") }
            } else {
                LoadingState()
            }
            OutlinedButton(onClick = onNavigateBack) { Text("Back") }
        }
    }
}
