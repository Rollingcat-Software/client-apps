package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import com.fivucsas.shared.ui.navigation.AppRoute

object BottomNavDestinations {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, AppRoute.Dashboard.id),
        BottomNavItem("History", Icons.Default.History, AppRoute.ActivityHistory.id),
        BottomNavItem("Profile", Icons.Default.Person, AppRoute.Profile.id)
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
