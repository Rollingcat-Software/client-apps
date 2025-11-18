package com.fivucsas.desktop.ui.kiosk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.fivucsas.desktop.ui.kiosk.screens.EnrollScreen
import com.fivucsas.desktop.ui.kiosk.screens.VerifyScreen
import com.fivucsas.desktop.ui.kiosk.screens.WelcomeScreen
import com.fivucsas.shared.presentation.state.KioskScreen
import com.fivucsas.shared.presentation.viewmodel.KioskViewModel
import org.koin.compose.koinInject

/**
 * Kiosk Mode - Refactored
 *
 * Clean, modular kiosk interface using extracted screen components.
 *
 * Benefits:
 * - Modular architecture with separated screens
 * - Each screen in its own file for maintainability
 * - Reusable components following Atomic Design
 * - Easy to test individual screens
 * - Follows Single Responsibility Principle
 *
 * @param onBack Callback when back button is clicked
 * @param viewModel Kiosk view model (injected via Koin)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KioskMode(
    onBack: () -> Unit,
    viewModel: KioskViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            // Only show app bar when not on welcome screen
            if (uiState.currentScreen != KioskScreen.WELCOME) {
                TopAppBar(
                    title = {
                        Text(
                            when (uiState.currentScreen) {
                                KioskScreen.ENROLL -> "New Enrollment"
                                KioskScreen.VERIFY -> "Identity Verification"
                                else -> "FIVUCSAS Kiosk"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            KioskContent(
                currentScreen = uiState.currentScreen,
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}

/**
 * Kiosk Content Router
 *
 * Routes to the appropriate screen based on current state.
 * Follows Open/Closed Principle - add new screens without modifying existing code.
 *
 * @param currentScreen Currently displayed screen
 * @param viewModel Kiosk view model
 * @param onBack Callback to go back
 */
@Composable
private fun KioskContent(
    currentScreen: KioskScreen,
    viewModel: KioskViewModel,
    onBack: () -> Unit
) {
    when (currentScreen) {
        KioskScreen.WELCOME -> WelcomeScreen(
            onEnroll = viewModel::navigateToEnroll,
            onVerify = viewModel::navigateToVerify
        )

        KioskScreen.ENROLL -> EnrollScreen(
            viewModel = viewModel,
            onBack = viewModel::navigateToWelcome
        )

        KioskScreen.VERIFY -> VerifyScreen(
            viewModel = viewModel,
            onBack = viewModel::navigateToWelcome
        )
    }
}
