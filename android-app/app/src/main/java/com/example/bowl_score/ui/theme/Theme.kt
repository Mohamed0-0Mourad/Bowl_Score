package com.example.bowl_score.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BowlingVisionTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = ElectricPurple,
        secondary = NeonPink,
        tertiary = LightCyan,
        background = LightBackground,
        surface = LightSurface,
        surfaceVariant = LightSurfaceVariant,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = DarkText,
        onBackground = DarkText,
        onSurface = DarkText,
        onSurfaceVariant = DarkText
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BowlingTypography,
        content = content
    )
}
