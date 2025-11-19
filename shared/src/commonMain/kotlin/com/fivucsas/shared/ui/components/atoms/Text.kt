package com.fivucsas.shared.ui.components.atoms

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppTypography

/**
 * Display Text Components
 */
@Composable
fun DisplayLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.DisplayLarge,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun DisplayMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.DisplayMedium,
        color = color,
        textAlign = textAlign
    )
}

/**
 * Headline Text Components
 */
@Composable
fun HeadlineLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.HeadlineLarge,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun HeadlineMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.HeadlineMedium,
        color = color,
        textAlign = textAlign
    )
}

/**
 * Title Text Components
 */
@Composable
fun TitleLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.TitleLarge,
        color = color,
        textAlign = textAlign
    )
}

@Composable
fun TitleMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.TitleMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Body Text Components
 */
@Composable
fun BodyLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.BodyLarge,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun BodyMediumText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.BodyMedium,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

@Composable
fun BodySmallText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurfaceVariant,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.BodySmall,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow
    )
}

/**
 * Label Text Components
 */
@Composable
fun LabelLargeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = AppColors.OnSurface,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        style = AppTypography.LabelLarge,
        color = color,
        textAlign = textAlign
    )
}

/**
 * Error Text Component
 */
@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier
) {
    BodySmallText(
        text = text,
        modifier = modifier,
        color = AppColors.Error
    )
}

/**
 * Success Text Component
 */
@Composable
fun SuccessText(
    text: String,
    modifier: Modifier = Modifier
) {
    BodySmallText(
        text = text,
        modifier = modifier,
        color = AppColors.Success
    )
}
