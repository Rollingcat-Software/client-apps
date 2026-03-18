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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    navItems: List<com.fivucsas.shared.ui.components.organisms.BottomNavItem> = com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations.items,
    showExportButton: Boolean = false,
    onExport: () -> Unit = {}
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

    // Activity history will be loaded from API when endpoint is available
    val sections = emptyList<Pair<String, List<HistoryEntry>>>()

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
                actions = {
                    if (showExportButton) {
                        IconButton(onClick = onExport) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Export History"
                            )
                        }
                    }
                },
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

            if (filteredSections.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No activity history yet",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = AppColors.OnSurfaceVariant
                    )
                }
            } else {
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
}
