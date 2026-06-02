package com.fivucsas.mobile.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import com.fivucsas.shared.ui.navigation.RouteIds

// Labels are resolved via `get()` so the active i18n language (StringResources)
// is applied at access time rather than frozen at object-load. Only labels with
// an existing NAV_* StringKey are localized; "Home", "Invites", "QR", "History",
// "Console", "Tenants" and "Audit" have no dedicated key yet and stay hardcoded
// (adding new keys is intentionally out of scope for this change).
object BottomNavDestinations {
    /** USER: registered but not yet in a tenant */
    val userItems: List<BottomNavItem>
        get() = listOf(
            BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
            BottomNavItem("Invites", Icons.Default.Notifications, RouteIds.INVITE_ACCEPT),
            BottomNavItem(s(StringKey.NAV_PROFILE), Icons.Default.Person, RouteIds.PROFILE),
            BottomNavItem(s(StringKey.NAV_SETTINGS), Icons.Default.Settings, RouteIds.SETTINGS)
        )

    /** TENANT_MEMBER: enrolled user in a tenant */
    val memberItems: List<BottomNavItem>
        get() = listOf(
            BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
            BottomNavItem("QR", Icons.Default.QrCodeScanner, RouteIds.QR_LOGIN_SCAN),
            BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
            BottomNavItem(s(StringKey.NAV_PROFILE), Icons.Default.Person, RouteIds.PROFILE)
        )

    /** Fallback / GUEST */
    val items: List<BottomNavItem>
        get() = listOf(
            BottomNavItem("Home", Icons.Default.Home, RouteIds.DASHBOARD),
            BottomNavItem("History", Icons.Default.History, RouteIds.ACTIVITY_HISTORY),
            BottomNavItem(s(StringKey.NAV_PROFILE), Icons.Default.Person, RouteIds.PROFILE)
        )
}
