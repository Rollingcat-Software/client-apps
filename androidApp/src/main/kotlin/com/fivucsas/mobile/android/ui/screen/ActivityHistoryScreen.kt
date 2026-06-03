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
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.domain.model.AuditLog
import com.fivucsas.shared.i18n.StringKey
import com.fivucsas.shared.i18n.s
import com.fivucsas.shared.ui.components.atoms.SectionHeader
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.components.molecules.ActivityItem
import com.fivucsas.shared.ui.components.molecules.ActivityItemData
import com.fivucsas.shared.ui.components.molecules.FilterChipItem
import com.fivucsas.shared.ui.components.molecules.FilterChipRow
import com.fivucsas.shared.ui.components.organisms.BottomNavBar
import com.fivucsas.shared.ui.theme.AppColors

private data class HistoryEntry(
    val category: String,
    val item: ActivityItemData
)

/** Chip category an audit action falls into. "all" entries only show under the All chip. */
private fun categoryFor(action: String): String {
    val upper = action.uppercase()
    return when {
        upper.contains("VERIF") -> "verification"
        upper.contains("ENROLL") -> "enrollment"
        else -> "all"
    }
}

/** Date portion (YYYY-MM-DD) of an ISO-8601 timestamp; used as the section header. */
private fun dateOf(timestamp: String): String =
    timestamp.substringBefore('T').ifBlank { timestamp }

private fun AuditLog.toEntry(): HistoryEntry {
    val category = categoryFor(action)
    val icon = when (category) {
        "verification" -> Icons.Default.CameraAlt
        "enrollment" -> Icons.Default.HowToReg
        else -> Icons.Default.Security
    }
    val badge = when (status.uppercase()) {
        "SUCCESS" -> StatusBadgeType.Success
        "FAILURE" -> StatusBadgeType.Failure
        else -> StatusBadgeType.Info
    }
    return HistoryEntry(
        category = category,
        item = ActivityItemData(
            title = action,
            description = details,
            timestamp = timestamp,
            status = badge,
            icon = icon
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityHistoryScreen(
    currentRoute: String,
    onNavigateBottom: (String) -> Unit,
    navItems: List<com.fivucsas.shared.ui.components.organisms.BottomNavItem> = com.fivucsas.mobile.android.ui.navigation.BottomNavDestinations.items,
    events: List<AuditLog> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    showExportButton: Boolean = false,
    onExport: () -> Unit = {}
) {
    val filters = listOf(
        FilterChipItem(s(StringKey.VERIFICATION_FILTER_ALL), "all"),
        FilterChipItem(s(StringKey.VERIFICATIONS), "verification"),
        FilterChipItem(s(StringKey.ENROLLMENTS), "enrollment")
    )
    var selectedFilter by remember { mutableStateOf(filters.first().value) }

    // Group the current user's activity events by date (events arrive newest-first
    // from GET /api/v1/my/activity, so the section order is preserved).
    val sections: List<Pair<String, List<HistoryEntry>>> = events
        .map { it.toEntry() }
        .groupBy { dateOf(it.item.timestamp) }
        .map { (date, entries) -> date to entries }

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
                title = { Text(s(StringKey.DASH_ACTIVITY_HISTORY)) },
                actions = {
                    if (showExportButton) {
                        IconButton(onClick = onExport) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = s(StringKey.ACTHIST_EXPORT_DESC)
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

            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s(StringKey.ACTHIST_LOAD_ERROR),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            color = AppColors.OnSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = errorMessage,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = AppColors.OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = UIDimens.SpacingMedium)
                        ) {
                            Text(s(StringKey.RETRY))
                        }
                    }
                }

                filteredSections.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = s(StringKey.ACTHIST_EMPTY),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            color = AppColors.OnSurfaceVariant
                        )
                    }
                }

                else -> {
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
}
