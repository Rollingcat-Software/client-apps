package com.fivucsas.mobile.android.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.OperatorDashboardScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.QrLoginScanScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.mobile.android.ui.screen.UsersManagementScreen
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.domain.model.BiometricError
import com.fivucsas.shared.domain.model.BiometricStepUpException
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.domain.usecase.auth.BiometricStepUpUseCase
import com.fivucsas.shared.presentation.viewmodel.SecuritySettingsViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.ui.navigation.AppRoute
import com.fivucsas.shared.ui.navigation.AppStartState
import com.fivucsas.shared.ui.navigation.RouteContent
import com.fivucsas.shared.ui.navigation.SharedAppRoot
import com.fivucsas.shared.ui.screen.SecuritySettingsScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// Route IDs for admin/operator screens (platform-specific, not in shared AppRoute)
private const val ROUTE_ADMIN_DASHBOARD = "admin-dashboard"
private const val ROUTE_OPERATOR_DASHBOARD = "operator-dashboard"
private const val ROUTE_USERS_MANAGEMENT = "users-management"
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val tokenManager = koinInject<TokenManager>()
    val loginViewModel = koinInject<LoginViewModel>()
    val registerViewModel = koinInject<RegisterViewModel>()

    // Role-based features
    val userRole = tokenManager.getRole()?.let { UserRole.fromString(it) } ?: UserRole.USER

    val startState = remember(preferences, tokenManager) {
        AppStartState(
            isFirstLaunch = preferences.isFirstLaunch(),
            isAuthenticated = tokenManager.isAuthenticated(),
            userRole = tokenManager.getRole()
        )
    }

    // Compute nav items for role
    val navItemsForRole = when (userRole) {
        UserRole.SUPERADMIN, UserRole.ORG_ADMIN -> BottomNavDestinations.adminItems
        UserRole.OPERATOR -> BottomNavDestinations.operatorItems
        else -> BottomNavDestinations.items
    }

    val platformRoutes: Map<String, RouteContent> = mapOf(
        AppRoute.ForgotPassword.id to { navigator, _ ->
            ForgotPasswordScreen(
                onNavigateBack = { navigator.pop() },
                onNavigateToLogin = { navigator.navigate(AppRoute.Login, replace = true) }
            )
        },

        // --- User Dashboard ---
        AppRoute.Dashboard.id to { navigator, _ ->
            DashboardScreen(
                userName = "Test User",
                userRole = userRole,
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

        // --- Admin Dashboard ---
        ROUTE_ADMIN_DASHBOARD to { navigator, _ ->
            AdminDashboardScreen(
                userRole = userRole,
                currentRoute = ROUTE_ADMIN_DASHBOARD,
                onNavigateToNotifications = { navigator.navigate(AppRoute.Notifications) },
                onNavigateToProfile = { navigator.navigate(AppRoute.Profile) },
                onNavigateToHistory = { navigator.navigate(AppRoute.ActivityHistory) },
                onNavigateToUsers = { navigator.navigate(AppRoute.Platform(ROUTE_USERS_MANAGEMENT)) },
                onNavigateToSettings = { navigator.navigate(AppRoute.Settings) },
                onNavigateBottom = { routeId ->
                    val route = when (routeId) {
                        ROUTE_ADMIN_DASHBOARD -> AppRoute.Platform(ROUTE_ADMIN_DASHBOARD)
                        AppRoute.ActivityHistory.id -> AppRoute.ActivityHistory
                        AppRoute.Profile.id -> AppRoute.Profile
                        else -> AppRoute.Platform(routeId)
                    }
                    navigator.navigate(route, replace = true)
                }
            )
        },

        // --- Users Management (permission guarded) ---
        ROUTE_USERS_MANAGEMENT to { navigator, _ ->
            if (!userRole.hasPermission(Permission.MANAGE_USERS)) {
                LaunchedEffect(Unit) { navigator.pop() }
            } else {
                UsersManagementScreen(
                    currentRoute = ROUTE_USERS_MANAGEMENT,
                    onNavigateBack = { navigator.pop() },
                    onNavigateBottom = { routeId ->
                        val route = when (routeId) {
                            ROUTE_ADMIN_DASHBOARD -> AppRoute.Platform(ROUTE_ADMIN_DASHBOARD)
                            AppRoute.ActivityHistory.id -> AppRoute.ActivityHistory
                            AppRoute.Profile.id -> AppRoute.Profile
                            else -> AppRoute.Platform(routeId)
                        }
                        navigator.navigate(route, replace = true)
                    }
                )
            }
        },

        // --- Operator Dashboard ---
        ROUTE_OPERATOR_DASHBOARD to { navigator, _ ->
            OperatorDashboardScreen(
                currentRoute = ROUTE_OPERATOR_DASHBOARD,
                onNavigateToNotifications = { navigator.navigate(AppRoute.Notifications) },
                onNavigateToProfile = { navigator.navigate(AppRoute.Profile) },
                onNavigateToEnroll = {
                    scope.launch {
                        runCatching {
                            if (!stepUpUseCase.isDeviceRegistered()) {
                                stepUpUseCase.ensureRegisteredDevice(deviceLabel = "Android Device")
                            }
                            stepUpUseCase.stepUp(reason = "Confirm with fingerprint")
                        }.onSuccess {
                            navigator.navigate(AppRoute.BiometricEnroll("1"))
                        }.onFailure { throwable ->
                            Toast.makeText(
                                context,
                                mapStepUpErrorMessage(throwable),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onNavigateToVerify = {
                    scope.launch {
                        runCatching {
                            if (!stepUpUseCase.isDeviceRegistered()) {
                                stepUpUseCase.ensureRegisteredDevice(deviceLabel = "Android Device")
                            }
                            stepUpUseCase.stepUp(reason = "Confirm with fingerprint")
                        }.onSuccess {
                            navigator.navigate(AppRoute.BiometricVerify("1"))
                        }.onFailure { throwable ->
                            Toast.makeText(
                                context,
                                mapStepUpErrorMessage(throwable),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onNavigateToHistory = { navigator.navigate(AppRoute.ActivityHistory) },
                onNavigateBottom = { routeId ->
                    val route = when (routeId) {
                        ROUTE_OPERATOR_DASHBOARD -> AppRoute.Platform(ROUTE_OPERATOR_DASHBOARD)
                        AppRoute.ActivityHistory.id -> AppRoute.ActivityHistory
                        AppRoute.Profile.id -> AppRoute.Profile
                        else -> AppRoute.Platform(routeId)
                    }
                    navigator.navigate(route, replace = true)
                }
            )
        },

        // --- Shared screens ---
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
                },
                navItems = navItemsForRole
            )
        },
        AppRoute.Profile.id to { navigator, _ ->
            ProfileScreen(
                userName = "Test User",
                userEmail = "test@fivucsas.com",
                userRole = userRole,
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
                onOpenSettings = { navigator.navigate(AppRoute.Settings) },
                navItems = navItemsForRole
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
