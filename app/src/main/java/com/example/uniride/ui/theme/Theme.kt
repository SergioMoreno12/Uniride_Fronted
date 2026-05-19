package com.example.uniride.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary              = BluePrimary,
    onPrimary            = Color.White,
    primaryContainer     = BlueContainer,
    onPrimaryContainer   = BlueDark,
    secondary            = BlueLight,
    onSecondary          = Color.White,
    background           = BgLight,
    onBackground         = TextDark,
    surface              = SurfaceLight,
    onSurface            = TextDark,
    surfaceVariant       = Color(0xFFE3F2FD),
    onSurfaceVariant     = Color(0xFF444444),
)

private val DarkColors = darkColorScheme(
    primary              = BlueOnDark,
    onPrimary            = Color(0xFF003C8F),
    primaryContainer     = BlueDark,
    onPrimaryContainer   = BlueContainer,
    secondary            = BlueLight,
    onSecondary          = Color.Black,
    background           = BgDark,
    onBackground         = TextLight,
    surface              = SurfaceDark,
    onSurface            = TextLight,
    surfaceVariant       = SurfaceVariantDark,
    onSurfaceVariant     = Color(0xFFADBBC4),
)

@Composable
fun UnirideTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = if (darkTheme) BgDark.toArgb() else BluePrimary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = AppTypography,
        content     = content
    )
}