package com.fivucsas.desktop.ui.admin.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fivucsas.desktop.ui.admin.components.AdminConstants
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.Statistics
import com.fivucsas.shared.presentation.viewmodel.AdminViewModel

/**
 * Analytics Tab Component
 *
 * Displays system analytics, statistics, and charts.
 *
 * @param viewModel Admin view model
 */
@Composable
fun AnalyticsTab(
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val statistics = uiState.statistics

    Column(
        modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
    ) {
        // Header
        Text(
            AdminConstants.ANALYTICS_TITLE,
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            "Real-time system metrics and verification trends",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(UIDimens.SpacingXLarge))

        // Statistics Cards
        StatisticsCards(statistics = statistics)

        Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

        // Charts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
        ) {
            VerificationTrendsChart(modifier = Modifier.weight(1f))
            SuccessRateChart(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Statistics Cards Component
 */
@Composable
private fun StatisticsCards(
    statistics: Statistics
) {
    val statCards = remember(statistics) {
        listOf(
            StatCardData(
                title = "Total Users",
                value = statistics.totalUsers.toString(),
                icon = Icons.Default.People
            ),
            StatCardData(
                title = "Verifications Today",
                value = statistics.verificationsToday.toString(),
                icon = Icons.Default.VerifiedUser
            ),
            StatCardData(
                title = "Success Rate",
                value = String.format("%.1f%%", statistics.successRate),
                icon = Icons.Default.TrendingUp
            ),
            StatCardData(
                title = "Failed Attempts",
                value = statistics.failedAttempts.toString(),
                icon = Icons.Default.Warning
            )
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium)
    ) {
        statCards.forEach { data ->
            StatCard(
                data = data,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Stat Card Data Model
 */
data class StatCardData(
    val title: String,
    val value: String,
    val icon: ImageVector
)

/**
 * Stat Card Component
 */
@Composable
private fun StatCard(
    data: StatCardData,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(UIDimens.SpacingMedium),
            horizontalArrangement = Arrangement.spacedBy(UIDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = data.title,
                modifier = Modifier.size(UIDimens.IconLarge),
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = data.value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = data.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Verification Trends Chart
 */
@Composable
private fun VerificationTrendsChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(300.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Verification Trends",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.height(UIDimens.SpacingMedium))

            // Simple bar chart
            val dailyData = listOf(45, 68, 52, 78, 85, 72, 90)
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyData.forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            value.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height((value * 2).dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF1976D2)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            days[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Success Rate Chart
 */
@Composable
private fun SuccessRateChart(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(300.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(UIDimens.SpacingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Success Rate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(UIDimens.SpacingLarge))

            // Large percentage display
            Text(
                "85.4%",
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )

            Text(
                "Average success rate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
