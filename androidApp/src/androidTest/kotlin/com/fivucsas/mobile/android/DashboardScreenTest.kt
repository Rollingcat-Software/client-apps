package com.fivucsas.mobile.android

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fivucsas.mobile.android.ui.screen.DashboardScreen
import com.fivucsas.shared.domain.model.AuthSession
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.repository.DashboardRepository
import com.fivucsas.shared.domain.repository.SessionRepository
import com.fivucsas.shared.platform.IFileSaver
import com.fivucsas.shared.presentation.viewmodel.AnalyticsViewModel
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import org.junit.Rule
import org.junit.Test

/**
 * E2E / instrumented tests for the Dashboard screen.
 *
 * Provides stub dependencies (SessionRepository, AnalyticsViewModel) so
 * Koin is not required. Tests verify the top-bar greeting, quick actions,
 * and bottom navigation rendering.
 */
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ── Stubs ──────────────────────────────────────────────────────

    private val stubSessionRepository = object : SessionRepository {
        override suspend fun getSessions(): Result<List<AuthSession>> =
            Result.success(emptyList())
        override suspend fun revokeSession(sessionId: String): Result<Unit> =
            Result.success(Unit)
    }

    private val stubDashboardRepository = object : DashboardRepository {
        override suspend fun getStatistics(): Result<Statistics> =
            Result.success(Statistics())
    }

    private val stubFileSaver = object : IFileSaver {
        override suspend fun saveTextFile(content: String, suggestedFileName: String, mimeType: String): Result<String> =
            Result.success("/tmp/test.csv")
    }

    private fun analyticsViewModel() = AnalyticsViewModel(stubDashboardRepository, stubFileSaver)

    private val navItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "dashboard"),
        BottomNavItem("History", Icons.Default.History, "activity_history"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    private fun setDashboardScreen(userName: String = "Test User") {
        val vm = analyticsViewModel()
        composeTestRule.setContent {
            DashboardScreen(
                userName = userName,
                userRole = UserRole.USER,
                navItems = navItems,
                currentRoute = "dashboard",
                onNavigateToNotifications = {},
                onNavigateToProfile = {},
                onNavigateToEnroll = {},
                onNavigateToVerify = {},
                onNavigateToQrScan = {},
                onNavigateToHistory = {},
                onNavigateToInvitations = {},
                onNavigateToExamEntry = {},
                onNavigateToRequestMembership = {},
                onNavigateToCardScan = {},
                onNavigateToNfcRead = {},
                onNavigateBottom = {},
                sessionRepository = stubSessionRepository,
                analyticsViewModel = vm
            )
        }
    }

    // ── Tests ──────────────────────────────────────────────────────

    @Test
    fun dashboardScreen_displaysGreeting() {
        setDashboardScreen(userName = "Ahmet")

        composeTestRule.onNodeWithText("Good day, Ahmet").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_displaysBranding() {
        setDashboardScreen()

        composeTestRule.onNodeWithText("FIVUCSAS").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsNotificationIcon() {
        setDashboardScreen()

        composeTestRule.onNodeWithContentDescription("Notifications").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_showsProfileIcon() {
        setDashboardScreen()

        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
    }

    @Test
    fun dashboardScreen_notificationIconTriggersCallback() {
        var clicked = false
        val vm = analyticsViewModel()
        composeTestRule.setContent {
            DashboardScreen(
                userName = "Test",
                userRole = UserRole.USER,
                navItems = navItems,
                currentRoute = "dashboard",
                onNavigateToNotifications = { clicked = true },
                onNavigateToProfile = {},
                onNavigateToEnroll = {},
                onNavigateToVerify = {},
                onNavigateToQrScan = {},
                onNavigateToHistory = {},
                onNavigateToInvitations = {},
                onNavigateToExamEntry = {},
                onNavigateToRequestMembership = {},
                onNavigateToCardScan = {},
                onNavigateToNfcRead = {},
                onNavigateBottom = {},
                sessionRepository = stubSessionRepository,
                analyticsViewModel = vm
            )
        }

        composeTestRule.onNodeWithContentDescription("Notifications").performClick()
        assert(clicked) { "Notifications callback was not invoked" }
    }

    @Test
    fun dashboardScreen_bottomNavIsDisplayed() {
        setDashboardScreen()

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }
}
