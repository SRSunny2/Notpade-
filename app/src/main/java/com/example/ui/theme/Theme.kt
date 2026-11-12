package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldDarkPrimary,
    onPrimary = Color(0xFF003822),
    primaryContainer = EmeraldDarkPrimaryContainer,
    onPrimaryContainer = Emerald200,
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF003549),
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color(0xFFBAE6FD),
    background = EmeraldDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = EmeraldDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = EmeraldDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF64748B)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldLightPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLightPrimaryContainer,
    onPrimaryContainer = Emerald900,
    secondary = OceanBlue700,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    background = EmeraldLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = EmeraldLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = EmeraldLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
