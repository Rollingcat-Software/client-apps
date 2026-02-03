package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.fivucsas.mobile.android.data.AppPreferences
import com.fivucsas.mobile.android.ui.screen.AboutScreen
import com.fivucsas.mobile.android.ui.screen.ActivityHistoryScreen
import com.fivucsas.mobile.android.ui.screen.BiometricEnrollScreen
import com.fivucsas.mobile.android.ui.screen.BiometricVerifyScreen
import com.fivucsas.mobile.android.ui.screen.ChangePasswordScreen
import com.fivucsas.mobile.android.ui.screen.DashboardScreen
import com.fivucsas.mobile.android.ui.screen.EditProfileScreen
import com.fivucsas.mobile.android.ui.screen.ForgotPasswordScreen
import com.fivucsas.mobile.android.ui.screen.HelpScreen
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.QrLoginScanScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.ui.navigation.AppRoute
import com.fivucsas.shared.ui.navigation.AppStartState
import com.fivucsas.shared.ui.navigation.RouteContent
import com.fivucsas.shared.ui.navigation.SharedAppRoot
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val tokenManager = koinInject<TokenManager>()
    val loginViewModel = koinInject<LoginViewModel>()
    val registerViewModel = koinInject<RegisterViewModel>()

    val startState = remember(preferences, tokenManager) {
        AppStartState(
            isFirstLaunch = preferences.isFirstLaunch(),
            isAuthenticated = tokenManager.isAuthenticated()
        )
    }

    val platformRoutes: Map<String, RouteContent> = mapOf(
        AppRoute.ForgotPassword.id to { navigator, _ ->
            ForgotPasswordScreen(
                onNavigateBack = { navigator.pop() },
                onNavigateToLogin = { navigator.navigate(AppRoute.Login, replace = true) }
            )
        },
        AppRoute.Dashboard.id to { navigator, _ ->
            DashboardScreen(
                userName = "Test User",
                currentRoute = AppRoute.Dashboard.id,
                onNavigateToNotifications = { navigator.navigate(AppRoute.Notifications) },
                onNavigateToProfile = { navigator.navigate(AppRoute.Profile) },
                onNavigateToEnroll = { navigator.navigate(AppRoute.BiometricEnroll("1")) },
                onNavigateToVerify = { navigator.navigate(AppRoute.BiometricVerify("1")) },
                onNavigateToQrLoginScan = { navigator.navigate(AppRoute.QrLoginScan) },
                onNavigateToHistory = { navigator.navigate(AppRoute.ActivityHistory) },
                onNavigateBottom = { routeId ->
                    val route = when (routeId) {
                        AppRoute.Dashboard.id -> AppRoute.Dashboard
                        AppRoute.ActivityHistory.id -> AppRoute.ActivityHistory
                        AppRoute.Profile.id -> AppRoute.Profile
                        else -> AppRoute.Platform(routeId)
                    }
                    navigator.navigate(route, replace = true)
                }
            )
        },
        AppRoute.ActivityHistory.id to { navigator, _ ->
            ActivityHistoryScreen(
                currentRoute = AppRoute.ActivityHistory.id,
                onNavigateBottom = { routeId ->
                    val route = when (routeId) {
                        AppRoute.Dashboard.id -> AppRoute.Dashboard
                        AppRoute.ActivityHistory.id -> AppRoute.ActivityHistory
                        AppRoute.Profile.id -> AppRoute.Profile
                        else -> AppRoute.Platform(routeId)
                    }
                    navigator.navigate(route, replace = true)
                }
            )
        },
        AppRoute.Profile.id to { navigator, _ ->
            ProfileScreen(
                userName = "Test User",
                userEmail = "test@fivucsas.com",
                currentRoute = AppRoute.Profile.id,
                onNavigateBottom = { routeId ->
                    val route = when (routeId) {
                        AppRoute.Dashboard.id -> AppRoute.Dashboard
                        AppRoute.ActivityHistory.id -> AppRoute.ActivityHistory
                        AppRoute.Profile.id -> AppRoute.Profile
                        else -> AppRoute.Platform(routeId)
                    }
                    navigator.navigate(route, replace = true)
                },
                onEditProfile = { navigator.navigate(AppRoute.EditProfile) },
                onChangePassword = { navigator.navigate(AppRoute.ChangePassword) },
                onReEnroll = { navigator.navigate(AppRoute.BiometricEnroll("1")) },
                onOpenSettings = { navigator.navigate(AppRoute.Settings) }
            )
        },
        AppRoute.EditProfile.id to { navigator, _ ->
            EditProfileScreen(
                initialFirstName = "Test",
                initialLastName = "User",
                email = "test@fivucsas.com",
                initialPhone = "+1 234 567 8900",
                idNumber = "NIC-12345678",
                onNavigateBack = { navigator.pop() },
                onSave = { _, _, _ -> navigator.pop() }
            )
        },
        AppRoute.ChangePassword.id to { navigator, _ ->
            ChangePasswordScreen(
                onNavigateBack = { navigator.pop() },
                onSubmit = { _, _, _ -> navigator.pop() }
            )
        },
        AppRoute.Settings.id to { navigator, _ ->
            SettingsScreen(
                onNavigateBack = { navigator.pop() },
                onNavigateToChangePassword = { navigator.navigate(AppRoute.ChangePassword) },
                onNavigateToHelp = { navigator.navigate(AppRoute.Help) },
                onNavigateToAbout = { navigator.navigate(AppRoute.About) }
            )
        },
        AppRoute.Notifications.id to { navigator, _ ->
            NotificationsScreen(onNavigateBack = { navigator.pop() })
        },
        AppRoute.Help.id to { navigator, _ ->
            HelpScreen(onNavigateBack = { navigator.pop() })
        },
        AppRoute.About.id to { navigator, _ ->
            AboutScreen(onNavigateBack = { navigator.pop() })
        },
        AppRoute.BIOMETRIC_ENROLL to { navigator, route ->
            val userId = (route as? AppRoute.BiometricEnroll)?.userId ?: "1"
            val viewModel = koinInject<BiometricViewModel>()
            BiometricEnrollScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navigator.pop() }
            )
        },
        AppRoute.BIOMETRIC_VERIFY to { navigator, route ->
            val userId = (route as? AppRoute.BiometricVerify)?.userId ?: "1"
            val viewModel = koinInject<BiometricViewModel>()
            BiometricVerifyScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = { navigator.pop() }
            )
        },
        AppRoute.QrLoginScan.id to { navigator, _ ->
            QrLoginScanScreen(onNavigateBack = { navigator.pop() })
        }
    )

    SharedAppRoot(
        startState = startState,
        onFirstLaunchComplete = { preferences.setFirstLaunchCompleted() },
        loginViewModel = loginViewModel,
        registerViewModel = registerViewModel,
        platformRoutes = platformRoutes
    )
}
