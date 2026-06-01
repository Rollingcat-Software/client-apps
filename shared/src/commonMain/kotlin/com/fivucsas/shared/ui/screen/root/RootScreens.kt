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
import androidx.compose.foundation.lazy.LazyRow
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
import com.fivucsas.shared.domain.repository.RootAdminRepository
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.domain.model.CapabilityPolicy
import com.fivucsas.shared.domain.model.GlobalUser
import com.fivucsas.shared.domain.model.RootPermission
import com.fivucsas.shared.domain.model.TenantDetail
import com.fivucsas.shared.domain.model.TenantSummary
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.presentation.state.RootConsoleUiEffect
import com.fivucsas.shared.presentation.state.RootConsoleUiEvent
import com.fivucsas.shared.domain.model.InviteStatus
import com.fivucsas.shared.presentation.viewmodel.InviteViewModel
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
import com.fivucsas.shared.ui.theme.AppColors
import org.koin.compose.koinInject
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
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RootConsoleUiEffect.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
                is RootConsoleUiEffect.OpenTenantContext -> scope.launch { snackbarHostState.showSnackbar(s(StringKey.ROOT_IMPERSONATING, effect.tenantId)) }
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
            AppScaffold(title = s(StringKey.ROOT_DASHBOARD), snackbarHostState = snackbarHostState) { baseModifier ->
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
        BottomNavItem(s(StringKey.ROOT_NAV_DASHBOARD), Icons.Default.Home, RouteIds.ROOT_CONSOLE),
        BottomNavItem(s(StringKey.ROOT_NAV_HISTORY), Icons.Default.History, RouteIds.TENANT_HISTORY),
        BottomNavItem(s(StringKey.NAV_PROFILE), Icons.Default.Person, RouteIds.PROFILE)
    )
    Scaffold(
        modifier = baseModifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = s(StringKey.ROOT_DASHBOARD),
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
                                contentDescription = s(StringKey.NAV_NOTIFICATIONS)
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = s(StringKey.NAV_PROFILE)
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
                placeholder = s(StringKey.ROOT_SEARCH_TENANTS_USERS_ACTIONS),
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
        s(StringKey.ROOT_TAB_CONSOLE) to "root/console",
        s(StringKey.ROOT_TAB_TENANTS) to "root/tenant-management",
        s(StringKey.ROOT_GLOBAL_USER_DIRECTORY) to "root/global-user-directory",
        s(StringKey.ROOT_AUDIT_EXPLORER) to "root/audit-explorer",
        s(StringKey.ROOT_SECURITY_EVENTS) to "root/security-events",
        s(StringKey.NAV_SETTINGS) to "root/system-settings"
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
        QuickActionItem(s(StringKey.ROOT_ACTION_TENANTS), Icons.Default.Store, { onOpen("root/tenant-management", null) }, AppColors.Primary),
        QuickActionItem(s(StringKey.ROOT_ACTION_ADMINS), Icons.Default.Group, { onOpen("root/tenant-admins", null) }, AppColors.WarningDark),
        QuickActionItem(s(StringKey.ROOT_ACTION_TENANT_MEMBERS), Icons.Default.VerifiedUser, { onOpen(RouteIds.ROOT_TENANT_MEMBERS, null) }, AppColors.SuccessDark),
        QuickActionItem(s(StringKey.ROOT_ACTION_USERS), Icons.Default.People, { onOpen(RouteIds.ROOT_USERS, null) }, AppColors.InfoDark),
        QuickActionItem(s(StringKey.ROOT_ACTION_INVITES), Icons.Default.Mail, { onOpen(RouteIds.ROOT_INVITE_MANAGEMENT, null) }, AppColors.SecondaryVariant),
        QuickActionItem(s(StringKey.ROOT_ACTION_AUDIT), Icons.Default.History, { onOpen("root/audit-explorer", null) }, AppColors.Warning),
        QuickActionItem(s(StringKey.ROOT_ACTION_SECURITY), Icons.Default.Security, { onOpen("root/security-events", null) }, AppColors.ErrorDark),
        QuickActionItem(s(StringKey.NAV_SETTINGS), Icons.Default.Settings, { onOpen(settingsRoute, null) }, AppColors.Gray700)
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = s(StringKey.ROOT_SYSTEM_OVERVIEW))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            StatCard(
                value = state.tenants.size.toString(),
                label = s(StringKey.ROOT_STAT_TENANTS),
                icon = Icons.Default.Store,
                iconTint = AppColors.Primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = state.users.size.toString(),
                label = s(StringKey.ROOT_STAT_GLOBAL_USERS),
                icon = Icons.Default.People,
                iconTint = AppColors.Info,
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
                label = s(StringKey.ROOT_STAT_TENANT_ADMINS),
                icon = Icons.Default.VerifiedUser,
                iconTint = AppColors.WarningDark,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = state.auditLogs.size.toString(),
                label = s(StringKey.ROOT_STAT_AUDIT_ITEMS),
                icon = Icons.Default.Analytics,
                iconTint = AppColors.SuccessDark,
                modifier = Modifier.weight(1f)
            )
        }

        SectionHeader(title = s(StringKey.DASH_QUICK_ACTIONS))
        QuickActionGrid(actions = quickActions)

        SectionHeader(title = s(StringKey.ROOT_RECENT_TENANTS))
        state.tenants.take(5).forEach { tenant ->
            val tenantPalette = listOf(
                AppColors.Primary.copy(alpha = 0.10f),
                AppColors.Success.copy(alpha = 0.12f),
                AppColors.Warning.copy(alpha = 0.12f),
                AppColors.Info.copy(alpha = 0.12f),
                AppColors.Secondary.copy(alpha = 0.12f)
            )
            val accent = tenantPalette[kotlin.math.abs(tenant.id.hashCode()) % tenantPalette.size]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen("root/tenant-detail", tenant.id) },
                colors = CardDefaults.cardColors(containerColor = accent)
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
                                s(StringKey.ROOT_ADMINS_MEMBERS_INLINE, tenant.adminCount, tenant.memberCount),
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
        Triple(s(StringKey.TENANT_MANAGEMENT), s(StringKey.ROOT_CARD_TENANT_MANAGEMENT_DESC), "root/tenant-management"),
        Triple(s(StringKey.ROOT_TENANT_ADMINS), s(StringKey.ROOT_CARD_TENANT_ADMINS_DESC), "root/tenant-admins"),
        Triple(s(StringKey.ROOT_GLOBAL_USERS), s(StringKey.ROOT_CARD_GLOBAL_USERS_DESC), "root/global-user-directory"),
        Triple(s(StringKey.ROOT_AUDIT_EXPLORER), s(StringKey.ROOT_CARD_AUDIT_EXPLORER_DESC), "root/audit-explorer"),
        Triple(s(StringKey.ROOT_SECURITY_EVENTS), s(StringKey.ROOT_CARD_SECURITY_EVENTS_DESC), "root/security-events"),
        Triple(s(StringKey.ROOT_CARD_ROLES_PERMISSIONS), s(StringKey.ROOT_CARD_ROLES_PERMISSIONS_DESC), "root/roles-permissions")
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
                    OutlinedButton(onClick = { onOpen(route, null) }) { Text(s(StringKey.ROOT_OPEN)) }
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
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }

    AppScaffold(title = s(StringKey.TENANT_MANAGEMENT), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            SearchBar(
                value = state.filter.query,
                placeholder = s(StringKey.ROOT_SEARCH_TENANT),
                onValueChange = { viewModel.onEvent(RootConsoleUiEvent.UpdateQuery(it)) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            TenantListAdaptive(
                tenants = state.tenants,
                onOpenTenant = onOpenTenant,
                onDelete = { viewModel.onEvent(RootConsoleUiEvent.DeleteTenant(it)) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
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
                            Text(s(StringKey.ROOT_QUOTA_INLINE, tenant.quotaUsed, tenant.quotaLimit))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onOpenTenant(tenant.id) }) { Text(s(StringKey.ROOT_DETAIL)) }
                                OutlinedButton(onClick = { onDelete(tenant.id) }) { Text(s(StringKey.DELETE)) }
                            }
                        }
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(s(StringKey.ROOT_TABLE_TENANT), fontWeight = FontWeight.Bold)
                        Text(s(StringKey.ROOT_TABLE_USAGE), fontWeight = FontWeight.Bold)
                        Text(s(StringKey.ROOT_TABLE_ADMINS), fontWeight = FontWeight.Bold)
                        Text(s(StringKey.ROOT_TABLE_MEMBERS), fontWeight = FontWeight.Bold)
                        Text(s(StringKey.ROOT_TABLE_ACTIONS), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    tenants.forEach { tenant ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tenant.name)
                            Text("${tenant.quotaUsed}/${tenant.quotaLimit}")
                            Text(tenant.adminCount.toString())
                            Text(tenant.memberCount.toString())
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onOpenTenant(tenant.id) }) { Text(s(StringKey.ROOT_OPEN)) }
                                OutlinedButton(onClick = { onDelete(tenant.id) }) { Text(s(StringKey.DELETE)) }
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
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    var askImpersonate by remember { mutableStateOf(false) }
    LaunchedEffect(tenantId) { viewModel.onEvent(RootConsoleUiEvent.Load(tenantId)) }

    val detail = state.tenantDetail
    AppScaffold(title = s(StringKey.ROOT_TENANT_DETAIL), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (detail == null) {
                LoadingState()
            } else {
                TenantDetailCard(detail = detail)
                if (state.capabilities?.can(RootPermission.USER_UPDATE) == true) {
                    OutlinedButton(onClick = { askImpersonate = true }) { Text(s(StringKey.ROOT_IMPERSONATE_TENANT_ADMIN)) }
                }
                OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
            }
        }
    }
    if (askImpersonate) {
        ConfirmDialog(
            title = s(StringKey.ROOT_IMPERSONATE_ADMIN),
            body = s(StringKey.ROOT_IMPERSONATE_CONFIRM, tenantId),
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
            Text(s(StringKey.ROOT_DETAIL_STATUS, detail.summary.status))
            Text(s(StringKey.ROOT_DETAIL_ADMINS_MEMBERS, detail.admins.size, detail.members.size))
            Text(s(StringKey.ROOT_DETAIL_USAGE, detail.summary.quotaUsed, detail.summary.quotaLimit))
            Text(s(StringKey.ROOT_DETAIL_SETTINGS))
            detail.settings.forEach { (k, v) -> Text(" - $k: $v") }
        }
    }
}

