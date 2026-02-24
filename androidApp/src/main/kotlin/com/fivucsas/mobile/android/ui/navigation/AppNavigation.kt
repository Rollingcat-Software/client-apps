package com.fivucsas.mobile.android.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.fivucsas.mobile.android.ui.screen.ChangePasswordScreen
import com.fivucsas.mobile.android.ui.screen.DashboardScreen
import com.fivucsas.mobile.android.ui.screen.EditProfileScreen
import com.fivucsas.mobile.android.ui.screen.ExamEntryScreen
import com.fivucsas.mobile.android.ui.screen.ForgotPasswordScreen
import com.fivucsas.mobile.android.ui.screen.HelpScreen
import com.fivucsas.mobile.android.ui.screen.IdentifyTenantScreen
import com.fivucsas.mobile.android.ui.screen.InviteManagementScreen
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.OperatorDashboardScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.QRLoginScanScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.mobile.android.ui.screen.TenantSettingsScreen
import com.fivucsas.mobile.android.ui.screen.UnauthorizedScreen
import com.fivucsas.mobile.android.ui.screen.UsersManagementScreen
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.domain.model.ConfidenceBand
import com.fivucsas.shared.domain.model.GuestFaceCheckOutcome
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintUiState
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.ui.screen.FingerprintFailureScreen
import com.fivucsas.shared.ui.screen.FingerprintGateScreen
import com.fivucsas.shared.ui.screen.FingerprintSuccessScreen
import com.fivucsas.shared.ui.screen.GuestFaceCheckResultScreen
import com.fivucsas.shared.ui.screen.LoginScreen
import com.fivucsas.shared.ui.screen.OnboardingScreen
import com.fivucsas.shared.ui.screen.RegisterScreen
import com.fivucsas.shared.ui.screen.SplashScreen
import org.koin.compose.koinInject

