package com.fivucsas.shared.ui.navigation

import androidx.compose.runtime.Composable
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.FingerprintViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.platform.isFingerprintFlowAvailable
import com.fivucsas.shared.ui.screen.FingerprintGateScreen
import com.fivucsas.shared.ui.screen.FingerprintFailureScreen
import com.fivucsas.shared.ui.screen.FingerprintSuccessScreen
import com.fivucsas.shared.ui.screen.ForgotPasswordScreen
import com.fivucsas.shared.ui.screen.LoginScreen
import com.fivucsas.shared.ui.screen.MissingRouteScreen
import com.fivucsas.shared.ui.screen.OnboardingScreen
import com.fivucsas.shared.ui.screen.RegisterScreen
import com.fivucsas.shared.ui.screen.SplashScreen
import com.fivucsas.shared.ui.screen.DeveloperPortalScreen
import com.fivucsas.shared.ui.screen.WidgetDemoScreen
import com.fivucsas.shared.presentation.viewmodel.DeveloperPortalViewModel
import org.koin.mp.KoinPlatform.getKoin

data class AppStartState(
    val isFirstLaunch: Boolean,
    val isAuthenticated: Boolean
)

typealias RouteContent = @Composable (AppNavigator, AppRoute) -> Unit

@Composable
fun SharedAppRoot(
    startState: AppStartState,
    onFirstLaunchComplete: () -> Unit,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    fingerprintViewModel: FingerprintViewModel? = null,
    onLoginSuccessRoute: AppRoute = AppRoute.Dashboard,
    platformRoutes: Map<String, RouteContent> = emptyMap(),
    navigator: AppNavigator = rememberAppNavigator(start = AppRoute.Splash)
) {
    AppNavigation(
        navigator = navigator,
        startState = startState,
        onFirstLaunchComplete = onFirstLaunchComplete,
        loginViewModel = loginViewModel,
        registerViewModel = registerViewModel,
        fingerprintViewModel = fingerprintViewModel,
        onLoginSuccessRoute = onLoginSuccessRoute,
        platformRoutes = platformRoutes
    )
}

@Composable
private fun AppNavigation(
    navigator: AppNavigator,
    startState: AppStartState,
    onFirstLaunchComplete: () -> Unit,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    fingerprintViewModel: FingerprintViewModel?,
    onLoginSuccessRoute: AppRoute,
    platformRoutes: Map<String, RouteContent>
) {
    when (val route = navigator.currentRoute) {
        AppRoute.Splash -> SplashScreen(
            isFirstLaunch = startState.isFirstLaunch,
            isAuthenticated = startState.isAuthenticated,
            userRole = null,
            onNavigateToOnboarding = { navigator.navigate(AppRoute.Onboarding, clearBackStack = true) },
            onNavigateToLogin = { navigator.navigate(AppRoute.Login, clearBackStack = true) },
            onNavigateToDashboard = { navigator.navigate(onLoginSuccessRoute, clearBackStack = true) },
            onNavigateToAdminDashboard = { navigator.navigate(onLoginSuccessRoute, clearBackStack = true) }
        )

        AppRoute.Onboarding -> OnboardingScreen(
            onComplete = {
                onFirstLaunchComplete()
                navigator.navigate(AppRoute.Login, clearBackStack = true)
            },
            onSkip = {
                onFirstLaunchComplete()
                navigator.navigate(AppRoute.Login, clearBackStack = true)
            }
        )

        AppRoute.Login -> LoginScreen(
            viewModel = loginViewModel,
            onNavigateToRegister = { navigator.navigate(AppRoute.Register) },
            onNavigateToForgotPassword = { navigator.navigate(AppRoute.ForgotPassword) },
            onNavigateToGuestFaceCheck = {
                navigator.navigate(AppRoute.GuestFaceCheckCapture)
            },
            onLoginSuccess = {
                if (isFingerprintFlowAvailable() && fingerprintViewModel != null) {
                    navigator.navigate(
                        AppRoute.FingerprintGate(targetRouteId = onLoginSuccessRoute.id),
                        clearBackStack = true
                    )
                } else {
                    navigator.navigate(onLoginSuccessRoute, clearBackStack = true)
                }
            }
        )

        AppRoute.Register -> RegisterScreen(
            viewModel = registerViewModel,
            onNavigateBack = { navigator.pop() },
            onRegisterSuccess = { navigator.navigate(AppRoute.Dashboard, clearBackStack = true) }
        )

        AppRoute.ForgotPassword -> ForgotPasswordScreen(
            onNavigateBack = { navigator.pop() },
            onNavigateToLogin = { navigator.navigate(AppRoute.Login, clearBackStack = true) }
        )

        is AppRoute.FingerprintGate -> {
            if (isFingerprintFlowAvailable() && fingerprintViewModel != null) {
                FingerprintGateScreen(
                    viewModel = fingerprintViewModel,
                    onStart = { fingerprintViewModel.startStepUp() },
                    onSkip = { navigator.navigate(onLoginSuccessRoute, clearBackStack = true) },
                    onBack = { navigator.pop() },
                    onSuccess = { navigator.navigate(AppRoute.FingerprintSuccess, replace = true) },
                    onFailure = { navigator.navigate(AppRoute.FingerprintFailure, replace = true) }
                )
            } else {
                MissingRouteScreen(
                    routeId = route.id,
                    onBack = { navigator.pop() }
                )
            }
        }

        AppRoute.FingerprintSuccess -> {
            val token = fingerprintViewModel?.state?.value
                ?.let { it as? com.fivucsas.shared.presentation.state.FingerprintUiState.Success }
                ?.stepUpToken
            FingerprintSuccessScreen(
                stepUpToken = token,
                onContinue = { navigator.navigate(onLoginSuccessRoute, clearBackStack = true) }
            )
        }

        AppRoute.FingerprintFailure -> {
            val failureState = fingerprintViewModel?.state?.value
                as? com.fivucsas.shared.presentation.state.FingerprintUiState.Error
            FingerprintFailureScreen(
                message = failureState?.message ?: "Fingerprint verification failed.",
                recoverable = failureState?.recoverable ?: true,
                onRetry = { navigator.pop() },
                onBack = { navigator.pop() }
            )
        }

        AppRoute.WidgetDemo -> WidgetDemoScreen(
            onBack = { navigator.pop() }
        )

        AppRoute.DeveloperPortal -> {
            val viewModel: DeveloperPortalViewModel = getKoin().get()
            DeveloperPortalScreen(
                viewModel = viewModel,
                onBack = { navigator.pop() }
            )
        }

        else -> {
            val content = platformRoutes[route.id]
            if (content != null) {
                content(navigator, route)
            } else {
                MissingRouteScreen(
                    routeId = route.id,
                    onBack = { navigator.pop() }
                )
            }
        }
    }
}
