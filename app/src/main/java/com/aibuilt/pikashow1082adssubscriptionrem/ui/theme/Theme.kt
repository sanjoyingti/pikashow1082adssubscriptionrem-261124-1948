package com.aibuilt.pikashow1082adssubscriptionrem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Grey = Color(0xFF5B6270)

private val DarkColors = darkColorScheme(
    primary = PikaRed,
    onPrimary = PikaWhite,
    primaryContainer = PikaRedDark,
    onPrimaryContainer = PikaWhite,
    secondary = PikaAmber,
    onSecondary = PikaInk,
    background = PikaInk,
    onBackground = PikaWhite,
    surface = PikaInkSoft,
    onSurface = PikaWhite,
    surfaceVariant = PikaInkCard,
    onSurfaceVariant = PikaGrey,
    outline = PikaGrey
)

private val LightColors = lightColorScheme(
    primary = PikaRed,
    onPrimary = PikaWhite,
    primaryContainer = PikaRedDark,
    onPrimaryContainer = PikaWhite,
    secondary = PikaRedDark,
    onSecondary = PikaWhite,
    background = PikaSurfaceLight,
    onBackground = PikaInk,
    surface = PikaSurfaceLight,
    onSurface = PikaInk,
    surfaceVariant = PikaCardLight,
    onSurfaceVariant = Grey,
    outline = Grey
)

@Composable
fun PikaTheme(themeMode: Int = 0, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = PikaTypography,
        content = content
    )
}