private const val PREFS_NAME = "fivucsas_prefs"
private const val KEY_FIRST_LAUNCH = "first_launch"

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot-password")
    object Dashboard : Screen("dashboard")
    object ActivityHistory : Screen("activity-history")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit-profile")
    object ChangePassword : Screen("change-password")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Help : Screen("help")
    object About : Screen("about")
    object QrLoginScan : Screen("qr-login-scan")
    object TenantHistory : Screen("tenant-history")
    object TenantSettings : Screen("tenant-settings")
    object Unauthorized : Screen("unauthorized/{message}") {
        fun createRoute(message: String): String = "unauthorized/${Uri.encode(message)}"
    }
    object GuestFaceCheckCapture : Screen("guest-face-check")
    object GuestFaceCheckResult : Screen("guest-face-check-result/{outcome}/{confidence}") {
        fun createRoute(outcome: GuestFaceCheckOutcome, confidence: ConfidenceBand?) =
            "guest-face-check-result/${outcome.name}/${confidence?.name ?: "NONE"}"
    }

    object AdminDashboard : Screen("admin-dashboard")
    object OperatorDashboard : Screen("operator-dashboard")
    object UsersManagement : Screen("users-management")
    object ExamEntry : Screen("exam-entry")
    object IdentifyTenant : Screen("identify-tenant")
    object InviteManagement : Screen("invite-management")

    object BiometricEnroll : Screen("biometric/enroll/{userId}") {
        fun createRoute(userId: String) = "biometric/enroll/$userId"
    }

    object BiometricVerify : Screen("biometric/verify/{userId}") {
        fun createRoute(userId: String) = "biometric/verify/$userId"
    }

    object FingerprintGate : Screen("fingerprint-gate/{target}") {
        fun createRoute(target: String) = "fingerprint-gate/$target"
    }

    object FingerprintSuccess : Screen("fingerprint-success/{target}") {
        fun createRoute(target: String) = "fingerprint-success/$target"
    }

    object FingerprintFailure : Screen("fingerprint-failure/{target}") {
        fun createRoute(target: String) = "fingerprint-failure/$target"
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
    fun isAdminRole(role: UserRole): Boolean = role == UserRole.TENANT_ADMIN || role == UserRole.ROOT
    fun hasQrAccess(role: UserRole): Boolean =
        canAccessAny(role, Permission.QR_SCAN, Permission.QR_DISPLAY)
    val navItemsForRole = when (currentUserRole()) {
        UserRole.ROOT, UserRole.TENANT_ADMIN -> BottomNavDestinations.adminItems
        else -> BottomNavDestinations.items
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true),
                isAuthenticated = isAuthenticated(),
                userRole = roleValue,
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
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
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
                    val destination = when (loginRole) {
                        UserRole.ROOT, UserRole.TENANT_ADMIN -> Screen.AdminDashboard.route
                        else -> Screen.Dashboard.route
                    }
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
                    navController.navigate(Screen.Dashboard.route) {
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
            DashboardScreen(
                userName = "Test User",
                userRole = userRole,
                currentRoute = Screen.Dashboard.route,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToEnroll = { navController.navigate(Screen.BiometricEnroll.createRoute("1")) },
                onNavigateToVerify = { navController.navigate(Screen.BiometricVerify.createRoute("1")) },
                onNavigateToQrScan = { navController.navigate(Screen.QrLoginScan.route) },
                onNavigateToHistory = { navController.navigate(Screen.ActivityHistory.route) },
                onNavigateToInvitations = { navController.navigate(Screen.Profile.route) },
                onNavigateToExamEntry = { navController.navigate(Screen.ExamEntry.route) },
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
            if (!userRole.hasPermission(Permission.TENANT_USERS_READ)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to view tenant users.")
                }
                return@composable
            }
            UsersManagementScreen(
                currentRoute = Screen.UsersManagement.route,
                onNavigateBack = { navController.popBackStack() },
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
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
                onNavigateToEnroll = { navController.navigate(Screen.BiometricEnroll.createRoute("1")) },
                onNavigateToVerify = { navController.navigate(Screen.BiometricVerify.createRoute("1")) },
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
            if (!userRole.hasPermission(Permission.HISTORY_READ_SELF)) {
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
            if (!userRole.hasPermission(Permission.HISTORY_READ_TENANT)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to view tenant history.")
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
                onExport = { /* TODO: implement export */ }
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
            ProfileScreen(
                userName = "Test User",
                userEmail = "test@fivucsas.com",
                userRole = userRole,
                currentRoute = Screen.Profile.route,
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onReEnroll = { navController.navigate(Screen.BiometricEnroll.createRoute("1")) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                navItems = navItemsForRole
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
            EditProfileScreen(
                initialFirstName = "Test",
                initialLastName = "User",
                email = "test@fivucsas.com",
                initialPhone = "+1 234 567 8900",
                idNumber = "NIC-12345678",
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
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubmit = { _, _, _ -> navController.popBackStack() }
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
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
            if (!userRole.hasPermission(Permission.TENANT_SETTINGS_READ)) {
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
            if (!hasQrAccess(userRole)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission to scan/display QR.")
                }
                return@composable
            }
            QRLoginScanScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.GuestFaceCheckCapture.route) {
            val userRole = currentUserRole()
            if (!userRole.hasPermission(Permission.GUEST_FACE_CHECK)) {
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
            if (!userRole.hasPermission(Permission.GUEST_FACE_CHECK)) {
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
            if (!userRole.hasPermission(Permission.IDENTIFY_TENANT)) {
                LaunchedEffect(Unit) {
                    navigateUnauthorized("No permission for 1:N identification.")
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
            if (!userRole.hasPermission(Permission.TENANT_INVITE_CREATE)) {
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
            if (!userRole.hasPermission(Permission.ENROLL_SELF_CREATE)) {
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
            if (!userRole.hasPermission(Permission.VERIFY_SELF)) {
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
            val target = backStackEntry.arguments?.getString("target") ?: Screen.Dashboard.route
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
            val target = backStackEntry.arguments?.getString("target") ?: Screen.Dashboard.route
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
            val target = backStackEntry.arguments?.getString("target") ?: Screen.Dashboard.route
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
    }
}
