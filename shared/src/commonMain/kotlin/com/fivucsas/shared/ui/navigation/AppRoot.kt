package com.fivucsas.shared.ui.navigation

import androidx.compose.runtime.Composable
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import com.fivucsas.shared.ui.screen.LoginScreen
import com.fivucsas.shared.ui.screen.MissingRouteScreen
import com.fivucsas.shared.ui.screen.OnboardingScreen
import com.fivucsas.shared.ui.screen.RegisterScreen
import com.fivucsas.shared.ui.screen.SplashScreen

data class AppStartState(
    val isFirstLaunch: Boolean,
    val isAuthenticated: Boolean,
    val userRole: String? = null
)

typealias RouteContent = @Composable (AppNavigator, AppRoute) -> Unit

@Composable
fun SharedAppRoot(
    startState: AppStartState,
    onFirstLaunchComplete: () -> Unit,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
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
    onLoginSuccessRoute: AppRoute,
    platformRoutes: Map<String, RouteContent>
) {
    when (val route = navigator.currentRoute) {
        AppRoute.Splash -> SplashScreen(
            isFirstLaunch = startState.isFirstLaunch,
            isAuthenticated = startState.isAuthenticated,
            userRole = startState.userRole,
            onNavigateToOnboarding = { navigator.navigate(AppRoute.Onboarding, clearBackStack = true) },
            onNavigateToLogin = { navigator.navigate(AppRoute.Login, clearBackStack = true) },
            onNavigateToDashboard = { navigator.navigate(AppRoute.Dashboard, clearBackStack = true) },
            onNavigateToAdminDashboard = {
                navigator.navigate(AppRoute.Platform("admin-dashboard"), clearBackStack = true)
            },
            onNavigateToOperatorDashboard = {
                navigator.navigate(AppRoute.Platform("operator-dashboard"), clearBackStack = true)
            }
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
            onLoginSuccess = { navigator.navigate(onLoginSuccessRoute, clearBackStack = true) }
        )

        AppRoute.Register -> RegisterScreen(
            viewModel = registerViewModel,
            onNavigateBack = { navigator.pop() },
            onRegisterSuccess = { navigator.navigate(AppRoute.Dashboard, clearBackStack = true) }
        )

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
