package com.fivucsas.mobile.android.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.fivucsas.mobile.android.ui.screen.ApproveLoginScreen
import com.fivucsas.mobile.android.ui.screen.CardScanScreen
import com.fivucsas.mobile.android.ui.screen.NfcReadScreen
import com.fivucsas.mobile.android.ui.screen.ChangePasswordScreen
import com.fivucsas.mobile.android.ui.screen.DashboardScreen
import com.fivucsas.mobile.android.ui.screen.EditProfileScreen
import com.fivucsas.mobile.android.ui.screen.HostedLoginScreen
import com.fivucsas.mobile.android.ui.screen.InviteAcceptScreen
import com.fivucsas.mobile.android.ui.screen.MyInvitationsScreen
import com.fivucsas.mobile.android.ui.screen.HelpScreen
import com.fivucsas.mobile.android.ui.screen.InviteManagementScreen
import com.fivucsas.mobile.android.ui.screen.NotificationsScreen
import com.fivucsas.mobile.android.ui.screen.ProfileScreen
import com.fivucsas.mobile.android.ui.screen.QRLoginScanScreen
import com.fivucsas.mobile.android.ui.screen.RequestMembershipScreen
import com.fivucsas.mobile.android.ui.screen.SettingsScreen
import com.fivucsas.mobile.android.ui.screen.UnauthorizedScreen
import com.fivucsas.mobile.android.ui.viewmodel.DataExportViewModel as AndroidDataExportViewModel
import com.fivucsas.authenticator.ui.AuthenticatorScreen
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.domain.repository.BiometricRepository
import com.fivucsas.shared.domain.repository.DataExportRepository
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.presentation.viewmodel.auth.ChangePasswordViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import com.fivucsas.shared.presentation.state.FingerprintUiState
import com.fivucsas.shared.presentation.viewmodel.UserProfileViewModel
import androidx.compose.runtime.collectAsState
import com.fivucsas.shared.ui.screen.FingerprintFailureScreen
import com.fivucsas.shared.ui.screen.FingerprintGateScreen
import com.fivucsas.shared.ui.screen.FingerprintSuccessScreen
import com.fivucsas.shared.ui.screen.OnboardingScreen
import com.fivucsas.shared.ui.screen.SplashScreen
import com.fivucsas.shared.ui.navigation.NavigationPolicy
import com.fivucsas.shared.ui.navigation.RouteIds
import com.fivucsas.shared.ui.util.disposeOnLeave
import org.koin.compose.koinInject

private const val PREFS_NAME = "fivucsas_prefs"
private const val KEY_FIRST_LAUNCH = "first_launch"

sealed class Screen(val route: String) {
    object Splash : Screen(RouteIds.SPLASH)
    object Onboarding : Screen(RouteIds.ONBOARDING)
    object Login : Screen(RouteIds.LOGIN)
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
    object ApproveLogin : Screen(RouteIds.APPROVE_LOGIN)
    object Unauthorized : Screen("${RouteIds.UNAUTHORIZED}/{message}") {
        fun createRoute(message: String): String = "${RouteIds.UNAUTHORIZED}/${Uri.encode(message)}"
    }

    object InviteAccept : Screen(RouteIds.INVITE_ACCEPT)
    object InviteManagement : Screen(RouteIds.INVITE_MANAGEMENT)
    object MyInvitations : Screen(RouteIds.MY_INVITATIONS)
    object RequestMembership : Screen(RouteIds.REQUEST_MEMBERSHIP)
    object CardScan : Screen(RouteIds.CARD_SCAN)
    object NfcRead : Screen(RouteIds.NFC_READ)

    object Authenticator : Screen(RouteIds.AUTHENTICATOR)

