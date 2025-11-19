package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fivucsas.shared.config.UIDimens
import com.fivucsas.shared.ui.components.atoms.BodyMediumText
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppShapes

/**
 * Message Banner Component
 *
 * A banner for displaying messages (success, error, info, warning).
 *
 * @param message Message text
 * @param type Message type (determines color and icon)
 * @param modifier Optional modifier
 */
@Composable
fun MessageBanner(
    message: String,
    type: MessageType = MessageType.INFO,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon) = when (type) {
        MessageType.SUCCESS -> Triple(
            AppColors.Success.copy(alpha = 0.1f),
            AppColors.Success,
            Icons.Default.CheckCircle
        )

        MessageType.ERROR -> Triple(
            AppColors.Error.copy(alpha = 0.1f),
            AppColors.Error,
            Icons.Default.Warning
        )

        MessageType.WARNING -> Triple(
            AppColors.Warning.copy(alpha = 0.1f),
            AppColors.Warning,
            Icons.Default.Warning
        )

        MessageType.INFO -> Triple(
            AppColors.Info.copy(alpha = 0.1f),
            AppColors.Info,
            Icons.Default.CheckCircle
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UIDimens.ElevationNone
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UIDimens.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(UIDimens.IconSmall)
            )

            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(end = UIDimens.SpacingSmall)
            )

            BodyMediumText(
                text = message,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Message Type Enum
 */
enum class MessageType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Success Message Component
 */
@Composable
fun SuccessMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    MessageBanner(
        message = message,
        type = MessageType.SUCCESS,
        modifier = modifier
    )
}

/**
 * Error Message Component
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    MessageBanner(
        message = message,
        type = MessageType.ERROR,
        modifier = modifier
    )
}

/**
 * Warning Message Component
 */
@Composable
fun WarningMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    MessageBanner(
        message = message,
        type = MessageType.WARNING,
        modifier = modifier
    )
}

/**
 * Info Message Component
 */
@Composable
fun InfoMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    MessageBanner(
        message = message,
        type = MessageType.INFO,
        modifier = modifier
    )
}
