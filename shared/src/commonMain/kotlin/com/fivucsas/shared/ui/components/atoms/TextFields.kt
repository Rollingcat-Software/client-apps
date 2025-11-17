package com.fivucsas.shared.ui.components.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.Icon
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes
import com.fivucsas.shared.ui.theme.AppTypography

/**
 * App Text Field Component
 *
 * A styled text input field with consistent theming.
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param label Field label
 * @param modifier Optional modifier
 * @param placeholder Optional placeholder text
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param isError Whether field has an error
 * @param errorMessage Optional error message to display
 * @param enabled Whether field is enabled
 * @param singleLine Whether to limit to single line
 * @param maxLines Maximum number of lines (if not singleLine)
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            },
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else maxLines,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.TextField,
            textStyle = AppTypography.BodyMedium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = AppColors.OnSurface,
                backgroundColor = AppColors.Surface,
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Gray300,
                errorBorderColor = AppColors.Error,
                cursorColor = AppColors.Primary,
                focusedLabelColor = AppColors.Primary,
                unfocusedLabelColor = AppColors.OnSurfaceVariant,
                errorLabelColor = AppColors.Error,
                placeholderColor = AppColors.OnSurfaceVariant,
                disabledTextColor = AppColors.OnSurfaceVariant,
                disabledBorderColor = AppColors.Gray300,
                disabledLabelColor = AppColors.OnSurfaceVariant
            )
        )

        // Error message
        if (isError && errorMessage != null) {
            VerticalSpacerXSmall()
            Text(
                text = errorMessage,
                color = AppColors.Error,
                style = AppTypography.BodySmall
            )
        }
    }
}

/**
 * Search Text Field Component
 *
 * A specialized text field optimized for search functionality.
 *
 * @param value Current search query
 * @param onValueChange Callback when query changes
 * @param placeholder Placeholder text
 * @param modifier Optional modifier
 */
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Search",
        placeholder = placeholder,
        modifier = modifier,
        singleLine = true
    )
}
