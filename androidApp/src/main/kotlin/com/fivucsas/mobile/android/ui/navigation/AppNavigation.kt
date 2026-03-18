package com.fivucsas.mobile.android.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fivucsas.mobile.android.ui.screen.AboutScreen
import com.fivucsas.mobile.android.ui.screen.ActivityHistoryScreen
import com.fivucsas.mobile.android.ui.screen.AdminDashboardScreen
import com.fivucsas.mobile.android.ui.screen.BiometricEnrollScreen
import com.fivucsas.mobile.android.ui.screen.BiometricVerifyScreen
import com.fivucsas.mobile.android.ui.screen.CardScanScreen
import com.fivucsas.mobile.android.ui.screen.NfcReadScreen
import com.fivucsas.mobile.android.ui.screen.ChangePasswordScreen
import com.fivucsas.mobile.android.ui.screen.DashboardScreen
import com.fivucsas.mobile.android.ui.screen.EditProfileScreen
import com.fivucsas.mobile.android.ui.screen.ExamEntryScreen
import com.fivucsas.mobile.android.ui.screen.InviteAcceptScreen
import com.fivucsas.mobile.android.ui.screen.MyInvitationsScreen
import com.fivucsas.mobile.android.ui.screen.HelpScreen
import com.fivucsas.mobile.android.ui.screen.IdentifyTenantScreen
import com.fivucsas.mobile.android.ui.screen.InviteManagementScreen
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.OperatorDashboardScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.QRLoginScanScreen
import com.fivucsas.mobile.android.ui.screen.RequestMembershipScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.mobile.android.ui.screen.TenantSettingsScreen
import com.fivucsas.mobile.android.ui.screen.UnauthorizedScreen
import com.fivucsas.mobile.android.ui.screen.UsersManagementScreen
import com.fivucsas.mobile.android.ui.screen.VoiceEnrollScreen
import com.fivucsas.mobile.android.ui.screen.EmailOtpScreen
import com.fivucsas.mobile.android.ui.screen.SmsOtpScreen
import com.fivucsas.mobile.android.ui.screen.TotpEnrollScreen
import com.fivucsas.mobile.android.ui.screen.AnalyticsScreen
import com.fivucsas.mobile.android.ui.screen.LivenessScreen
import com.fivucsas.mobile.android.ui.screen.CardDetectionScreen
import com.fivucsas.mobile.android.ui.screen.HardwareTokenScreen
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.domain.model.ConfidenceBand
import com.fivucsas.shared.domain.model.GuestFaceCheckOutcome
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.ChangePasswordViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import com.fivucsas.shared.presentation.state.FingerprintUiState
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.presentation.viewmodel.UserProfileViewModel
import androidx.compose.runtime.collectAsState
import com.fivucsas.shared.ui.screen.FingerprintFailureScreen
import com.fivucsas.shared.ui.screen.FingerprintGateScreen
import com.fivucsas.shared.ui.screen.FingerprintSuccessScreen
import com.fivucsas.shared.ui.screen.ForgotPasswordScreen
import com.fivucsas.shared.ui.screen.GuestFaceCheckResultScreen
import com.fivucsas.shared.ui.screen.LoginScreen
import com.fivucsas.shared.ui.screen.OnboardingScreen
import com.fivucsas.shared.ui.screen.RegisterScreen
import com.fivucsas.shared.ui.screen.SplashScreen
import com.fivucsas.shared.ui.screen.root.AuditExplorerScreen
import com.fivucsas.shared.ui.screen.root.GlobalUserDirectoryScreen
import com.fivucsas.shared.ui.screen.root.RootConsoleScreen
import com.fivucsas.shared.ui.screen.root.RootInviteManagementScreen
import com.fivucsas.shared.ui.screen.root.RolesPermissionsScreen
import com.fivucsas.shared.ui.screen.root.SecurityEventsScreen
import com.fivucsas.shared.ui.screen.root.SystemSettingsScreen
import com.fivucsas.shared.ui.screen.root.TenantAdminsScreen
import com.fivucsas.shared.ui.screen.root.TenantDetailScreen
import com.fivucsas.shared.ui.screen.root.TenantManagementScreen
import com.fivucsas.shared.ui.navigation.NavigationPolicy
import com.fivucsas.shared.ui.navigation.RouteIds
import org.koin.compose.koinInject

