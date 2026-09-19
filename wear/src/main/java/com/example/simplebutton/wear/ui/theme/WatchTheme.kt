package com.example.simplebutton.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WatchPureBlack = Color(0xFF000000)
val WatchSurfaceDark = Color(0xFF111827)
val WatchSurfaceCard = Color(0xFF1E293B)
val WatchSurfaceHighlight = Color(0xFF334155)

val WatchCobalt = Color(0xFF3B82F6)
val WatchCobaltDark = Color(0xFF1D4ED8)
val WatchCyan = Color(0xFF06B6D4)
val WatchEmerald = Color(0xFF10B981)
val WatchAmber = Color(0xFFF59E0B)
val WatchRose = Color(0xFFF43F5E)

val WatchTextPrimary = Color(0xFFF8FAFC)
val WatchTextSecondary = Color(0xFF94A3B8)
val WatchTextMuted = Color(0xFF64748B)

private val WatchColorPalette = Colors(
    primary = WatchCobalt,
    primaryVariant = WatchCobaltDark,
    secondary = WatchCyan,
    background = WatchPureBlack,
    surface = WatchSurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = WatchTextPrimary,
    onSurface = WatchTextPrimary,
    onSurfaceVariant = WatchTextSecondary,
    error = WatchRose
)

@Composable
fun LornshillWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WatchColorPalette,
        content = content
    )
}
