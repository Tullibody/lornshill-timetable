package com.example.simplebutton.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Navy to Light Blue Palette
val LornshillMidnight = Color(0xFF050F1E)
val LornshillNavyDark = Color(0xFF071B38)
val LornshillNavy = Color(0xFF0B254E)
val LornshillNavyLight = Color(0xFF10366F)
val LornshillRichBlue = Color(0xFF16488E)
val LornshillCobalt = Color(0xFF1D4ED8)
val LornshillBlueMedium = Color(0xFF2563EB)
val LornshillLightBlue = Color(0xFF38BDF8)
val LornshillSky = Color(0xFF7DD3FC)
val LornshillSkyLight = Color(0xFFE0F2FE)
val LornshillBlueContainer = Color(0xFFEEF5FF)

// Canvas & Surfaces (Subtle cool blue tint, avoiding plain stark white)
val LornshillBackground = Color(0xFFF1F5F9)
val LornshillCanvasBg = Color(0xFFF3F6FB)
val LornshillSurface = Color(0xFFFFFFFF)
val LornshillSurfaceElevated = Color(0xFFFFFFFF)
val LornshillSurfaceTinted = Color(0xFFF8FAFD)
val LornshillBorder = Color(0xFFE2E8F0)
val LornshillBorderLight = Color(0xFFEDF2F7)

// Text Colors
val LornshillTextPrimary = Color(0xFF0F172A)
val LornshillTextSecondary = Color(0xFF475569)
val LornshillTextMuted = Color(0xFF94A3B8)
val LornshillTextOnDark = Color(0xFFFFFFFF)
val LornshillTextOnDarkSecondary = Color(0xFFCBD5E1)
val LornshillTextOnDarkMuted = Color(0xFF94A3B8)

// Status & Badge Accents
val LornshillSuccess = Color(0xFF059669)
val LornshillSuccessBg = Color(0xFFD1FAE5)
val LornshillWarning = Color(0xFFD97706)
val LornshillWarningBg = Color(0xFFFEF3C7)
val LornshillBadgeBg = Color(0xFFDBEAFE)
val LornshillBadgeText = Color(0xFF1E40AF)

// Gradients (Dark Navy -> Rich Blue -> Medium Blue -> Light Blue)
val LornshillHeroGradient = Brush.verticalGradient(
    colors = listOf(
        LornshillMidnight,
        LornshillNavyDark,
        LornshillNavy
    )
)

val LornshillHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF071731),
        Color(0xFF0D2854),
        Color(0xFF133C78)
    )
)

val LornshillCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF071835),
        Color(0xFF0E2D5E),
        Color(0xFF184589)
    )
)

val LornshillActiveCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF081C3D),
        Color(0xFF103670),
        Color(0xFF1D52A0)
    )
)

val LornshillButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF0F2C59),
        Color(0xFF1D4ED8),
        Color(0xFF2563EB)
    )
)

val LornshillPillGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF1D4ED8),
        Color(0xFF38BDF8)
    )
)

val LornshillWarningPillGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFD97706),
        Color(0xFFF59E0B)
    )
)

val LornshillSuccessPillGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF059669),
        Color(0xFF10B981)
    )
)

// School House Colors (Devon: River Devon Azure, Forebraes: Tartan Crimson, Grange: Burnished Amber, Ochil: Highland Pine)
val HouseDevon = Color(0xFF0284C7)        // River Devon Azure (distinct from cobalt UI buttons)
val HouseForebraes = Color(0xFFB91C1C)    // Forebraes Tartan Crimson (refined, dignified red)
val HouseGrange = Color(0xFFC27803)       // Grange Burnished Gold / Amber (warm rich tone)
val HouseOchil = Color(0xFF15803D)        // Ochil Highland Pine (rich Scottish evergreen)

fun getHouseColor(houseName: String): Color {
    return when (houseName.trim().lowercase()) {
        "devon" -> HouseDevon
        "forebraes" -> HouseForebraes
        "grange" -> HouseGrange
        "ochil" -> HouseOchil
        else -> HouseDevon
    }
}

fun getHouseContentColor(houseName: String): Color {
    return Color.White
}


