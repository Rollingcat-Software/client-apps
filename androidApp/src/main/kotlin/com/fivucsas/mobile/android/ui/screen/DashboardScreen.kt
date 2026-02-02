package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ActivityItem
import com.fivucsas.shared.ui.components.molecules.ActivityItemData
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.components.organisms.QuickActionGrid
import com.fivucsas.shared.ui.components.organisms.QuickActionItem
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    currentRoute: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEnroll: () -> Unit,
    onNavigateToVerify: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateBottom: (String) -> Unit
) {
    val quickActions = listOf(
        QuickActionItem("Enroll Face", Icons.Default.CameraAlt, onNavigateToEnroll),
        QuickActionItem("Verify Identity", Icons.Default.Security, onNavigateToVerify),
        QuickActionItem("Activity History", Icons.Default.History, onNavigateToHistory),
        QuickActionItem("Profile & Settings", Icons.Default.Person, onNavigateToProfile)
    )

    val activityItems = listOf(
        ActivityItemData(
            title = "Verification Successful",
            description = "Confidence: 94%",
            timestamp = "Today, 10:30 AM",
            score = "94%",
            status = StatusBadgeType.Success,
            icon = Icons.Default.Security,
            iconTint = AppColors.Success
        ),
        ActivityItemData(
            title = "Verification Failed",
            description = "Low confidence score",
            timestamp = "Yesterday, 3:14 PM",
            score = "62%",
            status = StatusBadgeType.Failure,
            icon = Icons.Default.Security,
            iconTint = AppColors.Error
        ),
        ActivityItemData(
            title = "Face Enrollment Completed",
            description = "Quality score: 88%",
            timestamp = "Jan 28, 2026",
            score = "88%",
            status = StatusBadgeType.Info,
            icon = Icons.Default.CameraAlt,
            iconTint = AppColors.Primary
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FIVUCSAS",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Good day, $userName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications"
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
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
                items = com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations.items,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = UIDimens.ElevationLow)
            ) {
                Column(
                    modifier = Modifier.padding(UIDimens.SpacingMedium)
                ) {
                    Text(
                        text = "Enrollment Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enrolled",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.Success
                        )
                        Text(
                            text = "Last verified: 2 hours ago",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Confidence: 94%",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            }

            SectionHeader(title = "Quick Actions")
            QuickActionGrid(actions = quickActions)

            SectionHeader(
                title = "Recent Activity",
                actionContent = {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.Primary
                    )
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)) {
                activityItems.forEach { item ->
                    ActivityItem(data = item)
                }
            }
        }
    }
}
