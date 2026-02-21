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
import com.fivucsas.mobile.android.data.AppPreferences
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
import com.fivucsas.mobile.android.ui.screen.LoginScreen
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.OnboardingScreen
import com.fivucsas.mobile.android.ui.screen.OperatorDashboardScreen
import com.fivucsas.mobile.android.ui.screen.UsersManagementScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.RegisterScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.mobile.android.ui.screen.SplashScreen
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import org.koin.compose.koinInject

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

    object AdminDashboard : Screen("admin-dashboard")
    object OperatorDashboard : Screen("operator-dashboard")
    object UsersManagement : Screen("users-management")

    object BiometricEnroll : Screen("biometric/enroll/{userId}") {
        fun createRoute(userId: String) = "biometric/enroll/$userId"
    }

    object BiometricVerify : Screen("biometric/verify/{userId}") {
        fun createRoute(userId: String) = "biometric/verify/$userId"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val tokenManager = koinInject<TokenManager>()
    val userRole = tokenManager.getRole()?.let { UserRole.fromString(it) } ?: UserRole.USER
    val navItemsForRole = when (userRole) {
        UserRole.SUPERADMIN, UserRole.ORG_ADMIN -> BottomNavDestinations.adminItems
        UserRole.OPERATOR -> BottomNavDestinations.operatorItems
        else -> BottomNavDestinations.items
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                isFirstLaunch = preferences.isFirstLaunch(),
                isAuthenticated = tokenManager.isAuthenticated(),
                userRole = tokenManager.getRole(),
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
                },
                onNavigateToOperatorDashboard = {
                    navController.navigate(Screen.OperatorDashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    preferences.setFirstLaunchCompleted()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onSkip = {
                    preferences.setFirstLaunchCompleted()
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
                        UserRole.SUPERADMIN, UserRole.ORG_ADMIN -> Screen.AdminDashboard.route
                        UserRole.OPERATOR -> Screen.OperatorDashboard.route
                        else -> Screen.Dashboard.route
                    }
                    navController.navigate(destination) {
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
            if (!userRole.hasPermission(Permission.MANAGE_USERS)) {
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

        composable(
            route = Screen.BiometricEnroll.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            if (!userRole.hasPermission(Permission.ENROLL_FACE)) {
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
            if (!userRole.hasPermission(Permission.VERIFY_FACE)) {
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
    }
}
