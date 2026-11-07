package com.gabriel.cinelog.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private fun style(size: TextUnit, weight: FontWeight, lineHeight: TextUnit) = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = 0.sp,
)

private val LargeTitle = style(28.sp, FontWeight.Bold, 40.sp)
private val Title = style(22.sp, FontWeight.SemiBold, 32.sp)
private val Body = style(16.sp, FontWeight.Normal, 24.sp)
private val Caption = style(13.sp, FontWeight.Normal, 20.sp)
private val CaptionStrong = style(13.sp, FontWeight.Medium, 20.sp)
private val Small = style(11.sp, FontWeight.Medium, 16.sp)

val Typography = Typography(
    headlineMedium = LargeTitle,
    headlineSmall = Title,
    titleLarge = Title,
    titleMedium = Body,
    titleSmall = CaptionStrong,
    bodyLarge = Body,
    bodyMedium = Body,
    bodySmall = Caption,
    labelLarge = CaptionStrong,
    labelMedium = Small,
    labelSmall = Small,
)
