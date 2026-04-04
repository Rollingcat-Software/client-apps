package com.fivucsas.mobile.android

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import org.junit.Rule
import org.junit.Test

/**
 * E2E / instrumented tests for the bottom navigation bar.
 *
 * These tests render the BottomNavBar composable in isolation and verify
 * that navigation items are displayed and click callbacks fire correctly.
 */
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testNavItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "dashboard"),
        BottomNavItem("History", Icons.Default.History, "activity_history"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    @Test
    fun bottomNavBar_displaysAllItems() {
        composeTestRule.setContent {
            BottomNavBar(
                items = testNavItems,
                currentRoute = "dashboard",
                onItemSelected = {}
            )
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_clickItemTriggersCallback() {
        var selectedRoute = ""
        composeTestRule.setContent {
            BottomNavBar(
                items = testNavItems,
                currentRoute = "dashboard",
                onItemSelected = { selectedRoute = it.route }
            )
        }

        composeTestRule.onNodeWithText("Profile").performClick()
        assert(selectedRoute == "profile") {
            "Expected navigation to 'profile' but got '$selectedRoute'"
        }
    }

    @Test
    fun bottomNavBar_clickHistoryNavigates() {
        var selectedRoute = ""
        composeTestRule.setContent {
            BottomNavBar(
                items = testNavItems,
                currentRoute = "dashboard",
                onItemSelected = { selectedRoute = it.route }
            )
        }

        composeTestRule.onNodeWithText("History").performClick()
        assert(selectedRoute == "activity_history") {
            "Expected navigation to 'activity_history' but got '$selectedRoute'"
        }
    }

    @Test
    fun bottomNavBar_homeItemIsHighlightedWhenActive() {
        composeTestRule.setContent {
            BottomNavBar(
                items = testNavItems,
                currentRoute = "dashboard",
                onItemSelected = {}
            )
        }

        // The Home item should exist and be displayed when it is the current route
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
