package com.fivucsas.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fivucsas.desktop.ui.admin.AdminDashboard
import com.fivucsas.desktop.ui.auth.GuestFaceCheckScreen
import com.fivucsas.desktop.ui.auth.QrLoginScreen
import com.fivucsas.desktop.ui.components.DesktopAppShell
import com.fivucsas.desktop.ui.components.DesktopBannerType
import com.fivucsas.desktop.ui.components.DesktopDashboardActionCard
import com.fivucsas.desktop.ui.components.DesktopInfoBanner
import com.fivucsas.desktop.ui.components.DesktopSectionHeader
import com.fivucsas.desktop.ui.root.RootDesktopAuditScreen
import com.fivucsas.desktop.ui.root.RootDesktopConsoleScreen
import com.fivucsas.desktop.ui.root.RootDesktopInviteManagementScreen
import com.fivucsas.desktop.ui.root.RootDesktopRolesPermissionsScreen
import com.fivucsas.desktop.ui.root.RootDesktopSecurityScreen
import com.fivucsas.desktop.ui.root.RootDesktopSystemSettingsScreen
import com.fivucsas.desktop.ui.root.RootDesktopTenantDetailScreen
import com.fivucsas.desktop.ui.root.RootDesktopTenantManagementScreen
import com.fivucsas.desktop.ui.root.RootDesktopUserListScreen
import com.fivucsas.desktop.ui.kiosk.KioskMode
import com.fivucsas.desktop.ui.theme.DesktopTheme
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.di.getAppModules
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginStatus
import com.fivucsas.shared.presentation.viewmodel.auth.QrLoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.ui.navigation.HomeDestination
import com.fivucsas.shared.ui.navigation.RouteIds
import com.fivucsas.shared.ui.navigation.homeDestinationFor
import com.fivucsas.shared.ui.screen.ForgotPasswordScreen
import com.fivucsas.shared.ui.screen.LoginScreen
import com.fivucsas.shared.ui.screen.RegisterScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject

class AppStateManager {
    private val _currentMode = MutableStateFlow(AppMode.LAUNCHER)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _unauthorizedMessage = MutableStateFlow("No permission.")
    val unauthorizedMessage: StateFlow<String> = _unauthorizedMessage.asStateFlow()

    fun navigateTo(mode: AppMode) { _currentMode.value = mode }
    fun navigateUnauthorized(message: String) {
        _unauthorizedMessage.value = message
        _currentMode.value = AppMode.UNAUTHORIZED
    }
}

fun main() {
    startKoin { modules(getAppModules()) }
    application {
        val stateManager = remember { AppStateManager() }
        val currentMode by stateManager.currentMode.collectAsState()
        Window(
            onCloseRequest = ::exitApplication,
            title = "FIVUCSAS - Desktop",
            state = rememberWindowState(placement = WindowPlacement.Maximized)
        ) {
            DesktopTheme {
                AppContent(
                    currentMode = currentMode,
                    onNavigate = stateManager::navigateTo,
                    onUnauthorized = stateManager::navigateUnauthorized,
                    unauthorizedMessage = stateManager.unauthorizedMessage.collectAsState().value
                )
            }
        }
    }
}