private const val PREFS_NAME = "fivucsas_prefs"
private const val KEY_FIRST_LAUNCH = "first_launch"

sealed class Screen(val route: String) {
    object Splash : Screen(RouteIds.SPLASH)
    object Onboarding : Screen(RouteIds.ONBOARDING)
    object Login : Screen(RouteIds.LOGIN)
    object Register : Screen(RouteIds.REGISTER)
    object ForgotPassword : Screen(RouteIds.FORGOT_PASSWORD)
    object Dashboard : Screen(RouteIds.DASHBOARD)
    object ActivityHistory : Screen(RouteIds.ACTIVITY_HISTORY)
    object Profile : Screen(RouteIds.PROFILE)
    object EditProfile : Screen(RouteIds.EDIT_PROFILE)
    object ChangePassword : Screen(RouteIds.CHANGE_PASSWORD)
    object Settings : Screen(RouteIds.SETTINGS)
    object Notifications : Screen(RouteIds.NOTIFICATIONS)
    object Help : Screen(RouteIds.HELP)
    object About : Screen(RouteIds.ABOUT)
    object QrLoginScan : Screen(RouteIds.QR_LOGIN_SCAN)
    object TenantHistory : Screen(RouteIds.TENANT_HISTORY)
    object TenantSettings : Screen(RouteIds.TENANT_SETTINGS)
    object Unauthorized : Screen("${RouteIds.UNAUTHORIZED}/{message}") {
        fun createRoute(message: String): String = "${RouteIds.UNAUTHORIZED}/${Uri.encode(message)}"
    }
    object GuestFaceCheckCapture : Screen(RouteIds.GUEST_FACE_CHECK_CAPTURE)
    object GuestFaceCheckResult : Screen("${RouteIds.GUEST_FACE_CHECK_RESULT}/{outcome}/{confidence}") {
        fun createRoute(outcome: GuestFaceCheckOutcome, confidence: ConfidenceBand?) =
            "${RouteIds.GUEST_FACE_CHECK_RESULT}/${outcome.name}/${confidence?.name ?: "NONE"}"
    }

    object AdminDashboard : Screen(RouteIds.ADMIN_DASHBOARD)
    object OperatorDashboard : Screen(RouteIds.OPERATOR_DASHBOARD)
    object UsersManagement : Screen(RouteIds.USERS_MANAGEMENT)
    object ExamEntry : Screen(RouteIds.EXAM_ENTRY)
    object IdentifyTenant : Screen(RouteIds.IDENTIFY_TENANT)
    object InviteAccept : Screen(RouteIds.INVITE_ACCEPT)
    object InviteManagement : Screen(RouteIds.INVITE_MANAGEMENT)
    object MyInvitations : Screen(RouteIds.MY_INVITATIONS)
    object RequestMembership : Screen(RouteIds.REQUEST_MEMBERSHIP)
    object CardScan : Screen(RouteIds.CARD_SCAN)
    object NfcRead : Screen(RouteIds.NFC_READ)
    object RootConsole : Screen(RouteIds.ROOT_CONSOLE)
    object RootTenantManagement : Screen(RouteIds.ROOT_TENANT_MANAGEMENT)
    object RootTenantDetail : Screen("${RouteIds.ROOT_TENANT_DETAIL}/{tenantId}") {
        fun createRoute(tenantId: String): String = "${RouteIds.ROOT_TENANT_DETAIL}/$tenantId"
    }
    object RootGlobalUserDirectory : Screen(RouteIds.ROOT_GLOBAL_USER_DIRECTORY)
    object RootUsers : Screen(RouteIds.ROOT_USERS)
    object RootTenantMembers : Screen(RouteIds.ROOT_TENANT_MEMBERS)
    object RootTenantAdmins : Screen(RouteIds.ROOT_TENANT_ADMINS)
    object RootInviteManagement : Screen(RouteIds.ROOT_INVITE_MANAGEMENT)
    object RootRolesPermissions : Screen(RouteIds.ROOT_ROLES_PERMISSIONS)
    object RootAuditExplorer : Screen(RouteIds.ROOT_AUDIT_EXPLORER)
    object RootSecurityEvents : Screen(RouteIds.ROOT_SECURITY_EVENTS)
    object RootSystemSettings : Screen(RouteIds.ROOT_SYSTEM_SETTINGS)

