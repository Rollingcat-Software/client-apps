package com.fivucsas.shared.ui.components.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes
import com.fivucsas.shared.ui.theme.AppTypography

/**
 * Primary Button Component
 *
 * A filled button with primary color, used for main actions.
 *
 * @param onClick Callback when button is clicked
 * @param text Button label text
 * @param modifier Optional modifier
 * @param enabled Whether button is enabled
 * @param icon Optional leading icon
 * @param shape Button shape (defaults to AppShapes.Button)
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = AppShapes.Button
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(UIDimens.ButtonHeight),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.OnPrimary,
            disabledContainerColor = AppColors.Gray400,
            disabledContentColor = AppColors.Gray600
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = UIDimens.CardElevation,
            pressedElevation = UIDimens.CardElevationPressed
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconSmall)
            )
            AppSpacer(width = UIDimens.SpacingSmall)
        }
        Text(
            text = text,
            style = AppTypography.LabelLarge
        )
    }
}

/**
 * Secondary Button Component
 *
 * An outlined button with primary color, used for secondary actions.
 *
 * @param onClick Callback when button is clicked
 * @param text Button label text
 * @param modifier Optional modifier
 * @param enabled Whether button is enabled
 * @param icon Optional leading icon
 * @param shape Button shape (defaults to AppShapes.Button)
 */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = AppShapes.Button
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(UIDimens.ButtonHeight),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.Primary,
            disabledContentColor = AppColors.Gray600
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconSmall)
            )
            AppSpacer(width = UIDimens.SpacingSmall)
        }
        Text(
            text = text,
            style = AppTypography.LabelLarge
        )
    }
}

/**
 * Text Button Component
 *
 * A text-only button, used for tertiary/low-emphasis actions.
 *
 * @param onClick Callback when button is clicked
 * @param text Button label text
 * @param modifier Optional modifier
 * @param enabled Whether button is enabled
 * @param icon Optional leading icon
 */
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppColors.Primary,
            disabledContentColor = AppColors.Gray600
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconSmall)
            )
            AppSpacer(width = UIDimens.SpacingSmall)
        }
        Text(
            text = text,
            style = AppTypography.LabelLarge
        )
    }
}

/**
 * Icon Button Component
 *
 * A button that displays only an icon, used for actions in tight spaces.
 *
 * @param onClick Callback when button is clicked
 * @param icon Icon to display
 * @param contentDescription Accessibility description
 * @param modifier Optional modifier
 * @param enabled Whether button is enabled
 * @param tint Icon tint color
 */
@Composable
fun AppIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: androidx.compose.ui.graphics.Color = AppColors.OnSurface
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else AppColors.Gray600,
            modifier = Modifier.size(UIDimens.IconSmall)
        )
    }
}

/**
 * Large Kiosk Button Component
 *
 * A large, prominent button designed for kiosk interfaces with touch screens.
 *
 * @param onClick Callback when button is clicked
 * @param text Button label text
 * @param modifier Optional modifier
 * @param enabled Whether button is enabled
 * @param icon Optional icon
 */
@Composable
fun KioskButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(UIDimens.ButtonWidthKiosk)
            .height(UIDimens.ButtonHeightKiosk),
        enabled = enabled,
        shape = AppShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = AppColors.OnPrimary,
            disabledContainerColor = AppColors.Gray400
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = UIDimens.CardElevation,
            pressedElevation = UIDimens.CardElevationPressed
        ),
        contentPadding = PaddingValues(UIDimens.SpacingMedium)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(UIDimens.IconMedium)
            )
            AppSpacer(width = UIDimens.SpacingMedium)
        }
        Text(
            text = text,
            style = AppTypography.KioskButton
        )
    }
}