@Composable
private fun AppContent(
    currentMode: AppMode,
    onNavigate: (AppMode) -> Unit,
    onUnauthorized: (String) -> Unit,
    unauthorizedMessage: String
) {
    val tokenManager: TokenManager by inject(TokenManager::class.java)
    val qrLoginViewModel: QrLoginViewModel = koinInject()
    val loginViewModel: LoginViewModel = koinInject()
    val registerViewModel: RegisterViewModel = koinInject()
    val qrState by qrLoginViewModel.state.collectAsState()
    val currentRole = tokenManager.getRole()?.let { UserRole.fromString(it) }
    var rootSelectedTenantId by remember { mutableStateOf<String?>(null) }
    val onLogoutToLauncher = {
        tokenManager.clearTokens()
        loginViewModel.resetState()
        onNavigate(AppMode.LAUNCHER)
    }

    LaunchedEffect(currentMode) {
        if (currentMode != AppMode.QR_LOGIN) qrLoginViewModel.stopPolling()
    }

    when (currentMode) {
        AppMode.LAUNCHER -> LauncherScreen(
            onKioskSelected = { onNavigate(AppMode.KIOSK) },
            onDashboardSelected = { onNavigate(AppMode.LOGIN) },
            onGuestFaceCheckSelected = { onNavigate(AppMode.GUEST_FACE_CHECK) },
            onQrLoginSelected = { qrLoginViewModel.startDesktopSession(); onNavigate(AppMode.QR_LOGIN) }
        )
        AppMode.LOGIN -> LoginScreen(
            viewModel = loginViewModel,
            onNavigateToRegister = { onNavigate(AppMode.REGISTER) },
            onNavigateToForgotPassword = { onNavigate(AppMode.FORGOT_PASSWORD) },
            onNavigateToGuestFaceCheck = { onNavigate(AppMode.GUEST_FACE_CHECK) },
            onLoginSuccess = {
                loginViewModel.state.value.tokens?.let { tokenManager.saveTokens(it) }
                val role = loginViewModel.state.value.role ?: UserRole.USER
                onNavigate(modeForHomeDestination(homeDestinationFor(role)))
            }
        )
        AppMode.REGISTER -> RegisterScreen(
            viewModel = registerViewModel,
            onNavigateBack = { onNavigate(AppMode.LOGIN) },
            onRegisterSuccess = {
                registerViewModel.state.value.tokens?.let { tokenManager.saveTokens(it) }
                val role = registerViewModel.state.value.role ?: UserRole.USER
                onNavigate(modeForHomeDestination(homeDestinationFor(role)))
            }
        )
        AppMode.FORGOT_PASSWORD -> ForgotPasswordScreen(
            onNavigateBack = { onNavigate(AppMode.LOGIN) },
            onNavigateToLogin = { onNavigate(AppMode.LOGIN) }
        )
        AppMode.KIOSK -> KioskMode(onBack = { onNavigate(AppMode.LAUNCHER) })
        AppMode.GUEST_FACE_CHECK -> GuestFaceCheckScreen(
            onBack = { onNavigate(AppMode.LAUNCHER) },
            onBackToLogin = { onNavigate(backTargetForAuth(currentRole)) }
        )
        AppMode.QR_LOGIN -> QrLoginScreen(
            sessionCode = qrState.sessionId,
            qrPayload = qrState.qrPayload,
            status = qrState.status,
            isLoading = qrState.isLoading,
            errorMessage = qrState.error,
            onContinue = {
                if (qrState.status != QrLoginStatus.APPROVED) return@QrLoginScreen
                qrState.tokens?.let { tokenManager.saveTokens(it) }

                val roleFromSession = qrState.role
                val roleFromStorage = tokenManager.getRole()?.let { UserRole.fromString(it) }
                val effectiveRole = roleFromSession ?: roleFromStorage

                if (effectiveRole == null) {
                    onNavigate(AppMode.LOGIN)
                    return@QrLoginScreen
                }
                if (effectiveRole == UserRole.GUEST) {
                    onUnauthorized("Guest users cannot sign in with QR.")
                    return@QrLoginScreen
                }
                onNavigate(modeForHomeDestination(homeDestinationFor(effectiveRole)))
            },
            onBackToLogin = { onNavigate(backTargetForAuth(currentRole)) },
            onRefresh = { qrLoginViewModel.startDesktopSession() }
        )
        AppMode.USER_HOME -> RoleDashboard("User Dashboard", currentRole ?: UserRole.USER, userActions(), onNavigate, { onNavigate(AppMode.LAUNCHER) }) {
            onLogoutToLauncher()
        }
        AppMode.MEMBER_HOME -> RoleDashboard("Member Dashboard", currentRole ?: UserRole.TENANT_MEMBER, memberActions(), onNavigate, { onNavigate(AppMode.LAUNCHER) }) {
            onLogoutToLauncher()
        }
        AppMode.TENANT_ADMIN_HOME -> guardedComposable(currentRole, onUnauthorized, "No permission for tenant admin dashboard.", anyPermissions = setOf(Permission.TENANT_USERS_READ)) {
            AdminDashboard(
                onBack = { onNavigate(AppMode.LAUNCHER) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_HOME -> guardedComposable(currentRole, onUnauthorized, "No permission for root dashboard.", anyPermissions = setOf(Permission.PLATFORM_HEALTH_READ)) {
            RootDesktopConsoleScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onOpenTenant = { tenantId ->
                    rootSelectedTenantId = tenantId
                    onNavigate(AppMode.ROOT_TENANT_DETAIL)
                },
                onBack = { onNavigate(AppMode.LAUNCHER) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ADMIN_DASHBOARD -> guardedComposable(currentRole, onUnauthorized, "No permission for admin dashboard.", anyPermissions = setOf(Permission.TENANT_USERS_READ)) {
            AdminDashboard(onBack = { onNavigate(modeForRole(currentRole ?: UserRole.USER)) })
        }
        AppMode.ENROLL -> guardedComposable(currentRole, onUnauthorized, "No permission to enroll biometric data.", anyPermissions = setOf(Permission.ENROLL_SELF_CREATE)) {
            KioskMode(onBack = { onNavigate(modeForRole(currentRole ?: UserRole.USER)) })
        }
        AppMode.VERIFY -> guardedComposable(currentRole, onUnauthorized, "No permission to verify identity.", anyPermissions = setOf(Permission.VERIFY_SELF)) {
            KioskMode(onBack = { onNavigate(modeForRole(currentRole ?: UserRole.USER)) })
        }
        AppMode.HISTORY_SELF -> guardedComposable(currentRole, onUnauthorized, "No permission to view activity history.", anyPermissions = setOf(Permission.HISTORY_READ_SELF)) {
            PlaceholderScreen("Activity History", "Desktop history screen will be implemented here.") { onNavigate(modeForRole(currentRole ?: UserRole.USER)) }
        }
        AppMode.USERS_MANAGEMENT -> guardedComposable(currentRole, onUnauthorized, "No permission to view tenant users.", anyPermissions = setOf(Permission.TENANT_USERS_READ)) {
            PlaceholderScreen("Users Management", "Tenant users list.") { onNavigate(AppMode.TENANT_ADMIN_HOME) }
        }
        AppMode.TENANT_SETTINGS -> guardedComposable(currentRole, onUnauthorized, "No permission to view tenant settings.", anyPermissions = setOf(Permission.TENANT_SETTINGS_READ)) {
            PlaceholderScreen("Tenant Settings", "Tenant settings screen.") { onNavigate(AppMode.TENANT_ADMIN_HOME) }
        }
        AppMode.TENANT_HISTORY -> guardedComposable(currentRole, onUnauthorized, "No permission to view tenant history.", anyPermissions = setOf(Permission.HISTORY_READ_TENANT)) {
            PlaceholderScreen("Tenant History", "Tenant history and export.") { onNavigate(AppMode.TENANT_ADMIN_HOME) }
        }
        AppMode.IDENTIFY_TENANT -> guardedComposable(currentRole, onUnauthorized, "No permission for 1:N identification.", anyPermissions = setOf(Permission.IDENTIFY_TENANT)) {
            PlaceholderScreen("Identify Tenant", "1:N identification.") { onNavigate(AppMode.TENANT_ADMIN_HOME) }
        }
        AppMode.TENANT_MANAGE -> guardedComposable(currentRole, onUnauthorized, "No permission to manage tenants.", anyPermissions = setOf(Permission.TENANT_MANAGE)) {
            RootDesktopTenantManagementScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onOpenTenant = { tenantId ->
                    rootSelectedTenantId = tenantId
                    onNavigate(AppMode.ROOT_TENANT_DETAIL)
                },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.PLATFORM_HEALTH -> guardedComposable(currentRole, onUnauthorized, "No permission to read platform health.", anyPermissions = setOf(Permission.PLATFORM_HEALTH_READ)) {
            RootDesktopSecurityScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.PLATFORM_AUDIT -> guardedComposable(currentRole, onUnauthorized, "No permission to read platform audit.", anyPermissions = setOf(Permission.PLATFORM_AUDIT_READ)) {
            RootDesktopAuditScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.PLATFORM_SETTINGS -> guardedComposable(currentRole, onUnauthorized, "No permission to update platform settings.", anyPermissions = setOf(Permission.PLATFORM_SETTINGS_UPDATE)) {
            RootDesktopSystemSettingsScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_TENANT_MANAGEMENT -> guardedComposable(currentRole, onUnauthorized, "No permission to manage tenants.", anyPermissions = setOf(Permission.TENANT_MANAGE)) {
            RootDesktopTenantManagementScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onOpenTenant = { tenantId ->
                    rootSelectedTenantId = tenantId
                    onNavigate(AppMode.ROOT_TENANT_DETAIL)
                },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_TENANT_DETAIL -> guardedComposable(currentRole, onUnauthorized, "No permission to view tenant detail.", anyPermissions = setOf(Permission.TENANT_MANAGE)) {
            RootDesktopTenantDetailScreen(
                role = currentRole ?: UserRole.ROOT,
                tenantId = rootSelectedTenantId,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_TENANT_MANAGEMENT) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_GLOBAL_USER_DIRECTORY -> guardedComposable(currentRole, onUnauthorized, "No permission to view global users.", anyPermissions = setOf(Permission.TENANT_USERS_READ)) {
            RootDesktopUserListScreen(
                title = "Global User Directory",
                selected = AppMode.ROOT_GLOBAL_USER_DIRECTORY,
                role = currentRole ?: UserRole.ROOT,
                showTenantAdmins = false,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_TENANT_ADMINS -> guardedComposable(currentRole, onUnauthorized, "No permission to view tenant admins.", anyPermissions = setOf(Permission.TENANT_USERS_READ)) {
            RootDesktopUserListScreen(
                title = "Tenant Admins",
                selected = AppMode.ROOT_TENANT_ADMINS,
                role = currentRole ?: UserRole.ROOT,
                showTenantAdmins = true,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_ROLES_PERMISSIONS -> guardedComposable(currentRole, onUnauthorized, "No permission to edit role/permission matrix.", anyPermissions = setOf(Permission.TENANT_ROLES_ASSIGN)) {
            RootDesktopRolesPermissionsScreen(
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_INVITE_MANAGEMENT -> guardedComposable(currentRole, onUnauthorized, "No permission to manage invitations.", anyPermissions = setOf(Permission.TENANT_INVITE_CREATE)) {
            RootDesktopInviteManagementScreen(
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_AUDIT_EXPLORER -> guardedComposable(currentRole, onUnauthorized, "No permission for global audit.", anyPermissions = setOf(Permission.PLATFORM_AUDIT_READ)) {
            RootDesktopAuditScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_SECURITY_EVENTS -> guardedComposable(currentRole, onUnauthorized, "No permission for security events.", anyPermissions = setOf(Permission.PLATFORM_HEALTH_READ)) {
            RootDesktopSecurityScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.ROOT_SYSTEM_SETTINGS -> guardedComposable(currentRole, onUnauthorized, "No permission for system settings.", anyPermissions = setOf(Permission.PLATFORM_SETTINGS_UPDATE)) {
            RootDesktopSystemSettingsScreen(
                role = currentRole ?: UserRole.ROOT,
                onNavigate = { mode -> onNavigate(mode) },
                onBack = { onNavigate(AppMode.ROOT_HOME) },
                onLogout = { onLogoutToLauncher() }
            )
        }
        AppMode.UNAUTHORIZED -> StateCardScreen(
            title = "Access Denied",
            description = unauthorizedMessage,
            onBack = { onNavigate(backTargetForAuth(currentRole)) },
            isError = true
        )
    }
}

@Composable
private fun guardedComposable(
    role: UserRole?,
    onUnauthorized: (String) -> Unit,
    deniedMessage: String,
    anyPermissions: Set<Permission> = emptySet(),
    allPermissions: Set<Permission> = emptySet(),
    content: @Composable () -> Unit
) {
    val allowed = role != null && isAllowed(role, anyPermissions, allPermissions)
    if (!allowed) {
        LaunchedEffect(role, anyPermissions, allPermissions) { onUnauthorized(deniedMessage) }
        return
    }
    content()
}

private fun isAllowed(role: UserRole, anyPermissions: Set<Permission>, allPermissions: Set<Permission>): Boolean {
    val allGranted = allPermissions.isEmpty() || allPermissions.all { role.hasPermission(it) }
    val anyGranted = anyPermissions.isEmpty() || anyPermissions.any { role.hasPermission(it) }
    return allGranted && anyGranted
}

private data class ActionSpec(
    val label: String,
    val icon: ImageVector,
    val target: AppMode,
    val permissions: Set<Permission> = emptySet(),
    val any: Boolean = false
)

private fun userActions() = listOf(
    ActionSpec("Invitations", Icons.Default.Person, AppMode.USER_HOME, setOf(Permission.TENANT_INVITE_ACCEPT)),
    ActionSpec("Profile", Icons.Default.Person, AppMode.USER_HOME, setOf(Permission.PROFILE_READ_SELF)),
    ActionSpec("Settings / Help", Icons.Default.Settings, AppMode.USER_HOME)
)

private fun memberActions() = listOf(
    ActionSpec("Enroll", Icons.Default.Fingerprint, AppMode.ENROLL, setOf(Permission.ENROLL_SELF_CREATE)),
    ActionSpec("Verify", Icons.Default.Shield, AppMode.VERIFY, setOf(Permission.VERIFY_SELF)),
    ActionSpec("QR", Icons.Default.CameraAlt, AppMode.QR_LOGIN, setOf(Permission.QR_SCAN, Permission.QR_DISPLAY), any = true),
    ActionSpec("History", Icons.Default.History, AppMode.HISTORY_SELF, setOf(Permission.HISTORY_READ_SELF)),
    ActionSpec("Profile", Icons.Default.Person, AppMode.MEMBER_HOME, setOf(Permission.PROFILE_READ_SELF))
)

private fun tenantAdminActions() = listOf(
    ActionSpec("Admin Dashboard", Icons.Default.AdminPanelSettings, AppMode.ADMIN_DASHBOARD, setOf(Permission.TENANT_USERS_READ)),
    ActionSpec("Users Management", Icons.Default.ManageAccounts, AppMode.USERS_MANAGEMENT, setOf(Permission.TENANT_USERS_READ)),
    ActionSpec("Tenant Settings", Icons.Default.Settings, AppMode.TENANT_SETTINGS, setOf(Permission.TENANT_SETTINGS_READ)),
    ActionSpec("Tenant History", Icons.Default.History, AppMode.TENANT_HISTORY, setOf(Permission.HISTORY_READ_TENANT)),
    ActionSpec("Identify Tenant", Icons.Default.PersonSearch, AppMode.IDENTIFY_TENANT, setOf(Permission.IDENTIFY_TENANT)),
    ActionSpec("Enroll", Icons.Default.Fingerprint, AppMode.ENROLL, setOf(Permission.ENROLL_SELF_CREATE)),
    ActionSpec("Verify", Icons.Default.Shield, AppMode.VERIFY, setOf(Permission.VERIFY_SELF))
)

private fun rootActions() = listOf(
    ActionSpec("Tenant Manage", Icons.Default.ManageAccounts, AppMode.TENANT_MANAGE, setOf(Permission.TENANT_MANAGE)),
    ActionSpec("Platform Health", Icons.Default.HealthAndSafety, AppMode.PLATFORM_HEALTH, setOf(Permission.PLATFORM_HEALTH_READ)),
    ActionSpec("Platform Audit", Icons.Default.History, AppMode.PLATFORM_AUDIT, setOf(Permission.PLATFORM_AUDIT_READ)),
    ActionSpec("Platform Settings", Icons.Default.Settings, AppMode.PLATFORM_SETTINGS, setOf(Permission.PLATFORM_SETTINGS_UPDATE)),
    ActionSpec("Tenant Admin", Icons.Default.AdminPanelSettings, AppMode.TENANT_ADMIN_HOME, setOf(Permission.TENANT_USERS_READ))
)

@Composable
private fun RoleDashboard(
    title: String,
    role: UserRole,
    actions: List<ActionSpec>,
    onNavigate: (AppMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    DesktopAppShell(
        title = title,
        onBack = onBack,
        onLogout = onLogout
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            DesktopSectionHeader(
                title = title,
                subtitle = "Role: ${role.name.replace('_', ' ')}"
            )
            if (role == UserRole.USER) {
                DesktopInfoBanner(
                    type = DesktopBannerType.Warning,
                    text = "You are not a tenant member yet."
                )
            }

            val visibleActions = actions.filter { action ->
                if (action.permissions.isEmpty()) true
                else if (action.any) action.permissions.any { role.hasPermission(it) }
                else action.permissions.all { role.hasPermission(it) }
            }

            visibleActions.chunked(2).forEach { rowActions ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    rowActions.forEach { action ->
                        DesktopDashboardActionCard(
                            icon = action.icon,
                            title = action.label,
                            subtitle = "Open ${action.label.lowercase()}",
                            onClick = { onNavigate(action.target) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowActions.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, description: String, onBack: () -> Unit) {
    StateCardScreen(
        title = title,
        description = description,
        onBack = onBack,
        isError = false
    )
}

@Composable
private fun StateCardScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
    isError: Boolean
) {
    DesktopAppShell(
        title = if (isError) "Access Denied" else title,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DesktopSectionHeader(title = title, subtitle = description)
            Spacer(modifier = Modifier.height(12.dp))
            DesktopInfoBanner(
                type = if (isError) DesktopBannerType.Error else DesktopBannerType.Info,
                text = description,
                modifier = Modifier.fillMaxWidth(0.65f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Back") }
        }
    }
}

@Composable
private fun LauncherScreen(
    onKioskSelected: () -> Unit,
    onDashboardSelected: () -> Unit,
    onGuestFaceCheckSelected: () -> Unit,
    onQrLoginSelected: () -> Unit
) {
    DesktopAppShell(title = "FIVUCSAS") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DesktopSectionHeader(
                title = "FIVUCSAS",
                subtitle = "Desktop control center"
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DesktopDashboardActionCard(
                    icon = Icons.Default.TouchApp,
                    title = "Kiosk Mode",
                    subtitle = "Enrollment and verification terminal",
                    onClick = onKioskSelected,
                    modifier = Modifier.width(260.dp)
                )
                DesktopDashboardActionCard(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Dashboards",
                    subtitle = "Login and open role-based dashboard",
                    onClick = onDashboardSelected,
                    modifier = Modifier.width(260.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DesktopDashboardActionCard(
                    icon = Icons.Default.PersonSearch,
                    title = "Guest Face Check",
                    subtitle = "Verify guest identity without login",
                    onClick = onGuestFaceCheckSelected,
                    modifier = Modifier.width(260.dp)
                )
                DesktopDashboardActionCard(
                    icon = Icons.Default.CameraAlt,
                    title = "QR Login",
                    subtitle = "Sign in using mobile QR approval",
                    onClick = onQrLoginSelected,
                    modifier = Modifier.width(260.dp)
                )
            }
        }
    }
}

private fun modeForRole(role: UserRole): AppMode = modeForHomeDestination(homeDestinationFor(role))

private fun backTargetForAuth(role: UserRole?): AppMode {
    return role?.let { modeForRole(it) } ?: AppMode.LAUNCHER
}

private fun modeForHomeDestination(destination: HomeDestination): AppMode {
    return when (destination) {
        HomeDestination.UserHome -> AppMode.USER_HOME
        HomeDestination.MemberHome -> AppMode.MEMBER_HOME
        HomeDestination.TenantAdminHome -> AppMode.TENANT_ADMIN_HOME
        HomeDestination.RootHome -> AppMode.ROOT_HOME
    }
}

enum class AppMode {
    LAUNCHER,
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    KIOSK,
    GUEST_FACE_CHECK,
    QR_LOGIN,
    USER_HOME,
    MEMBER_HOME,
    TENANT_ADMIN_HOME,
    ROOT_HOME,
    ADMIN_DASHBOARD,
    ENROLL,
    VERIFY,
    HISTORY_SELF,
    USERS_MANAGEMENT,
    TENANT_SETTINGS,
    TENANT_HISTORY,
    IDENTIFY_TENANT,
    TENANT_MANAGE,
    PLATFORM_HEALTH,
    PLATFORM_AUDIT,
    PLATFORM_SETTINGS,
    ROOT_TENANT_MANAGEMENT,
    ROOT_TENANT_DETAIL,
    ROOT_GLOBAL_USER_DIRECTORY,
    ROOT_TENANT_ADMINS,
    ROOT_ROLES_PERMISSIONS,
    ROOT_INVITE_MANAGEMENT,
    ROOT_AUDIT_EXPLORER,
    ROOT_SECURITY_EVENTS,
    ROOT_SYSTEM_SETTINGS,
    UNAUTHORIZED;

    val routeId: String
        get() = when (this) {
            LAUNCHER -> RouteIds.DESKTOP_LAUNCHER
            LOGIN -> RouteIds.DESKTOP_LOGIN
            REGISTER -> RouteIds.DESKTOP_REGISTER
            FORGOT_PASSWORD -> RouteIds.DESKTOP_FORGOT_PASSWORD
            KIOSK -> RouteIds.DESKTOP_KIOSK
            GUEST_FACE_CHECK -> RouteIds.DESKTOP_GUEST_FACE_CHECK
            QR_LOGIN -> RouteIds.DESKTOP_QR_LOGIN
            USER_HOME -> RouteIds.DESKTOP_USER_HOME
            MEMBER_HOME -> RouteIds.DESKTOP_MEMBER_HOME
            TENANT_ADMIN_HOME -> RouteIds.DESKTOP_TENANT_ADMIN_HOME
            ROOT_HOME -> RouteIds.DESKTOP_ROOT_HOME
            ADMIN_DASHBOARD -> RouteIds.ADMIN_DASHBOARD
            ENROLL -> RouteIds.BIOMETRIC_ENROLL
            VERIFY -> RouteIds.BIOMETRIC_VERIFY
            HISTORY_SELF -> RouteIds.ACTIVITY_HISTORY
            USERS_MANAGEMENT -> RouteIds.USERS_MANAGEMENT
            TENANT_SETTINGS -> RouteIds.TENANT_SETTINGS
            TENANT_HISTORY -> RouteIds.TENANT_HISTORY
            IDENTIFY_TENANT -> RouteIds.IDENTIFY_TENANT
            TENANT_MANAGE -> RouteIds.TENANT_MANAGE
            PLATFORM_HEALTH -> RouteIds.PLATFORM_HEALTH
            PLATFORM_AUDIT -> RouteIds.PLATFORM_AUDIT
            PLATFORM_SETTINGS -> RouteIds.PLATFORM_SETTINGS
            ROOT_TENANT_MANAGEMENT -> RouteIds.ROOT_TENANT_MANAGEMENT
            ROOT_TENANT_DETAIL -> RouteIds.ROOT_TENANT_DETAIL
            ROOT_GLOBAL_USER_DIRECTORY -> RouteIds.ROOT_GLOBAL_USER_DIRECTORY
            ROOT_TENANT_ADMINS -> RouteIds.ROOT_TENANT_ADMINS
            ROOT_ROLES_PERMISSIONS -> RouteIds.ROOT_ROLES_PERMISSIONS
            ROOT_INVITE_MANAGEMENT -> RouteIds.ROOT_INVITE_MANAGEMENT
            ROOT_AUDIT_EXPLORER -> RouteIds.ROOT_AUDIT_EXPLORER
            ROOT_SECURITY_EVENTS -> RouteIds.ROOT_SECURITY_EVENTS
            ROOT_SYSTEM_SETTINGS -> RouteIds.ROOT_SYSTEM_SETTINGS
            UNAUTHORIZED -> RouteIds.UNAUTHORIZED
        }
}

