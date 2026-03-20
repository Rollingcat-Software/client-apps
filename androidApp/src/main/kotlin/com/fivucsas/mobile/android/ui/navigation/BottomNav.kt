package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import com.fivucsas.shared.ui.navigation.RouteIds

object BottomNavDestinations {
    /** USER: registered but not yet in a tenant */
    val userItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
        BottomNavItem("Invites", Icons.Default.Notifications, RouteIds.INVITE_ACCEPT),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE),
        BottomNavItem("Settings", Icons.Default.Settings, RouteIds.SETTINGS)
    )

    /** TENANT_MEMBER: enrolled user in a tenant */
    val memberItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
        BottomNavItem("QR", Icons.Default.QrCodeScanner, RouteIds.QR_LOGIN_SCAN),
        BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )

    /** Fallback / GUEST */
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
        BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )

    /** TENANT_ADMIN */
    val adminItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, RouteIds.ADMIN_DASHBOARD),
        BottomNavItem("Users", Icons.Default.Person, RouteIds.USERS_MANAGEMENT),
        BottomNavItem("History", Icons.Default.History, RouteIds.TENANT_HISTORY),
        BottomNavItem("Settings", Icons.Default.Settings, RouteIds.TENANT_SETTINGS)
    )

    /** ROOT */
    val rootItems = listOf(
        BottomNavItem("Console", Icons.Default.Home, RouteIds.ROOT_CONSOLE),
        BottomNavItem("Tenants", Icons.Default.Home, RouteIds.ROOT_TENANT_MANAGEMENT),
        BottomNavItem("Audit", Icons.Default.History, RouteIds.ROOT_AUDIT_EXPLORER),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )

    val operatorItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, RouteIds.OPERATOR_DASHBOARD),
        BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
        BottomNavItem("Profile", Icons.Default.Person, RouteIds.PROFILE)
    )
}