    object VoiceAuth : Screen("${RouteIds.VOICE_AUTH}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.VOICE_AUTH}/$userId"
    }
    object EmailOtp : Screen("${RouteIds.EMAIL_OTP}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.EMAIL_OTP}/$userId"
    }
    object SmsOtp : Screen("${RouteIds.SMS_OTP}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.SMS_OTP}/$userId"
    }
    object TotpEnroll : Screen("${RouteIds.TOTP_ENROLL}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.TOTP_ENROLL}/$userId"
    }
    object Analytics : Screen(RouteIds.ANALYTICS)
    object LivenessPuzzle : Screen(RouteIds.LIVENESS_PUZZLE)
    object CardDetection : Screen(RouteIds.CARD_DETECTION)
    object HardwareToken : Screen(RouteIds.HARDWARE_TOKEN)

    object AuthFlows : Screen("${RouteIds.AUTH_FLOWS}/{tenantId}") {
        fun createRoute(tenantId: String) = "${RouteIds.AUTH_FLOWS}/$tenantId"
    }
    object Sessions : Screen(RouteIds.SESSIONS)
    object Devices : Screen("${RouteIds.DEVICES}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.DEVICES}/$userId"
    }
    object EnrollmentsList : Screen("${RouteIds.ENROLLMENTS_LIST}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.ENROLLMENTS_LIST}/$userId"
    }

    object BiometricEnroll : Screen("${RouteIds.BIOMETRIC_ENROLL}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.BIOMETRIC_ENROLL}/$userId"
    }

    object BiometricVerify : Screen("${RouteIds.BIOMETRIC_VERIFY}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.BIOMETRIC_VERIFY}/$userId"
    }

    object FingerprintGate : Screen("${RouteIds.FINGERPRINT_GATE_ANDROID}/{target}") {
        fun createRoute(target: String) = "${RouteIds.FINGERPRINT_GATE_ANDROID}/${Uri.encode(target)}"
    }

    object FingerprintSuccess : Screen("${RouteIds.FINGERPRINT_SUCCESS_ANDROID}/{target}") {
        fun createRoute(target: String) = "${RouteIds.FINGERPRINT_SUCCESS_ANDROID}/${Uri.encode(target)}"
    }

