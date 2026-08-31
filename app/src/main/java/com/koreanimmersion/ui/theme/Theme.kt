package com.koreanimmersion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenPrimary = Color(0xFF2D6A4F)
private val GreenSecondary = Color(0xFF40916C)
private val GreenBackground = Color(0xFFF8FAF9)
private val GreenSurface = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = GreenBackground,
    surface = GreenSurface
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF95D5B2),
    secondary = Color(0xFF74C69D),
    background = Color(0xFF1B4332),
    surface = Color(0xFF2D6A4F)
)

@Composable
fun KoreanImmersionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
