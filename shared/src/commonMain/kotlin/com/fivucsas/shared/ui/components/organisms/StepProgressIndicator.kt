package com.fivucsas.shared.ui.components.organisms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fivucsas.shared.presentation.state.StepDisplayStatus
import com.fivucsas.shared.ui.theme.AppColors

/**
 * Data describing a single step in the progress indicator.
 */
data class StepIndicatorItem(
    val label: String,
    val methodType: String,
    val status: StepDisplayStatus
)

/**
 * Visual stepper component showing step progress for multi-step auth flows.
 *
 * Displays step numbers in circles connected by lines, with
 * completed/current/upcoming/failed states distinguished by color.
 */
@Composable
fun StepProgressIndicator(
    steps: List<StepIndicatorItem>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step circles with connecting lines
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                StepCircle(
                    stepNumber = index + 1,
                    status = step.status
                )

                // Connecting line between steps
                if (index < steps.size - 1) {
                    StepConnectorLine(
                        isCompleted = step.status == StepDisplayStatus.COMPLETED
                                || step.status == StepDisplayStatus.SKIPPED,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Step labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, step ->
                val displayLabel = methodTypeToLabel(step.methodType)
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (index == currentStepIndex) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = when (step.status) {
                        StepDisplayStatus.COMPLETED -> AppColors.Success
                        StepDisplayStatus.IN_PROGRESS -> AppColors.Primary
                        StepDisplayStatus.FAILED -> AppColors.Error
                        StepDisplayStatus.SKIPPED -> AppColors.Gray500
                        StepDisplayStatus.PENDING -> AppColors.Gray400
                    },
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StepCircle(
    stepNumber: Int,
    status: StepDisplayStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status) {
        StepDisplayStatus.COMPLETED -> AppColors.Success
        StepDisplayStatus.IN_PROGRESS -> AppColors.Primary
        StepDisplayStatus.FAILED -> AppColors.Error
        StepDisplayStatus.SKIPPED -> AppColors.Gray400
        StepDisplayStatus.PENDING -> AppColors.Gray300
    }

    val textColor = when (status) {
        StepDisplayStatus.COMPLETED,
        StepDisplayStatus.IN_PROGRESS,
        StepDisplayStatus.FAILED -> AppColors.White
        StepDisplayStatus.SKIPPED -> AppColors.White
        StepDisplayStatus.PENDING -> AppColors.Gray600
    }

    val displayText = when (status) {
        StepDisplayStatus.COMPLETED -> "\u2713" // checkmark
        StepDisplayStatus.SKIPPED -> "\u2192"   // arrow
        else -> stepNumber.toString()
    }

    val size = if (status == StepDisplayStatus.IN_PROGRESS) 32.dp else 28.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = textColor,
            fontSize = if (status == StepDisplayStatus.IN_PROGRESS) 14.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StepConnectorLine(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (isCompleted) AppColors.Success else AppColors.Gray300
    Canvas(
        modifier = modifier
            .height(2.dp)
            .padding(horizontal = 4.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = size.height
        )
    }
}

/**
 * Maps backend method type strings to short human-readable labels.
 */
private fun methodTypeToLabel(methodType: String): String {
    return when (methodType.uppercase()) {
        "PASSWORD" -> "Password"
        "FACE" -> "Face"
        "VOICE" -> "Voice"
        "TOTP" -> "TOTP"
        "EMAIL_OTP" -> "Email"
        "SMS_OTP" -> "SMS"
        "QR_CODE" -> "QR"
        "FINGERPRINT" -> "Fingerprint"
        "HARDWARE_KEY" -> "Security Key"
        "NFC_DOCUMENT" -> "NFC"
        else -> methodType
    }
}
