package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AppThemeMode

// 1. Classic Islamic Emerald & Gold (Default on Install)
val EmeraldColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Emerald950,
    primaryContainer = Emerald800,
    onPrimaryContainer = Gold300,
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

// 2. Dark Navy Blue (Deep Navy background and all text white)
val NavyBlueColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF60A5FA),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF93C5FD),
    onTertiary = Color(0xFF0F172A),
    background = Color(0xFF0A1128),
    onBackground = Color.White,
    surface = Color(0xFF101F42),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF172B5E),
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = Color(0xFF25427D),
    outlineVariant = Color(0xFF1D3567),
    error = UrgentRed,
    onError = Color.White
)

// 3. Clean White (White background and all text black)
val PureWhiteColorScheme = lightColorScheme(
    primary = Color(0xFF0F172A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F5F9),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFF475569),
    onTertiary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFF1E293B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = UrgentRed,
    onError = Color.White
)

// 4. OLED Pitch Black (Black background and all text white)
val OledBlackColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF18181B),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFE4E4E7),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFA1A1AA),
    onTertiary = Color.Black,
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0D0D0D),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFF4F4F5),
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0xFF27272A),
    error = UrgentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.EMERALD_GREEN,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when (themeMode) {
        AppThemeMode.EMERALD_GREEN -> EmeraldColorScheme
        AppThemeMode.NAVY_BLUE -> NavyBlueColorScheme
        AppThemeMode.PURE_WHITE -> PureWhiteColorScheme
        AppThemeMode.OLED_BLACK -> OledBlackColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


