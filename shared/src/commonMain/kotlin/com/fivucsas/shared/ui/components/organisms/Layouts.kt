package com.fivucsas.shared.ui.components.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.LoadingBox
import com.fivucsas.shared.ui.components.atoms.VerticalSpacerMedium
import com.fivucsas.shared.ui.components.molecules.ErrorMessage
import com.fivucsas.shared.ui.components.molecules.SuccessMessage
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Screen Layout Component
 *
 * A standard screen layout with optional loading, error, and success states.
 *
 * @param modifier Optional modifier
 * @param isLoading Whether to show loading state
 * @param errorMessage Optional error message to display
 * @param successMessage Optional success message to display
 * @param topBar Optional top app bar
 * @param content Screen content
 */
@Composable
fun ScreenLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // Top bar
        topBar?.invoke()

        // Messages
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium)
        ) {
            if (errorMessage != null) {
                ErrorMessage(message = errorMessage)
                VerticalSpacerMedium()
            }

            if (successMessage != null) {
                SuccessMessage(message = successMessage)
                VerticalSpacerMedium()
            }
        }

        // Content or Loading
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                LoadingBox(
                    modifier = Modifier.fillMaxSize(),
                    message = "Loading..."
                )
            } else {
                content()
            }
        }
    }
}

/**
 * Card Container Layout
 *
 * A layout that wraps content in a centered card with padding.
 *
 * @param modifier Optional modifier
 * @param content Card content
 */
@Composable
fun CardContainerLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(UIDimens.SpacingLarge)
    ) {
        com.fivucsas.shared.ui.components.molecules.AppCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(UIDimens.SpacingLarge)) {
                content()
            }
        }
    }
}
