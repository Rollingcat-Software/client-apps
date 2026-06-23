package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contactless
// import androidx.compose.material.icons.filled.CreditCard // #8 fix: "Add card" QuickAction hidden (see below)
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.mobile.android.ui.model.QuickAction
import com.fivucsas.mobile.android.ui.navigation.Screen
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.domain.repository.SessionRepository
import com.fivucsas.shared.presentation.viewmodel.AnalyticsViewModel
import org.koin.compose.koinInject
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Permission
import com.fivucsas.shared.domain.model.UserRole
import com.fivucsas.shared.domain.model.hasPermission
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ActivityItem
import com.fivucsas.shared.ui.components.molecules.ActivityItemData
import com.fivucsas.shared.ui.components.organisms.BottomNavItem
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.components.organisms.QuickActionGrid
import com.fivucsas.shared.ui.components.organisms.QuickActionItem
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.util.disposeOnLeave

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    userRole: UserRole = UserRole.USER,
    navItems: List<BottomNavItem>,
    currentRoute: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToQrScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToInvitations: () -> Unit,
    onNavigateToExamEntry: () -> Unit,
    onNavigateToRequestMembership: () -> Unit,
    onNavigateToCardScan: () -> Unit,
    onNavigateToNfcRead: () -> Unit = {},
    onNavigateToApproveLogin: () -> Unit = {},
    onNavigateBottom: (String) -> Unit,
    sessionRepository: SessionRepository = koinInject(),
    analyticsViewModel: AnalyticsViewModel = koinInject<AnalyticsViewModel>().disposeOnLeave()
) {
    val analyticsState by analyticsViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { analyticsViewModel.loadStatistics() }
    val stats: Statistics? = analyticsState.statistics
    val canViewEnrollmentStatus = userRole.hasPermission(Permission.ENROLL_SELF_CREATE) ||
        userRole.hasPermission(Permission.VERIFY_SELF)
    val canViewRecentActivity = userRole.hasPermission(Permission.HISTORY_READ_SELF)

    val actions = listOf(
        QuickAction(
            id = "qr",
            title = s(StringKey.DASH_QR),
            icon = Icons.Default.CameraAlt,
            route = Screen.QrLoginScan.route,
            anyPermissions = setOf(Permission.QR_SCAN, Permission.QR_DISPLAY)
        ),
        QuickAction(
            id = "activity-history",
            title = s(StringKey.DASH_ACTIVITY_HISTORY),
            icon = Icons.Default.History,
            route = Screen.ActivityHistory.route,
            anyPermissions = setOf(Permission.HISTORY_READ_SELF)
        ),
        QuickAction(
            id = "invite-accept",
            title = s(StringKey.DASH_INVITATIONS),
            icon = Icons.Default.Notifications,
            route = Screen.InviteAccept.route,
            anyPermissions = setOf(Permission.TENANT_INVITE_ACCEPT)
        ),
        QuickAction(
            id = "profile",
            title = s(StringKey.NAV_PROFILE),
            icon = Icons.Default.Person,
            route = Screen.Profile.route,
            anyPermissions = setOf(Permission.PROFILE_READ_SELF)
        ),
        QuickAction(
            id = "login-requests",
            title = s(StringKey.DASH_LOGIN_REQUESTS),
            icon = Icons.Default.CheckCircle,
            route = Screen.ApproveLogin.route,
            anyPermissions = setOf(Permission.PROFILE_READ_SELF)
        ),
        QuickAction(
            id = "request-membership",
            title = s(StringKey.DASH_JOIN_TENANT),
            icon = Icons.Default.PersonAdd,
            route = Screen.RequestMembership.route,
            anyPermissions = setOf(Permission.TENANT_MEMBERSHIP_REQUEST)
        ),
        // "Add card" (#8 fix, pre-demo 2026-06-03): HIDDEN. The CardScan flow is a
        // camera photo wizard that captures front/back images but never uploads or
        // persists them (CardScanScreen has no API/repository call) — a misleading
        // dead-end for a demo. The screen + nav route are left intact (CardScanScreen,
        // Screen.CardScan, onNavigateToCardScan) so this is fully reversible: restore
        // the QuickAction below once the OCR/upload backend is wired.
        //
        // QuickAction(
        //     id = "card-scan",
        //     title = s(StringKey.DASH_ADD_CARD),
        //     icon = Icons.Default.CreditCard,
        //     route = Screen.CardScan.route,
        //     anyPermissions = setOf(Permission.CARD_ADD_SELF)
        // ),
        QuickAction(
            id = "nfc-read",
            title = s(StringKey.DASH_NFC_READER),
            icon = Icons.Default.Contactless,
            route = Screen.NfcRead.route
        )
    )

    val visibleActions = actions.filter { it.isAllowed(userRole) }
    val quickActions = visibleActions.map { action ->
        QuickActionItem(
            title = action.title,
            icon = action.icon,
            onClick = {
                when (action.route) {
                    Screen.QrLoginScan.route -> onNavigateToQrScan()
                    Screen.ActivityHistory.route -> onNavigateToHistory()
                    Screen.InviteAccept.route -> onNavigateToInvitations()
                    Screen.Profile.route -> onNavigateToProfile()
                    Screen.RequestMembership.route -> onNavigateToRequestMembership()
                    Screen.CardScan.route -> onNavigateToCardScan()
                    Screen.NfcRead.route -> onNavigateToNfcRead()
                    Screen.ApproveLogin.route -> onNavigateToApproveLogin()
                }
            }
        )
    }

    var activityItems by remember { mutableStateOf(emptyList<ActivityItemData>()) }
    LaunchedEffect(Unit) {
        sessionRepository.getSessions().onSuccess { sessions ->
            activityItems = sessions.take(5).map { session ->
                ActivityItemData(
                    title = session.deviceInfo.ifBlank { s(StringKey.DASH_SESSION_FALLBACK) },
                    description = session.ipAddress.ifBlank { session.userAgent },
                    timestamp = session.lastActiveAt.ifBlank { session.createdAt },
                    status = when (session.status) {
                        "ACTIVE" -> StatusBadgeType.Success
                        "EXPIRED" -> StatusBadgeType.Warning
                        "REVOKED" -> StatusBadgeType.Failure
                        else -> StatusBadgeType.Info
                    }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = s(StringKey.APP_NAME),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = s(StringKey.DASH_GREETING, userName),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Notifications bell hidden: there is no backend notifications
                    // feed yet, so the screen was permanently "No notifications yet".
                    // onNavigateToNotifications stays wired — restore the bell once a
                    // feed endpoint exists (see NotificationsScreen TODO(#103)).
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = s(StringKey.NAV_PROFILE)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            // Greeting banner — gradient accent header mirroring the web dashboard hero.
            DashboardGreetingBanner(userName = userName)

            if (canViewEnrollmentStatus) {
                if (stats != null) {
                    DashboardSectionLabel(text = s(StringKey.DASH_ENROLLMENT_STATUS))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
                    ) {
                        DashboardStatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Groups,
                            value = "${stats.totalUsers}",
                            caption = s(StringKey.TOTAL_USERS)
                        )
                        DashboardStatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.CheckCircle,
                            value = "${stats.verificationsToday}",
                            caption = s(StringKey.ANALYTICS_VERIFICATIONS_TODAY)
                        )
                        DashboardStatTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            value = "${((stats.successRate) * 100).toInt()}%",
                            caption = s(StringKey.ANALYTICS_SUCCESS_RATE)
                        )
                    }
                } else {
                    DashboardSurfaceCard {
                        Text(
                            text = s(StringKey.DASH_ENROLLMENT_STATUS),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.OnSurface
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = s(StringKey.DASH_ENROLL_PROMPT),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                }
            }

            if (userRole == UserRole.USER) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppColors.Warning.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            AppColors.Warning.copy(alpha = 0.30f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(UIDimens.SpacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = AppColors.WarningDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = s(StringKey.DASH_NOT_TENANT_MEMBER),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurface
                    )
                }
            }

            DashboardSectionLabel(text = s(StringKey.DASH_QUICK_ACTIONS))
            QuickActionGrid(actions = quickActions)

            if (canViewRecentActivity) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DashboardSectionLabel(
                        text = s(StringKey.ANALYTICS_RECENT_ACTIVITY),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = s(StringKey.DASH_VIEW_ALL),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Primary
                    )
                }
                if (activityItems.isEmpty()) {
                    Text(
                        text = s(StringKey.DASH_NO_RECENT_ACTIVITY),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.OnSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)) {
                        activityItems.forEach { item ->
                            ActivityItem(data = item)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(UIDimens.SpacingSmall))
        }
    }
}

/**
 * Small bold uppercase-ish section label in the muted variant colour — the web
 * dashboard's group headers.
 */
@Composable
private fun DashboardSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp,
        color = AppColors.OnSurfaceVariant,
        modifier = modifier
    )
}

/** Bordered Surface card matching the hosted-login card language. */
@Composable
private fun DashboardSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.OnSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        content = content
    )
}

/**
 * Gradient-accented greeting hero — indigo→purple band with a waving-hand mark,
 * mirroring the app.fivucsas dashboard header.
 */
@Composable
private fun DashboardGreetingBanner(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.PrimaryGradient)
            .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = s(StringKey.DASH_GREETING, userName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = s(StringKey.APP_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

/**
 * Gradient stat tile — colored icon chip, big bold value, caption. Matches the
 * web's indigo→purple stat cards.
 */
@Composable
private fun DashboardStatTile(
    icon: ImageVector,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.PrimaryGradient)
            .padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
