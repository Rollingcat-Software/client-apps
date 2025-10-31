package com.fivucsas.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fivucsas.desktop.ui.admin.AdminDashboard
import com.fivucsas.desktop.ui.kiosk.KioskMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FIVUCSAS Desktop Application
 *
 * Main entry point for the desktop application.
 * Provides two modes:
 * 1. Kiosk Mode - Self-service enrollment and verification
 * 2. Admin Mode - Management dashboard
 *
 * Based on Kotlin Multiplatform + Compose Multiplatform
 * Shares 90-95% code with mobile apps
 *
 * ARCHITECTURE:
 * - Follows MVVM pattern
 * - Uses StateFlow for reactive state management
 * - Implements Single Responsibility Principle
 */

// Constants for configuration
private object AppConfig {
    const val WINDOW_TITLE = "FIVUCSAS - Face and Identity Verification"
    const val WINDOW_WIDTH_DP = 1280
    const val WINDOW_HEIGHT_DP = 720
    const val APP_NAME = "FIVUCSAS"
    const val APP_DESCRIPTION = "Face and Identity Verification System"
    const val COPYRIGHT = "Marmara University | Engineering Project 2025"
}

// Dimensions
private object Dimens {
    val IconSize = 120.dp
    val IconSizeMedium = 64.dp
    val IconSizeSmall = 20.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingXLarge = 32.dp
    val SpacingXXLarge = 64.dp
    val CardWidth = 300.dp
    val CardHeight = 250.dp
    val ElevationMedium = 4.dp
}

/**
 * Application State Manager
 * Implements Single Responsibility Principle - only manages navigation state
 */
class AppStateManager {
    private val _currentMode = MutableStateFlow(AppMode.LAUNCHER)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    fun navigateTo(mode: AppMode) {
        _currentMode.value = mode
    }
}

/**
 * Main entry point with proper separation of concerns
 */
fun main() = application {
    // State management separated from UI
    val stateManager = remember { AppStateManager() }
    val currentMode by stateManager.currentMode.collectAsState()

    Window(
        onCloseRequest = ::exitApplication,
        title = AppConfig.WINDOW_TITLE,
        state = rememberWindowState(
            width = AppConfig.WINDOW_WIDTH_DP.dp,
            height = AppConfig.WINDOW_HEIGHT_DP.dp,
            placement = WindowPlacement.Maximized
        )
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme()
        ) {
            AppContent(
                currentMode = currentMode,
                onNavigate = stateManager::navigateTo
            )
        }
    }

    // System tray - disabled for now (requires icon resource)
    // TODO: Add icon.png and enable system tray
    // AppSystemTray(
    //     onNavigate = stateManager::navigateTo,
    //     onExit = ::exitApplication
    // )
}

/**
 * Main application content - follows Open/Closed Principle
 * Easy to add new modes without modifying existing code
 */
@Composable
private fun AppContent(
    currentMode: AppMode,
    onNavigate: (AppMode) -> Unit
) {
    when (currentMode) {
        AppMode.LAUNCHER -> LauncherScreen(
            onKioskSelected = { onNavigate(AppMode.KIOSK) },
            onAdminSelected = { onNavigate(AppMode.ADMIN) }
        )

        AppMode.KIOSK -> KioskMode(
            onBack = { onNavigate(AppMode.LAUNCHER) }
        )

        AppMode.ADMIN -> AdminDashboard(
            onBack = { onNavigate(AppMode.LAUNCHER) }
        )
    }
}

/**
 * System tray component - Disabled until icon resource is added
 * TODO: Create icon.png and enable
 */
/*
@Composable
private fun ApplicationScope.AppSystemTray(
    onNavigate: (AppMode) -> Unit,
    onExit: () -> Unit
) {
    Tray(
        icon = painterResource("icon.png"),
        tooltip = AppConfig.APP_NAME,
        menu = {
            Item("Open", onClick = { /* Restore window */ })
            Separator()
            Item("Kiosk Mode", onClick = { onNavigate(AppMode.KIOSK) })
            Item("Admin Dashboard", onClick = { onNavigate(AppMode.ADMIN) })
            Separator()
            Item("Exit", onClick = onExit)
        }
    )
}
*/

/**
 * Launcher Screen - Pure presentation component
 * Follows Dependency Inversion Principle (depends on callbacks, not concrete implementations)
 */
@Composable
fun LauncherScreen(
    onKioskSelected: () -> Unit,
    onAdminSelected: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo section
            AppLogo()

            Spacer(modifier = Modifier.height(Dimens.SpacingXXLarge))

            // Mode selection cards
            ModeSelectionCards(
                onKioskSelected = onKioskSelected,
                onAdminSelected = onAdminSelected
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingXXLarge))

            // Footer
            AppFooter()
        }
    }
}

/**
 * App Logo Component - Reusable, follows Single Responsibility
 */
@Composable
private fun AppLogo() {
    Icon(
        imageVector = Icons.Default.Fingerprint,
        contentDescription = "FIVUCSAS Logo",
        modifier = Modifier.size(Dimens.IconSize),
        tint = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(Dimens.SpacingXLarge))

    Text(
        text = AppConfig.APP_NAME,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.primary
    )

    Text(
        text = AppConfig.APP_DESCRIPTION,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Mode Selection Cards - Extracted for reusability
 */
@Composable
private fun ModeSelectionCards(
    onKioskSelected: () -> Unit,
    onAdminSelected: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingXLarge)
    ) {
        ModeCard(
            title = "Kiosk Mode",
            description = "Self-service enrollment and verification",
            icon = Icons.Default.TouchApp,
            onClick = onKioskSelected
        )

        ModeCard(
            title = "Admin Dashboard",
            description = "User management and system configuration",
            icon = Icons.Default.AdminPanelSettings,
            onClick = onAdminSelected
        )
    }
}

/**
 * App Footer Component - Reusable
 */
@Composable
private fun AppFooter() {
    Text(
        text = AppConfig.COPYRIGHT,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Mode Card Component - Follows Interface Segregation Principle
 * Only exposes necessary parameters
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(Dimens.CardWidth)
            .height(Dimens.CardHeight),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.ElevationMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(Dimens.IconSizeMedium),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Application Mode Enum
 * Can be moved to separate file for better organization
 */
enum class AppMode {
    LAUNCHER,
    KIOSK,
    ADMIN
}
