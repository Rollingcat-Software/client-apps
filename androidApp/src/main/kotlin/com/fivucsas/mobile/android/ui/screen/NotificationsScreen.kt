package com.fivucsas.mobile.android.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.molecules.NotificationItem
import com.fivucsas.shared.ui.components.molecules.NotificationItemData
import com.fivucsas.shared.ui.components.atoms.StatusBadgeType
import com.fivucsas.shared.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit
) {
    val notifications = listOf(
        NotificationItemData(
            title = "Security Alert",
            message = "Failed verification attempt",
            time = "2 min ago",
            isUnread = true,
            status = StatusBadgeType.Failure,
            icon = Icons.Default.Error,
            iconTint = AppColors.Error
        ),
        NotificationItemData(
            title = "Warning",
            message = "Multiple failed verifications",
            time = "15 min ago",
            isUnread = false,
            status = StatusBadgeType.Warning,
            icon = Icons.Default.Warning,
            iconTint = AppColors.Warning
        ),
        NotificationItemData(
            title = "Info",
            message = "New device login detected",
            time = "1 hour ago",
            isUnread = false,
            status = StatusBadgeType.Info,
            icon = Icons.Default.Info,
            iconTint = AppColors.Info
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                    titleContentColor = AppColors.OnSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(UIDimens.SpacingMedium),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(UIDimens.SpacingSmall)
        ) {
            items(notifications) { item ->
                NotificationItem(data = item)
            }
        }
    }
}
