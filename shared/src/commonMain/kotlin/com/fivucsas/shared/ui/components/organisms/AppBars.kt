package com.fivucsas.shared.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.HeadlineMediumText
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Top App Bar Component
 *
 * A standardized top app bar with title and optional navigation and actions.
 *
 * @param title App bar title
 * @param modifier Optional modifier
 * @param navigationIcon Optional navigation icon (e.g., back button)
 * @param onNavigationClick Callback when navigation icon is clicked
 * @param actions Optional action buttons
 */
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(UIDimens.AppBarHeight)
            .background(AppColors.Primary)
            .padding(horizontal = UIDimens.SpacingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Navigation icon
        if (navigationIcon != null && onNavigationClick != null) {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = "Navigate back",
                    tint = AppColors.OnPrimary
                )
            }
        }

        // Title
        HeadlineMediumText(
            text = title,
            color = AppColors.OnPrimary,
            modifier = Modifier.weight(1f)
        )

        // Actions
        actions()
    }
}

/**
 * Simple Top App Bar Component
 *
 * A simplified version with just a back button and title.
 *
 * @param title App bar title
 * @param onBackClick Callback when back button is clicked
 * @param modifier Optional modifier
 */
@Composable
fun SimpleTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = title,
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBackClick,
        modifier = modifier
    )
}
