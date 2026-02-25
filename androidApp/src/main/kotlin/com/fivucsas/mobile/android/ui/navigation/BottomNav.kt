package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import com.fivucsas.shared.ui.navigation.RouteIds

object BottomNavDestinations {
    val userItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
        BottomNavItem("Invites", Icons.Default.Notifications, RouteIds.INVITE_ACCEPT),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )

    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
        BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )

    val adminItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, RouteIds.ADMIN_DASHBOARD),
        BottomNavItem("History", Icons.Default.History, RouteIds.TENANT_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )

    val operatorItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, RouteIds.OPERATOR_DASHBOARD),
        BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )
}
