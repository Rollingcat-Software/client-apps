package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fivucsas.mobile.android.ui.screen.BiometricEnrollScreen
import com.fivucsas.mobile.android.ui.screen.BiometricVerifyScreen
import com.fivucsas.mobile.android.ui.screen.HomeScreen
import com.fivucsas.mobile.android.ui.screen.LoginScreen
import com.fivucsas.mobile.android.ui.screen.RegisterScreen
import com.fivucsas.shared.presentation.viewmodel.auth.BiometricViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.LoginViewModel
import com.fivucsas.shared.presentation.viewmodel.auth.RegisterViewModel
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String) = "home/$userId/$userName"
    }

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

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val viewModel = koinInject<LoginViewModel>()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.createRoute("1", "Test User")) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            val viewModel = koinInject<RegisterViewModel>()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.createRoute("1", "Test User")) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            HomeScreen(
                userId = userId,
                userName = userName,
                onEnrollBiometric = {
                    navController.navigate(Screen.BiometricEnroll.createRoute(userId))
                },
                onVerifyBiometric = {
                    navController.navigate(Screen.BiometricVerify.createRoute(userId))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.BiometricEnroll.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<BiometricViewModel>()

            BiometricEnrollScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.BiometricVerify.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val viewModel = koinInject<BiometricViewModel>()

            BiometricVerifyScreen(
                userId = userId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
