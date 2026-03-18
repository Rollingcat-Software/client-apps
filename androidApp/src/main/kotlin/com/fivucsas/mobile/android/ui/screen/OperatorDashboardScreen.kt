package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel
import com.fivucsas.shared.ui.components.atoms.PrimaryButton
import com.fivucsas.shared.ui.components.atoms.SecondaryButton
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ActivityItem
import com.fivucsas.shared.ui.components.molecules.ActivityItemData
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.StatCard
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.theme.AppColors
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorDashboardScreen(
    currentRoute: String,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEnroll: () -> Unit,
    onNavigateToVerify: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateBottom: (String) -> Unit,
    viewModel: AdminViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    // Activity data will be loaded from API when endpoint is available
    val recentActivities = emptyList<ActivityItemData>()

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
                            text = "Operator Station",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
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
                items = BottomNavDestinations.operatorItems,
                currentRoute = currentRoute,
                onItemSelected = { onNavigateBottom(it.route) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(UIDimens.SpacingMedium),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            // [A] Error banner
            uiState.errorMessage?.let { error ->
                ErrorMessage(message = error)
            }

            // [B] Primary Actions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = UIDimens.ElevationLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UIDimens.SpacingMedium),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrimaryButton(
                        onClick = onNavigateToVerify,
                        text = "Verify Identity",
                        icon = Icons.Default.Security,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SecondaryButton(
                        onClick = onNavigateToEnroll,
                        text = "Enroll New User",
                        icon = Icons.Default.CameraAlt,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // [C] Today's Statistics - 3-column row
            SectionHeader(title = "Today's Statistics")

            val stats = uiState.statistics
            val successful = stats.verificationsToday - stats.failedAttempts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
            ) {
                StatCard(
                    value = stats.verificationsToday.toString(),
                    label = "Total",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Security,
                    iconTint = AppColors.Primary
                )
                StatCard(
                    value = successful.toString(),
                    label = "Successful",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    iconTint = AppColors.Success
                )
                StatCard(
                    value = stats.failedAttempts.toString(),
                    label = "Failed",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Error,
                    iconTint = AppColors.Error
                )
            }

            // [D] Success Rate card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = UIDimens.ElevationLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(UIDimens.SpacingMedium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Success Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    val rate = (uiState.statistics.successRate * 100).toInt()
                    Text(
                        text = "$rate%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (rate >= 80) AppColors.Success else AppColors.Warning
                    )
                }
            }

            // [E] Recent Activity + "View All" link
            SectionHeader(
                title = "Recent Activity",
                actionContent = {
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelLarge,
                        color = AppColors.Primary,
                        modifier = Modifier.clickable { onNavigateToHistory() }
                    )
                }
            )
            if (recentActivities.isEmpty()) {
                Text(
                    text = "No recent activity yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.OnSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)) {
                    recentActivities.forEach { activity ->
                        ActivityItem(data = activity)
                    }
                }
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))
        }
    }
}