    object AuthFlows : Screen("${RouteIds.AUTH_FLOWS}/{tenantId}") {
        fun createRoute(tenantId: String) = "${RouteIds.AUTH_FLOWS}/$tenantId"
    }
    object Sessions : Screen(RouteIds.SESSIONS)
    object LinkedAccounts : Screen(RouteIds.LINKED_ACCOUNTS)
    object Devices : Screen("${RouteIds.DEVICES}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.DEVICES}/$userId"
    }
    object EnrollmentsList : Screen("${RouteIds.ENROLLMENTS_LIST}/{userId}") {
        fun createRoute(userId: String) = "${RouteIds.ENROLLMENTS_LIST}/$userId"
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
    // Thin-companion nav (2026-06-02 lock): no admin/root/operator bottom nav on
    // mobile — those management surfaces live only on the web dashboard. ROOT /
    // TENANT_ADMIN fall back to the member nav (personal tools + QR + history).
    val navItemsForRole = when (currentUserRole()) {
        UserRole.ROOT, UserRole.TENANT_ADMIN, UserRole.TENANT_MEMBER ->
            BottomNavDestinations.memberItems
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
            // Hosted-first login (2026-06-02 architecture lock): the whole
            // credential + MFA ceremony runs on verify.fivucsas.com in a Custom
            // Tab; this app is a thin OAuth client. On success we route by the
            // role from /auth/me. Native password/MFA/register/forgot screens are
            // retired in favour of the hosted page.
            HostedLoginScreen(
                onLoginSuccess = { role ->
                    val destination = NavigationPolicy.loginSuccessRoute(UserRole.fromString(role))
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
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
                onNavigateToQrScan = { navController.navigate(Screen.QrLoginScan.route) },
                onNavigateToHistory = { navController.navigate(Screen.ActivityHistory.route) },
                onNavigateToInvitations = { navController.navigate(Screen.InviteAccept.route) },
                // Exam-entry is an admin/operator surface — removed from the mobile
                // companion (lives on the web dashboard only). No-op on mobile.
                onNavigateToExamEntry = { },
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
            val profileVm = koinInject<UserProfileViewModel>().disposeOnLeave()
            val profileState by profileVm.state.collectAsState()
            LaunchedEffect(Unit) { profileVm.loadProfile() }
            val profileNavItems = when (userRole) {
                UserRole.ROOT, UserRole.TENANT_ADMIN, UserRole.TENANT_MEMBER ->
                    BottomNavDestinations.memberItems
                UserRole.USER -> BottomNavDestinations.userItems
                else -> BottomNavDestinations.items
            }
            val profileUserName = profileState.user?.name ?: tokenManager?.getUserName() ?: "User"
            val profileUserEmail = profileState.user?.email ?: tokenManager?.getUserEmail() ?: ""
            val profileBiometricRepository = koinInject<BiometricRepository>()
            val dataExportRepository = koinInject<DataExportRepository>()
            val profileContext = LocalContext.current.applicationContext
            val dataExportVm = remember(dataExportRepository) {
                AndroidDataExportViewModel(
                    repository = dataExportRepository,
                    appContext = profileContext,
                )
            }
            // AndroidDataExportViewModel owns a CoroutineScope but is not a BaseViewModel
            // (its dispatcher is injectable for tests); dispose it on leave directly.
            DisposableEffect(dataExportVm) {
                onDispose { dataExportVm.dispose() }
            }
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
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onOpenLinkedAccounts = { navController.navigate(Screen.LinkedAccounts.route) },
                onOpenLoginRequests = { navController.navigate(Screen.ApproveLogin.route) },
                onDeleteEnrollment = {
                    // Real delete: DELETE biometric/face/{userId} via BiometricRepository.
                    val deleteUserId = profileState.user?.id ?: tokenManager?.getUserId()
                    if (deleteUserId.isNullOrBlank()) {
                        Result.failure(IllegalStateException("No signed-in user to delete enrollment for."))
                    } else {
                        profileBiometricRepository.deleteBiometricData(deleteUserId)
                    }
                },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                navItems = profileNavItems,
                userId = profileState.user?.id ?: tokenManager?.getUserId() ?: "",
                dataExportViewModel = dataExportVm,
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
            val editProfileVm = koinInject<UserProfileViewModel>().disposeOnLeave()
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
                onSave = { firstName, lastName, phone ->
                    editProfileVm.updateProfile(firstName, lastName, phone)
                    navController.popBackStack()
                }
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
            val changePasswordVm = koinInject<ChangePasswordViewModel>().disposeOnLeave()
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
                // Server-pipeline biometric surfaces (Voice enroll/search, TOTP
                // enroll, Liveness, Card-Detection, Biometric-Backup) plus Email/SMS
                // OTP, Analytics, Hardware-token and System-settings are removed from
                // the mobile companion — those duplicate the hosted page / web
                // dashboard. The SettingsScreen callbacks default to no-ops, so they
                // are omitted here. Only the native TOTP Authenticator remains.
                onNavigateToAuthenticator = { navController.navigate(Screen.Authenticator.route) },
                onLogout = {
                    tokenManager?.clearTokens()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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

        composable(Screen.ApproveLogin.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            ApproveLoginScreen(onNavigateBack = { navController.popBackStack() })
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
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                return@composable
            }
            NfcReadScreen(
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
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.AuthFlowViewModel>().disposeOnLeave()
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
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.SessionViewModel>().disposeOnLeave()
            com.fivucsas.shared.ui.screen.SessionsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Linked accounts + workspace switcher
        composable(Screen.LinkedAccounts.route) {
            if (!isAuthenticated()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
                return@composable
            }
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.AccountLinkingViewModel>().disposeOnLeave()
            com.fivucsas.shared.ui.screen.LinkedAccountsScreen(
                viewModel = viewModel,
                onSwitched = {
                    // Membership switch persisted new (login-shaped) tokens — reset
                    // app context by routing to the post-login home for the new role.
                    val destination = NavigationPolicy.loginSuccessRoute(currentUserRole())
                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.DeviceViewModel>().disposeOnLeave()
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
            val viewModel = koinInject<com.fivucsas.shared.presentation.viewmodel.EnrollmentViewModel>().disposeOnLeave()
            com.fivucsas.shared.ui.screen.EnrollmentsScreen(
                viewModel = viewModel,
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        // Standalone TOTP Authenticator (Google/Microsoft Authenticator replacement)
        composable(Screen.Authenticator.route) {
            AuthenticatorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}
