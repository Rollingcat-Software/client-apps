package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ActivityItem
import com.fivucsas.shared.ui.components.molecules.ActivityItemData
import com.fivucsas.shared.ui.components.molecules.FilterChipItem
import com.fivucsas.shared.ui.components.molecules.FilterChipRow
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    currentRoute: String,
    onNavigateBottom: (String) -> Unit,
    navItems: List<com.fivucsas.shared.ui.components.organisms.BottomNavItem> = com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations.items
) {
    data class HistoryEntry(
        val category: String,
        val item: ActivityItemData
    )

    val filters = listOf(
        FilterChipItem("All", "all"),
        FilterChipItem("Verifications", "verification"),
        FilterChipItem("Enrollments", "enrollment")
    )
    var selectedFilter by remember { mutableStateOf(filters.first().value) }

    val sections = listOf(
        "Today" to listOf(
            HistoryEntry(
                category = "verification",
                item = ActivityItemData(
                    title = "Verification Successful",
                    description = "Confidence: 94%",
                    timestamp = "10:30 AM",
                    score = "94%",
                    status = StatusBadgeType.Success,
                    icon = Icons.Default.Security,
                    iconTint = AppColors.Success
                )
            ),
            HistoryEntry(
                category = "verification",
                item = ActivityItemData(
                    title = "Verification Successful",
                    description = "Confidence: 91%",
                    timestamp = "09:15 AM",
                    score = "91%",
                    status = StatusBadgeType.Success,
                    icon = Icons.Default.Security,
                    iconTint = AppColors.Success
                )
            )
        ),
        "Yesterday" to listOf(
            HistoryEntry(
                category = "verification",
                item = ActivityItemData(
                    title = "Verification Failed",
                    description = "Low confidence score",
                    timestamp = "3:14 PM",
                    score = "62%",
                    status = StatusBadgeType.Failure,
                    icon = Icons.Default.Security,
                    iconTint = AppColors.Error
                )
            ),
            HistoryEntry(
                category = "verification",
                item = ActivityItemData(
                    title = "Verification Successful",
                    description = "Confidence: 91%",
                    timestamp = "3:15 PM",
                    score = "91%",
                    status = StatusBadgeType.Success,
                    icon = Icons.Default.Security,
                    iconTint = AppColors.Success
                )
            )
        ),
        "January 28, 2026" to listOf(
            HistoryEntry(
                category = "enrollment",
                item = ActivityItemData(
                    title = "Face Enrollment Completed",
                    description = "Quality score: 88%",
                    timestamp = "2:00 PM",
                    score = "88%",
                    status = StatusBadgeType.Info,
                    icon = Icons.Default.CameraAlt,
                    iconTint = AppColors.Primary
                )
            )
        )
    )

    val filteredSections = sections.mapNotNull { (title, entries) ->
        val filteredEntries = if (selectedFilter == "all") {
            entries
        } else {
            entries.filter { it.category == selectedFilter }
        }

        if (filteredEntries.isEmpty()) {
            null
        } else {
            title to filteredEntries
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity History") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
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
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium)
        ) {
            FilterChipRow(
                items = filters,
                selectedValue = selectedFilter,
                onSelected = { selectedFilter = it.value }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
            ) {
                filteredSections.forEach { (title, itemsList) ->
                    item {
                        SectionHeader(
                            title = title,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(itemsList) { entry ->
                        ActivityItem(data = entry.item)
                    }
                }
            }
        }
    }
}
