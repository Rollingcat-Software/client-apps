package com.fivucsas.mobile.android.ui.navigation

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
import com.fivucsas.mobile.android.ui.screen.ForgotPasswordScreen
import com.fivucsas.mobile.android.ui.screen.HelpScreen
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.OperatorDashboardScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.QRLoginScanScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.mobile.android.ui.screen.UsersManagementScreen
import com.fivucsas.shared.data.local.TokenManager
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

    object AdminDashboard : Screen("admin-dashboard")
    object OperatorDashboard : Screen("operator-dashboard")
    object UsersManagement : Screen("users-management")

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
    val roleValue = runCatching { tokenManager?.getRole() }.getOrNull()
    val userRole = roleValue?.let { UserRole.fromString(it) } ?: UserRole.USER
    val isAuthenticated = runCatching { tokenManager?.isAuthenticated() == true }.getOrDefault(false)
    val navItemsForRole = when (userRole) {
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
                isAuthenticated = isAuthenticated,
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
                onLoginSuccess = {
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
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                userRole = userRole,
                currentRoute = Screen.AdminDashboard.route,
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToHistory = { navController.navigate(Screen.ActivityHistory.route) },
                onNavigateToUsers = { navController.navigate(Screen.UsersManagement.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateBottom = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable(Screen.UsersManagement.route) {
            if (!userRole.hasPermission(Permission.TENANT_USERS_READ)) {
                LaunchedEffect(Unit) {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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

        composable(Screen.Profile.route) {
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
            ChangePasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onSubmit = { _, _, _ -> navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Help.route) {
            HelpScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) {
            AboutScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.QrLoginScan.route) {
            QRLoginScanScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.BiometricEnroll.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!userRole.hasPermission(Permission.ENROLL_SELF_CREATE)) {
                LaunchedEffect(Unit) {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
            if (!userRole.hasPermission(Permission.VERIFY_SELF)) {
                LaunchedEffect(Unit) {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
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
    }
}
