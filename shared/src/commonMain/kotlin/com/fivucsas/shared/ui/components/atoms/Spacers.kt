package com.fivucsas.shared.ui.components.atoms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.fivucsas.shared.config.UIDimens

/**
 * Generic Spacer Component
 *
 * Creates horizontal or vertical space between components.
 *
 * @param width Optional horizontal space
 * @param height Optional vertical space
 */
@Composable
fun AppSpacer(
    width: Dp? = null,
    height: Dp? = null
) {
    Spacer(
        modifier = Modifier
            .apply {
                width?.let { width(it) }
                height?.let { height(it) }
            }
    )
}

/**
 * Vertical Spacers (Predefined sizes)
 */
@Composable
fun VerticalSpacerXSmall() = AppSpacer(height = UIDimens.SpacingXSmall)

@Composable
fun VerticalSpacerSmall() = AppSpacer(height = UIDimens.SpacingSmall)

@Composable
fun VerticalSpacerMedium() = AppSpacer(height = UIDimens.SpacingMedium)

@Composable
fun VerticalSpacerLarge() = AppSpacer(height = UIDimens.SpacingLarge)

@Composable
fun VerticalSpacerXLarge() = AppSpacer(height = UIDimens.SpacingXLarge)

/**
 * Horizontal Spacers (Predefined sizes)
 */
@Composable
fun HorizontalSpacerXSmall() = AppSpacer(width = UIDimens.SpacingXSmall)

@Composable
fun HorizontalSpacerSmall() = AppSpacer(width = UIDimens.SpacingSmall)

@Composable
fun HorizontalSpacerMedium() = AppSpacer(width = UIDimens.SpacingMedium)

@Composable
fun HorizontalSpacerLarge() = AppSpacer(width = UIDimens.SpacingLarge)

@Composable
fun HorizontalSpacerXLarge() = AppSpacer(width = UIDimens.SpacingXLarge)