    object FingerprintFailure : Screen("${RouteIds.FINGERPRINT_FAILURE_ANDROID}/{target}") {
        fun createRoute(target: String) = "${RouteIds.FINGERPRINT_FAILURE_ANDROID}/${Uri.encode(target)}"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    }
    val tokenManager = runCatching { koinInject<TokenManager>() }.getOrNull()
    val roleValue = runCatching { tokenManager?.getRole() }
        .getOrNull()
        ?.let { UserRole.fromString(it) }
    fun isAuthenticated(): Boolean =
        runCatching { tokenManager?.isAuthenticated() == true }.getOrDefault(false)
    fun currentUserRole(): UserRole {
        if (!isAuthenticated()) return UserRole.GUEST
        val role = runCatching { tokenManager?.getRole() }.getOrNull()
        return role?.let { UserRole.fromString(it) } ?: UserRole.USER
    }
    fun navigateUnauthorized(message: String) {
        navController.navigate(Screen.Unauthorized.createRoute(message)) {
            launchSingleTop = true
        }
    }
    fun isAdminRole(role: UserRole): Boolean =
        NavigationPolicy.canAccessRoute(role, RouteIds.ADMIN_DASHBOARD)
    fun hasQrAccess(role: UserRole): Boolean =
        NavigationPolicy.canAccessRoute(role, RouteIds.QR_LOGIN_SCAN)
    val navItemsForRole = when (currentUserRole()) {
        UserRole.ROOT -> BottomNavDestinations.rootItems
        UserRole.TENANT_ADMIN -> BottomNavDestinations.adminItems
        UserRole.USER -> BottomNavDestinations.userItems
        else -> BottomNavDestinations.items
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val isAuth = isAuthenticated()
            val splashRole = if (isAuth) currentUserRole() else null
            SplashScreen(
                isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true),
                isAuthenticated = isAuth,
                userRole = splashRole,
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    val dest = NavigationPolicy.loginSuccessRoute(currentUserRole())
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    val dest = NavigationPolicy.loginSuccessRoute(currentUserRole())
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val viewModel = koinInject<LoginViewModel>()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateToGuestFaceCheck = { navController.navigate(Screen.GuestFaceCheckCapture.route) },
                onLoginSuccess = {
                    viewModel.state.value.tokens?.let { tokenManager?.saveTokens(it) }
                    val loginRole = viewModel.state.value.role
                    val destination = NavigationPolicy.loginSuccessRoute(loginRole)
                    navController.navigate(Screen.FingerprintGate.createRoute(destination)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val viewModel = koinInject<RegisterViewModel>()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    viewModel.state.value.tokens?.let { tokenManager?.saveTokens(it) }
                    val registerRole = viewModel.state.value.role
                    val destination = NavigationPolicy.loginSuccessRoute(registerRole)
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            val dashboardUserName = tokenManager?.getUserName() ?: "User"
            DashboardScreen(
                userName = dashboardUserName,
                userRole = userRole,
                navItems = navItemsForRole,
                currentRoute = Screen.Dashboard.route,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToEnroll = { navController.navigate(Screen.BiometricEnroll.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToVerify = { navController.navigate(Screen.BiometricVerify.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToQrScan = { navController.navigate(Screen.QrLoginScan.route) },
                onNavigateToHistory = { navController.navigate(Screen.ActivityHistory.route) },
                onNavigateToInvitations = { navController.navigate(Screen.InviteAccept.route) },
                onNavigateToExamEntry = { navController.navigate(Screen.ExamEntry.route) },
                onNavigateToRequestMembership = { navController.navigate(Screen.RequestMembership.route) },
                onNavigateToCardScan = { navController.navigate(Screen.CardScan.route) },
                onNavigateToNfcRead = { navController.navigate(Screen.NfcRead.route) },
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!isAdminRole(userRole)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission for admin dashboard.")
                }
                return@composable
            }
            AdminDashboardScreen(
                userRole = userRole,
                currentRoute = Screen.AdminDashboard.route,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToHistory = { navController.navigate(Screen.TenantHistory.route) },
                onNavigateToUsers = { navController.navigate(Screen.UsersManagement.route) },
                onNavigateToSettings = { navController.navigate(Screen.TenantSettings.route) },
                onNavigateToIdentify = { navController.navigate(Screen.IdentifyTenant.route) },
                onNavigateToInvitations = { navController.navigate(Screen.InviteManagement.route) },
                onNavigateToExamEntry = { navController.navigate(Screen.ExamEntry.route) },
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.RootConsole.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_CONSOLE)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission for root console.") }
                return@composable
            }
            RootConsoleScreen(
                role = userRole,
                currentRoute = Screen.RootConsole.route,
                settingsRoute = RouteIds.SETTINGS,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateBottom = { route ->
                    val destination = when (route) {
                        RouteIds.TENANT_HISTORY -> Screen.RootAuditExplorer.route
                        else -> route
                    }
                    navController.navigate(destination) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigate = { route, arg ->
                    when (route) {
                        RouteIds.SETTINGS -> navController.navigate(Screen.Settings.route)
                        RouteIds.ROOT_TENANT_MANAGEMENT -> navController.navigate(Screen.RootTenantManagement.route)
                        RouteIds.ROOT_GLOBAL_USER_DIRECTORY -> navController.navigate(Screen.RootGlobalUserDirectory.route)
                        RouteIds.ROOT_USERS -> navController.navigate(Screen.RootUsers.route)
                        RouteIds.ROOT_TENANT_MEMBERS -> navController.navigate(Screen.RootTenantMembers.route)
                        RouteIds.ROOT_TENANT_ADMINS -> navController.navigate(Screen.RootTenantAdmins.route)
                        RouteIds.ROOT_INVITE_MANAGEMENT -> navController.navigate(Screen.RootInviteManagement.route)
                        RouteIds.ROOT_ROLES_PERMISSIONS -> navController.navigate(Screen.RootRolesPermissions.route)
                        RouteIds.ROOT_AUDIT_EXPLORER -> navController.navigate(Screen.RootAuditExplorer.route)
                        RouteIds.ROOT_SECURITY_EVENTS -> navController.navigate(Screen.RootSecurityEvents.route)
                        RouteIds.ROOT_SYSTEM_SETTINGS -> navController.navigate(Screen.RootSystemSettings.route)
                        RouteIds.ROOT_TENANT_DETAIL -> arg?.let { navController.navigate(Screen.RootTenantDetail.createRoute(it)) }
                    }
                }
            )
        }

        composable(Screen.RootTenantManagement.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_TENANT_MANAGEMENT)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to manage tenants.") }
                return@composable
            }
            TenantManagementScreen(
                role = userRole,
                onOpenTenant = { tenantId -> navController.navigate(Screen.RootTenantDetail.createRoute(tenantId)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RootTenantDetail.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_TENANT_DETAIL)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to view tenant detail.") }
                return@composable
            }
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: return@composable
            TenantDetailScreen(
                role = userRole,
                tenantId = tenantId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RootGlobalUserDirectory.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_GLOBAL_USER_DIRECTORY)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to view global users.") }
                return@composable
            }
            GlobalUserDirectoryScreen(role = userRole, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RootUsers.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_USERS)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to view users.") }
                return@composable
            }
            GlobalUserDirectoryScreen(
                role = userRole,
                screenTitle = "Users",
                initialRoleFilter = "USER",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RootTenantMembers.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_TENANT_MEMBERS)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to view tenant members.") }
                return@composable
            }
            GlobalUserDirectoryScreen(
                role = userRole,
                screenTitle = "Tenant Members",
                initialRoleFilter = "TENANT_MEMBER",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RootTenantAdmins.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_TENANT_ADMINS)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to view tenant admins.") }
                return@composable
            }
            GlobalUserDirectoryScreen(
                role = userRole,
                screenTitle = "Tenant Admins",
                initialRoleFilter = "TENANT_ADMIN",
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RootInviteManagement.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_INVITE_MANAGEMENT)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission to manage invitations.") }
                return@composable
            }
            RootInviteManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RootRolesPermissions.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_ROLES_PERMISSIONS)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission for role/permission editor.") }
                return@composable
            }
            RolesPermissionsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RootAuditExplorer.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_AUDIT_EXPLORER)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission for global audit.") }
                return@composable
            }
            AuditExplorerScreen(role = userRole, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RootSecurityEvents.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_SECURITY_EVENTS)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission for security events.") }
                return@composable
            }
            SecurityEventsScreen(role = userRole, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.RootSystemSettings.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ROOT_SYSTEM_SETTINGS)) {
                LaunchedEffect(Unit) { navigateUnauthorized("No permission for system settings.") }
                return@composable
            }
            SystemSettingsScreen(role = userRole, onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.UsersManagement.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.USERS_MANAGEMENT)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to view tenant users.")
                }
                return@composable
            }
            UsersManagementScreen(
                currentRoute = Screen.UsersManagement.route,
                userRole = userRole,
                onNavigateBack = { navController.popBackStack() },
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToEnrollUser = { userId ->
                    navController.navigate(Screen.BiometricEnroll.createRoute(userId))
                }
            )
        }

        composable(Screen.OperatorDashboard.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            OperatorDashboardScreen(
                currentRoute = Screen.OperatorDashboard.route,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToEnroll = { navController.navigate(Screen.BiometricEnroll.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToVerify = { navController.navigate(Screen.BiometricVerify.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToHistory = { navController.navigate(Screen.ActivityHistory.route) },
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.ActivityHistory.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.ACTIVITY_HISTORY)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to view your activity history.")
                }
                return@composable
            }
            ActivityHistoryScreen(
                currentRoute = Screen.ActivityHistory.route,
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                navItems = navItemsForRole
            )
        }

        composable(Screen.TenantHistory.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.TENANT_HISTORY)) {
                LaunchedEffect(Unit) {
                    if (NavigationPolicy.canAccessRoute(userRole, RouteIds.ACTIVITY_HISTORY)) {
                        navController.navigate(Screen.ActivityHistory.route) {
                            popUpTo(Screen.TenantHistory.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        navigateUnauthorized("No permission to view tenant history.")
                    }
                }
                return@composable
            }
            ActivityHistoryScreen(
                currentRoute = Screen.TenantHistory.route,
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                navItems = BottomNavDestinations.adminItems,
                showExportButton = userRole.hasPermission(Permission.HISTORY_EXPORT_TENANT),
                onExport = { /* Export feature not yet available */ }
            )
        }

        composable(Screen.Profile.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            val profileVm = koinInject<UserProfileViewModel>()
            val profileState by profileVm.state.collectAsState()
            LaunchedEffect(Unit) { profileVm.loadProfile() }
            val profileNavItems = when (userRole) {
                UserRole.ROOT -> BottomNavDestinations.rootItems
                UserRole.TENANT_ADMIN -> BottomNavDestinations.adminItems
                UserRole.USER -> BottomNavDestinations.userItems
                else -> BottomNavDestinations.items
            }
            val profileUserName = profileState.user?.name ?: tokenManager?.getUserName() ?: "User"
            val profileUserEmail = profileState.user?.email ?: tokenManager?.getUserEmail() ?: ""
            ProfileScreen(
                userName = profileUserName,
                userEmail = profileUserEmail,
                userRole = userRole,
                userPhone = profileState.user?.phoneNumber ?: "",
                enrollmentDate = profileState.user?.enrollmentDate ?: "",
                isLoading = profileState.isLoading,
                errorMessage = profileState.errorMessage,
                currentRoute = Screen.Profile.route,
                onNavigateBottom = { route ->
                    val destination = if (userRole == UserRole.ROOT && route == RouteIds.ADMIN_DASHBOARD) {
                        Screen.RootConsole.route
                    } else {
                        route
                    }
                    navController.navigate(destination) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onReEnroll = { navController.navigate(Screen.BiometricEnroll.createRoute(tokenManager?.getUserId() ?: "me")) },
                onDeleteEnrollment = { /* Enrollment deletion not yet available */ },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                navItems = profileNavItems
            )
        }

        composable(Screen.EditProfile.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val editProfileVm = koinInject<UserProfileViewModel>()
            val editProfileState by editProfileVm.state.collectAsState()
            LaunchedEffect(Unit) { editProfileVm.loadProfile() }
            val editUser = editProfileState.user
            val editNameParts = (editUser?.name ?: tokenManager?.getUserName() ?: "").split(" ", limit = 2)
            EditProfileScreen(
                initialFirstName = editNameParts.getOrElse(0) { "" },
                initialLastName = editNameParts.getOrElse(1) { "" },
                email = editUser?.email ?: tokenManager?.getUserEmail() ?: "",
                initialPhone = editUser?.phoneNumber ?: "",
                idNumber = editUser?.idNumber ?: "",
                onNavigateBack = { navController.popBackStack() },
                onSave = { _, _, _ -> navController.popBackStack() }
            )
        }

        composable(Screen.ChangePassword.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val changePasswordVm = koinInject<ChangePasswordViewModel>()
            val cpState by changePasswordVm.state.collectAsState()
            LaunchedEffect(cpState.isSuccess) {
                if (cpState.isSuccess) {
                    navController.popBackStack()
                }
            }
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubmit = { current, newPw, confirm ->
                    changePasswordVm.changePassword(current, newPw, confirm)
                },
                isLoading = cpState.isLoading,
                errorMessage = cpState.errorMessage,
                onClearError = { changePasswordVm.clearError() }
            )
        }

        composable(Screen.Settings.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            SettingsScreen(
                userRole = currentUserRole(),
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToVoiceAuth = { navController.navigate(Screen.VoiceAuth.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToEmailOtp = { navController.navigate(Screen.EmailOtp.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToSmsOtp = { navController.navigate(Screen.SmsOtp.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToTotpEnroll = { navController.navigate(Screen.TotpEnroll.createRoute(tokenManager?.getUserId() ?: "me")) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToLiveness = { navController.navigate(Screen.LivenessPuzzle.route) },
                onNavigateToCardDetection = { navController.navigate(Screen.CardDetection.route) },
                onNavigateToHardwareToken = { navController.navigate(Screen.HardwareToken.route) },
                onLogout = {
                    tokenManager?.clearTokens()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TenantSettings.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.TENANT_SETTINGS)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to view tenant settings.")
                }
                return@composable
            }
            TenantSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Help.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.QrLoginScan.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.QR_LOGIN_SCAN)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to scan/display QR.")
                }
                return@composable
            }
            QRLoginScanScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.GuestFaceCheckCapture.route) {
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.GUEST_FACE_CHECK_CAPTURE)) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val viewModel = koinInject<BiometricViewModel>()
            BiometricVerifyScreen(
                userId = "guest",
                viewModel = viewModel,
                guestMode = true,
                onGuestResult = { outcome, confidence ->
                    navController.navigate(
                        Screen.GuestFaceCheckResult.createRoute(
                            outcome = outcome,
                            confidence = confidence
                        )
                    )
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GuestFaceCheckResult.route,
            arguments = listOf(
                navArgument("outcome") { type = NavType.StringType },
                navArgument("confidence") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.GUEST_FACE_CHECK_CAPTURE)) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }

            val outcomeValue = backStackEntry.arguments?.getString("outcome")
            val confidenceValue = backStackEntry.arguments?.getString("confidence")
            val outcome = runCatching { GuestFaceCheckOutcome.valueOf(outcomeValue ?: "") }
                .getOrDefault(GuestFaceCheckOutcome.NOT_FOUND)
            val confidenceBand = if (confidenceValue == null || confidenceValue == "NONE") {
                null
            } else {
                runCatching { ConfidenceBand.valueOf(confidenceValue) }.getOrNull()
            }

            GuestFaceCheckResultScreen(
                outcome = outcome,
                confidenceBand = confidenceBand,
                onRetry = { navController.navigate(Screen.GuestFaceCheckCapture.route) },
                onLoginToContinue = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.GuestFaceCheckCapture.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.InviteAccept.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.INVITE_ACCEPT)) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Unauthorized.createRoute("No permission to accept invitations.")) {
                        popUpTo(Screen.InviteAccept.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                return@composable
            }
            InviteAcceptScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MyInvitations.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.MY_INVITATIONS)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to view invitations.")
                }
                return@composable
            }
            MyInvitationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RequestMembership.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.REQUEST_MEMBERSHIP)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to request tenant membership.")
                }
                return@composable
            }
            RequestMembershipScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.CardScan.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.CARD_SCAN)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to scan ID cards.")
                }
                return@composable
            }
            CardScanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.NfcRead.route) {
            NfcReadScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ExamEntry.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            ExamEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.IdentifyTenant.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.IDENTIFY_TENANT)) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Unauthorized.createRoute("No permission for 1:N identification.")) {
                        popUpTo(Screen.IdentifyTenant.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                return@composable
            }
            IdentifyTenantScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.InviteManagement.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.INVITE_MANAGEMENT)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to manage invitations.")
                }
                return@composable
            }
            InviteManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BiometricEnroll.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.BIOMETRIC_ENROLL)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to enroll biometric data.")
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<BiometricViewModel>()
            BiometricEnrollScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BiometricVerify.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val userRole = currentUserRole()
            if (!NavigationPolicy.canAccessRoute(userRole, RouteIds.BIOMETRIC_VERIFY)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to verify biometric data.")
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<BiometricViewModel>()
            BiometricVerifyScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FingerprintGate.route,
            arguments = listOf(navArgument("target") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val target = backStackEntry.arguments?.getString("target")?.let(Uri::decode) ?: Screen.Dashboard.route
            val viewModel = koinInject<FingerprintViewModel>()
            FingerprintGateScreen(
                viewModel = viewModel,
                onStart = { viewModel.startStepUp() },
                onSkip = {
                    navController.navigate(target) {
                        popUpTo(Screen.FingerprintGate.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onSuccess = { navController.navigate(Screen.FingerprintSuccess.createRoute(target)) },
                onFailure = { navController.navigate(Screen.FingerprintFailure.createRoute(target)) }
            )
        }

        composable(
            route = Screen.FingerprintSuccess.route,
            arguments = listOf(navArgument("target") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val target = backStackEntry.arguments?.getString("target")?.let(Uri::decode) ?: Screen.Dashboard.route
            val viewModel = koinInject<FingerprintViewModel>()
            val stepUpToken = (viewModel.state.value as? FingerprintUiState.Success)?.stepUpToken
            FingerprintSuccessScreen(
                stepUpToken = stepUpToken,
                onContinue = {
                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.FingerprintFailure.route,
            arguments = listOf(navArgument("target") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            val target = backStackEntry.arguments?.getString("target")?.let(Uri::decode) ?: Screen.Dashboard.route
            val viewModel = koinInject<FingerprintViewModel>()
            val failureState = viewModel.state.value as? FingerprintUiState.Error
            FingerprintFailureScreen(
                message = failureState?.message ?: "Fingerprint verification failed.",
                recoverable = failureState?.recoverable ?: true,
                onRetry = {
                    navController.navigate(Screen.FingerprintGate.createRoute(target))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Unauthorized.route,
            arguments = listOf(navArgument("message") { type = NavType.StringType })
        ) { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: "No permission."
            UnauthorizedScreen(
                message = message,
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Auth Flows screen
        composable(
            route = Screen.AuthFlows.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val tenantId = backStackEntry.arguments?.getString("tenantId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.AuthFlowViewModel>()
            com.fivucsas.shared.ui.screen.AuthFlowsScreen(
                viewModel = viewModel,
                tenantId = tenantId,
                onBack = { navController.popBackStack() }
            )
        }

        // Sessions screen
        composable(Screen.Sessions.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.SessionViewModel>()
            com.fivucsas.shared.ui.screen.SessionsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Devices screen
        composable(
            route = Screen.Devices.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.DeviceViewModel>()
            com.fivucsas.shared.ui.screen.DevicesScreen(
                viewModel = viewModel,
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        // Enrollments screen
        composable(
            route = Screen.EnrollmentsList.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.EnrollmentViewModel>()
            com.fivucsas.shared.ui.screen.EnrollmentsScreen(
                viewModel = viewModel,
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        // Voice Auth screen
        composable(
            route = Screen.VoiceAuth.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.VoiceViewModel>()
            VoiceEnrollScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Email OTP screen
        composable(
            route = Screen.EmailOtp.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.OtpViewModel>()
            EmailOtpScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // SMS OTP screen
        composable(
            route = Screen.SmsOtp.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.OtpViewModel>()
            SmsOtpScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // TOTP Enroll screen
        composable(
            route = Screen.TotpEnroll.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.TotpViewModel>()
            TotpEnrollScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Analytics screen
        composable(Screen.Analytics.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.AnalyticsViewModel>()
            AnalyticsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // P1-4: Liveness Puzzle screen
        composable(Screen.LivenessPuzzle.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.LivenessViewModel>()
            LivenessScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // P1-5: Card Detection screen
        composable(Screen.CardDetection.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.CardDetectionViewModel>()
            CardDetectionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // P1-6: Hardware Token screen
        composable(Screen.HardwareToken.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.HardwareTokenViewModel>()
            HardwareTokenScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
