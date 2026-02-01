package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppTypography

@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = calculateStrength(password)
    val progress = strength.score / 4f
    val (label, color) = strengthLabel(strength.score)

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = "Strength: ",
                style = AppTypography.BodySmall,
                color = AppColors.OnSurfaceVariant
            )
            Text(
                text = label,
                style = AppTypography.BodySmall,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = AppColors.Gray200,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strength.hint,
            style = AppTypography.BodySmall,
            color = AppColors.OnSurfaceVariant
        )
    }
}

private data class Strength(val score: Int, val hint: String)

private fun calculateStrength(password: String): Strength {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    val hint = when {
        password.isBlank() -> "Use at least 8 characters."
        password.length < 8 -> "Add more characters."
        !password.any { it.isUpperCase() } -> "Add an uppercase letter."
        !password.any { it.isLowerCase() } -> "Add a lowercase letter."
        !password.any { it.isDigit() } -> "Add a number."
        else -> "Looks good."
    }
    return Strength(score, hint)
}

private fun strengthLabel(score: Int): Pair<String, Color> {
    return when (score) {
        0, 1 -> "Weak" to AppColors.Error
        2 -> "Fair" to AppColors.Warning
        3 -> "Good" to AppColors.Info
        else -> "Strong" to AppColors.Success
    }
}
