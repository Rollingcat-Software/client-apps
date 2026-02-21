package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.fivucsas.shared.ui.components.organisms.BottomNavItem

object BottomNavDestinations {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, Screen.Dashboard.route),
        BottomNavItem("History", Icons.Default.History, Screen.ActivityHistory.route),
        BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
    )

    val adminItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, Screen.AdminDashboard.route),
        BottomNavItem("History", Icons.Default.History, Screen.ActivityHistory.route),
        BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
    )

    val operatorItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, Screen.OperatorDashboard.route),
        BottomNavItem("History", Icons.Default.History, Screen.ActivityHistory.route),
        BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
    )
}
