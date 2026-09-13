package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WinampDarkColorScheme = darkColorScheme(
    primary = WinampNeonGreen,
    onPrimary = Color.Black,
    primaryContainer = WinampSurfaceElevated,
    onPrimaryContainer = WinampNeonGreen,
    secondary = WinampElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = WinampSurfaceVariant,
    onSecondaryContainer = WinampElectricCyan,
    tertiary = WinampNeonAmber,
    onTertiary = Color.Black,
    background = WinampBackground,
    onBackground = WinampTextPrimary,
    surface = WinampSurface,
    onSurface = WinampTextPrimary,
    surfaceVariant = WinampSurfaceVariant,
    onSurfaceVariant = WinampTextSecondary,
    outline = WinampBorder,
    outlineVariant = WinampBorderHighlight,
    error = WinampAlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // The requirement explicitly mandates a dark mode interface with Winamp aesthetics
    MaterialTheme(
        colorScheme = WinampDarkColorScheme,
        typography = Typography,
        content = content
    )
}
