package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Emerald950,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    secondary = Gold400,
    onSecondary = Gold900,
    secondaryContainer = Gold700,
    onSecondaryContainer = Gold100,
    tertiary = Gold300,
    onTertiary = Gold900,
    background = DarkBg,
    onBackground = Color(0xFFF1F5F2),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F2),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCADBD1),
    outline = BorderDark,
    outlineVariant = Color(0xFF1E4336),
    error = UrgentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald800,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = Gold600,
    onSecondary = Color.White,
    secondaryContainer = Gold100,
    onSecondaryContainer = Gold900,
    tertiary = Emerald600,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = Color(0xFF11221A),
    surface = LightSurface,
    onSurface = Color(0xFF11221A),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF384F44),
    outline = BorderLight,
    outlineVariant = Color(0xFFE2ECE7),
    error = UrgentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize rich bespoke Islamic theme
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

