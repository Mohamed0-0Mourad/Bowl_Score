package com.example.bowl_score.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Top-level color variables (accessible anywhere in your app)
val DeepViolet = Color(0xFF2E004E)
val ElectricPurple = Color(0xFFBF00FF)
val NeonPink = Color(0xFFFF00FF)

@Composable
fun BowlingVisionTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = ElectricPurple,
        secondary = NeonPink,
        background = DeepViolet,
        surface = DeepViolet,
        onPrimary = Color.White,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BowlingTypography, // We will define this in the next step
        content = content
    )
}