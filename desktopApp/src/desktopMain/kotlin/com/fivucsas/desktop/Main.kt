package com.fivucsas.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.fivucsas.desktop.ui.admin.AdminDashboard
import com.fivucsas.desktop.ui.kiosk.KioskMode
import com.fivucsas.shared.data.local.TokenManager
import com.fivucsas.shared.di.getAppModules
import org.koin.java.KoinJavaComponent.inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.context.startKoin

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
fun main() {
    // Initialize Koin dependency injection
    startKoin {
        modules(getAppModules())
    }

    application {
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
    val tokenManager: TokenManager by inject(TokenManager::class.java)
    val persistedRole = tokenManager.getRole()
    val adminAllowed = persistedRole == null ||
            persistedRole == "SUPERADMIN" ||
            persistedRole == "ORG_ADMIN"

    when (currentMode) {
        AppMode.LAUNCHER -> LauncherScreen(
            onKioskSelected = { onNavigate(AppMode.KIOSK) },
            onAdminSelected = {
                if (adminAllowed) onNavigate(AppMode.ADMIN)
            }
        )

        AppMode.KIOSK -> KioskMode(
            onBack = { onNavigate(AppMode.LAUNCHER) }
        )

        AppMode.ADMIN -> {
            if (adminAllowed) {
                AdminDashboard(
                    onBack = { onNavigate(AppMode.LAUNCHER) }
                )
            } else {
                onNavigate(AppMode.LAUNCHER)
            }
        }
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
 * Launcher Screen - Modern gradient design with elevated cards
 */
@Composable
fun LauncherScreen(
    onKioskSelected: () -> Unit,
    onAdminSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D47A1),
                        Color(0xFF1976D2),
                        Color(0xFF42A5F5)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Modern Logo
            AppLogo()

            Spacer(modifier = Modifier.height(Dimens.SpacingXXLarge))

            // Modern Mode Cards
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
 * App Logo Component - Modern circular design
 */
@Composable
private fun AppLogo() {
    // Elevated circular logo card
    Card(
        modifier = Modifier.size(140.dp),
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "FIVUCSAS Logo",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF1976D2)
            )
        }
    }

    Spacer(modifier = Modifier.height(Dimens.SpacingXLarge))

    Text(
        text = AppConfig.APP_NAME,
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            color = Color.White,
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.3f),
                offset = Offset(4f, 4f),
                blurRadius = 12f
            )
        )
    )

    Spacer(modifier = Modifier.height(Dimens.SpacingMedium))

    Text(
        text = AppConfig.APP_DESCRIPTION,
        style = MaterialTheme.typography.titleLarge.copy(
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
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
 * App Footer Component - Subtle white text
 */
@Composable
private fun AppFooter() {
    Text(
        text = AppConfig.COPYRIGHT,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Normal
        )
    )
}

/**
 * Mode Card Component - Modern gradient design with shadow
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
            .width(350.dp)
            .height(280.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.SpacingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Gradient icon background
            Card(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (title.contains("Kiosk")) {
                                    listOf(Color(0xFF1976D2), Color(0xFF1565C0))
                                } else {
                                    listOf(Color(0xFF00ACC1), Color(0xFF0097A7))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.SpacingLarge))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            )

            Spacer(modifier = Modifier.height(Dimens.SpacingSmall))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Normal
                ),
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
