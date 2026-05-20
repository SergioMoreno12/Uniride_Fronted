package com.example.uniride.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary              = Blue500,
    onPrimary            = Color.White,
    primaryContainer     = Blue100,
    onPrimaryContainer   = Blue900,
    secondary            = Green500,
    onSecondary          = Color.White,
    secondaryContainer   = Green100,
    onSecondaryContainer = Color(0xFF004D40),
    tertiary             = Orange500,
    onTertiary           = Color.White,
    tertiaryContainer    = Orange100,
    onTertiaryContainer  = Color(0xFF7F2700),
    background           = BgLight,
    onBackground         = Color(0xFF0D1117),
    surface              = SurfaceLight,
    onSurface            = Color(0xFF0D1117),
    surfaceVariant       = CardLight,
    onSurfaceVariant     = Color(0xFF3D4A6B),
    outline              = Color(0xFFBCC5D8),
    error                = Color(0xFFD32F2F),
    onError              = Color.White,
)

private val DarkColors = darkColorScheme(
    primary              = Blue300,
    onPrimary            = Color(0xFF001A3D),
    primaryContainer     = Blue900,
    onPrimaryContainer   = Blue100,
    secondary            = Green300,
    onSecondary          = Color(0xFF002925),
    secondaryContainer   = Color(0xFF00413A),
    onSecondaryContainer = Green100,
    tertiary             = Orange300,
    onTertiary           = Color(0xFF4A1500),
    tertiaryContainer    = Color(0xFF7F2700),
    onTertiaryContainer  = Orange100,
    background           = BgDark,
    onBackground         = TextDarkMode,
    surface              = SurfaceDark,
    onSurface            = TextDarkMode,
    surfaceVariant       = CardDark,
    onSurfaceVariant     = SubtextDark,
    outline              = Color(0xFF3A4560),
    error                = Color(0xFFFF6B6B),
    onError              = Color(0xFF3D0000),
)

// Estado global del tema — persiste en memoria durante la sesión
object ThemeState {
    var isDarkMode = mutableStateOf(false)
}

@Composable
fun UnirideTheme(
    darkTheme: Boolean = ThemeState.isDarkMode.value,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) BgDark.toArgb() else Blue500.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography  = AppTypography,
        content     = content
    )
}