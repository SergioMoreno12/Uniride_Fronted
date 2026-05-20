package com.example.uniride.ui.theme

import android.app.Activity
import android.os.Build
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
    onSecondaryContainer = Color(0xFF004D54),
    tertiary             = Orange500,
    onTertiary           = Color.White,
    tertiaryContainer    = Orange100,
    onTertiaryContainer  = Color(0xFF4A2500),
    background           = BgLight,
    onBackground         = Color(0xFF0D1117),
    surface              = SurfaceLight,
    onSurface            = Color(0xFF0D1117),
    surfaceVariant       = CardLight,
    onSurfaceVariant     = Color(0xFF3D4A6B),
    outline              = Color(0xFFBCC5D8),
    error                = Color(0xFFD32F2F),
    onError              = Color.White,
    surfaceContainer     = CardLight,
)

private val DarkColors = darkColorScheme(
    primary              = Blue300,
    onPrimary            = Color(0xFF003366),
    primaryContainer     = Color(0xFF1565C0),
    onPrimaryContainer   = Blue100,
    secondary            = Green300,
    onSecondary          = Color(0xFF002B30),
    secondaryContainer   = Color(0xFF006978),
    onSecondaryContainer = Green100,
    tertiary             = Orange300,
    onTertiary           = Color(0xFF3E1F00),
    tertiaryContainer    = Color(0xFF7A4200),
    onTertiaryContainer  = Orange100,
    background           = BgDark,
    onBackground         = TextPrimary,
    surface              = SurfaceDark,
    onSurface            = TextPrimary,
    surfaceVariant       = CardDark,
    onSurfaceVariant     = TextSecondary,
    outline              = DividerDark,
    error                = Color(0xFFFF6B6B),
    onError              = Color(0xFF3D0000),
    surfaceContainer     = ElevatedDark,
    inverseSurface       = Color(0xFFE0E0E0),
    inverseOnSurface     = Color(0xFF121212),
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

object ThemeState {
    var themeMode = mutableStateOf(ThemeMode.SYSTEM)
    // Legacy compatibility
    var isDarkMode: Boolean
        get() = themeMode.value == ThemeMode.DARK
        set(value) { themeMode.value = if (value) ThemeMode.DARK else ThemeMode.LIGHT }
}

@Composable
fun UnirideTheme(
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (ThemeState.themeMode.value) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> systemDark
    }

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