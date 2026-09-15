package com.example.simplebutton.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LornshillLightBlue,
    onPrimary = LornshillMidnight,
    primaryContainer = LornshillNavy,
    onPrimaryContainer = LornshillSkyLight,
    secondary = LornshillCobalt,
    onSecondary = Color.White,
    secondaryContainer = LornshillNavyLight,
    onSecondaryContainer = LornshillSky,
    background = LornshillMidnight,
    onBackground = Color(0xFFF1F5F9),
    surface = LornshillNavyDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = LornshillNavy,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF1E3A8A)
)

private val LightColorScheme = lightColorScheme(
    primary = LornshillNavy,
    onPrimary = Color.White,
    primaryContainer = LornshillBlueContainer,
    onPrimaryContainer = LornshillNavy,
    secondary = LornshillCobalt,
    onSecondary = Color.White,
    secondaryContainer = LornshillSkyLight,
    onSecondaryContainer = LornshillNavyDark,
    tertiary = LornshillBlueMedium,
    background = LornshillBackground,
    onBackground = LornshillTextPrimary,
    surface = LornshillSurface,
    onSurface = LornshillTextPrimary,
    surfaceVariant = LornshillSurfaceTinted,
    onSurfaceVariant = LornshillTextSecondary,
    outline = LornshillBorder
)

data class AppColors(
    val isDark: Boolean,
    val canvasBg: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val cardSecondaryBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentCobalt: Color,
    val blueContainer: Color,
    val navBarBg: Color,
    val navBarIndicator: Color,
    val breakCardBg: Color,
    val activeCardBg: Color,
    val glassBg: Color,
    val glassBorder: Color,
    val chipBg: Color,
    val chipText: Color
)

val LightAppColors = AppColors(
    isDark = false,
    canvasBg = Color(0xFFF3F6FB),
    cardBg = Color.White,
    cardBorder = Color(0xFFE2E8F0),
    cardSecondaryBg = Color(0xFFF8FAFC),
    textPrimary = Color(0xFF0B254E),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    accentCobalt = Color(0xFF1D4ED8),
    blueContainer = Color(0xFFEEF5FF),
    navBarBg = Color.White,
    navBarIndicator = Color(0xFFEEF5FF),
    breakCardBg = Color(0xFFEBF3FC),
    activeCardBg = Color(0xFFF0F6FF),
    glassBg = Color(0xFFFFFFFF).copy(alpha = 0.85f),
    glassBorder = Color(0xFFCBD5E1).copy(alpha = 0.7f),
    chipBg = Color(0xFFF1F5F9),
    chipText = Color(0xFF0B254E)
)

val DarkAppColors = AppColors(
    isDark = true,
    canvasBg = Color(0xFF050F1E),
    cardBg = Color(0xFF0B1F3B),
    cardBorder = Color(0xFF1B3D6D),
    cardSecondaryBg = Color(0xFF0E274A),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    accentCobalt = Color(0xFF38BDF8),
    blueContainer = Color(0xFF13325B),
    navBarBg = Color(0xFF071731),
    navBarIndicator = Color(0xFF173F73),
    breakCardBg = Color(0xFF0A2244),
    activeCardBg = Color(0xFF10305C),
    glassBg = Color(0xFF081C38).copy(alpha = 0.85f),
    glassBorder = Color(0xFF38BDF8).copy(alpha = 0.45f),
    chipBg = Color(0xFF132D52),
    chipText = Color(0xFFE2E8F0)
)

val LocalAppColors = androidx.compose.runtime.staticCompositionLocalOf { LightAppColors }

@Composable
fun appColors(): AppColors = LocalAppColors.current

@Composable
fun LornshillTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appThemeColors = if (darkTheme) DarkAppColors else LightAppColors

    androidx.compose.runtime.CompositionLocalProvider(LocalAppColors provides appThemeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Keep SimpleButtonTheme alias for backwards compatibility
@Composable
fun SimpleButtonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LornshillTheme(darkTheme = darkTheme, content = content)
}