@Composable
fun GlobalUserDirectoryScreen(
    role: UserRole,
    screenTitle: String = s(StringKey.ROOT_GLOBAL_USER_DIRECTORY),
    initialRoleFilter: String? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
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
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    RootUserManagementScreen(
        title = s(StringKey.ROOT_TENANT_ADMINS),
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
                text = s(StringKey.ROOT_USER_TYPE_FILTERS),
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
                                    s(StringKey.ROOT_USER_ROLE_TENANT_INLINE, user.role, user.tenantId ?: s(StringKey.ROOT_INVITE_GLOBAL)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(onClick = { onToggle(user.id, !user.enabled) }) { Text(if (user.enabled) s(StringKey.ROOT_USER_DISABLE) else s(StringKey.ROOT_USER_ENABLE)) }
                                OutlinedButton(onClick = { editingUser = user }) { Text(s(StringKey.EDIT)) }
                                OutlinedButton(onClick = { onDelete(user.id) }) { Text(s(StringKey.DELETE)) }
                            }
                        }
                    }
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
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
@OptIn(ExperimentalLayoutApi::class)
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
        title = { Text(s(StringKey.EDIT_USER)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(s(StringKey.ROOT_EDIT_FULL_NAME)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(s(StringKey.EMAIL)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = tenantId,
                    onValueChange = { tenantId = it },
                    label = { Text(s(StringKey.ROOT_EDIT_TENANT_ID_OPTIONAL)) },
                    singleLine = true
                )
                Text(s(StringKey.ROOT_EDIT_ROLE), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.forEach { option ->
                        AssistChip(
                            onClick = { role = option },
                            label = { Text(option.replace('_', ' ')) },
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
            TextButton(onClick = { onSave(name, email, role, tenantId.takeIf { it.isNotBlank() }) }) { Text(s(StringKey.SAVE)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s(StringKey.CANCEL)) }
        }
    )
}

@Composable
fun RootInviteManagementScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: InviteViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("TENANT_MEMBER") }
    var inviteTenantId by remember { mutableStateOf<String?>(null) }
    var inviteTenantName by remember { mutableStateOf<String?>(null) }
    var selectedInviteRole by remember { mutableStateOf<String?>(null) }
    val roleOptions = listOf("USER", "TENANT_MEMBER", "TENANT_ADMIN")
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

    AppScaffold(title = s(StringKey.ROOT_INVITE_MANAGEMENT), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(
            modifier = modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchBar(
                value = state.searchQuery,
                placeholder = s(StringKey.ROOT_INVITE_SEARCH_BY_EMAIL),
                onValueChange = viewModel::updateSearch
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    AssistChip(
                        onClick = { viewModel.setFilter(null) },
                        label = { Text("ALL") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (state.selectedFilter == null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                items(InviteStatus.entries) { status ->
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    AssistChip(
                        onClick = { selectedInviteRole = null },
                        label = { Text(s(StringKey.ROOT_INVITE_ALL_ROLES)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedInviteRole == null) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                items(roleOptions) { role ->
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    AssistChip(
                        onClick = { viewModel.setTenantFilter(null) },
                        label = { Text(s(StringKey.ROOT_INVITE_ALL_TENANTS)) },
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

            OutlinedButton(onClick = { viewModel.showCreateDialog() }) { Text(s(StringKey.ROOT_INVITE_SEND_INVITE)) }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visibleInvites) { invite ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(invite.email, fontWeight = FontWeight.SemiBold)
                            Text(
                                s(StringKey.ROOT_INVITE_ROLE_TENANT_INLINE, invite.role, invite.tenantName ?: s(StringKey.ROOT_INVITE_GLOBAL)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                s(StringKey.ROOT_INVITE_STATUS_EXPIRES_INLINE, invite.status.name, invite.expiresAt),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (invite.status == InviteStatus.PENDING) {
                                OutlinedButton(onClick = { viewModel.revokeInvite(invite.id) }) {
                                    Text(s(StringKey.ROOT_INVITE_REVOKE))
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
        }
    }

    if (state.showCreateDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCreateDialog() },
            title = { Text(s(StringKey.ROOT_INVITE_CREATE_TITLE)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text(s(StringKey.EMAIL)) },
                        singleLine = true
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(roleOptions) { role ->
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
                                label = { Text(s(StringKey.ROOT_INVITE_GLOBAL)) },
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
                ) { Text(s(StringKey.ROOT_INVITE_SEND)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCreateDialog() }) { Text(s(StringKey.CANCEL)) }
            }
        )
    }
}

@Composable
fun RolesPermissionsScreen(onNavigateBack: () -> Unit = {}) {
    AppScaffold(title = s(StringKey.ROOT_ROLES_PERMISSIONS), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(s(StringKey.ROOT_RBAC_TITLE), style = MaterialTheme.typography.titleMedium)
            Text(s(StringKey.ROOT_RBAC_NOTE))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("TENANT_ADMIN", fontWeight = FontWeight.Bold)
                    Text("TENANT_USERS_READ, TENANT_SETTINGS_UPDATE, HISTORY_READ_TENANT")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("TENANT_MEMBER", fontWeight = FontWeight.Bold)
                    Text("VERIFY_SELF, ENROLL_SELF_CREATE, HISTORY_READ_SELF")
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
        }
    }
}

@Composable
fun AuditExplorerScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    AppScaffold(title = s(StringKey.ROOT_AUDIT_EXPLORER), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SearchBar(
                value = state.filter.query,
                placeholder = s(StringKey.ROOT_AUDIT_FILTER),
                onValueChange = { viewModel.onEvent(RootConsoleUiEvent.UpdateQuery(it)) }
            )
            FilterChips(
                filters = listOf("SUCCESS", "FAILED", "TENANT_UPDATE", "USER_DISABLE"),
                selected = state.filter.status,
                onSelect = { }
            )
            OutlinedButton(onClick = { viewModel.onEvent(RootConsoleUiEvent.RefreshAudit) }) { Text(s(StringKey.REFRESH)) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.auditLogs) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(s(StringKey.ROOT_AUDIT_ACTOR_ACTION, log.actor, log.action), fontWeight = FontWeight.SemiBold)
                            Text(s(StringKey.ROOT_AUDIT_STATUS_TENANT_INLINE, log.status, log.tenantId ?: s(StringKey.ROOT_INVITE_GLOBAL)))
                            Text(log.details, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
        }
    }
}

@Composable
fun SecurityEventsScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    AppScaffold(title = s(StringKey.ROOT_SECURITY_EVENTS_TITLE), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.onEvent(RootConsoleUiEvent.RefreshSecurity) }) { Text(s(StringKey.REFRESH)) }
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
            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
        }
    }
}

@Composable
fun SystemSettingsScreen(
    role: UserRole,
    onNavigateBack: () -> Unit = {},
    viewModel: RootConsoleViewModel = koinInject<RootAdminRepository>().let { repo -> remember { RootConsoleViewModel(role, repo) } }
) {
    val state by viewModel.state.collectAsState()
    var rateLimit by remember { mutableStateOf("120") }
    LaunchedEffect(Unit) { viewModel.onEvent(RootConsoleUiEvent.Load()) }
    val settings = state.settings
    AppScaffold(title = s(StringKey.ROOT_SYSTEM_SETTINGS), snackbarHostState = remember { SnackbarHostState() }) { modifier ->
        Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (settings != null) {
                Text(s(StringKey.ROOT_SETTINGS_JWT_POLICY, settings.jwtPolicySummary))
                SearchBar(
                    value = rateLimit,
                    placeholder = s(StringKey.ROOT_SETTINGS_RATE_LIMIT),
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
                ) { Text(s(StringKey.SAVE)) }
            } else {
                LoadingState()
            }
            OutlinedButton(onClick = onNavigateBack) { Text(s(StringKey.BACK)) }
        }
    }
}
