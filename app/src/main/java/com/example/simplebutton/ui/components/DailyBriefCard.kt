package com.example.simplebutton.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.DailyBriefSummary
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors

@Composable
fun DailyBriefCard(
    summary: DailyBriefSummary,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFFF59E0B))
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🌅", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MORNING DAILY BRIEF",
                                fontFamily = Staatliches,
                                fontSize = 16.sp,
                                color = colors.textPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (colors.isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFEF3C7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "07:30 – 08:55",
                                    fontFamily = Lexend,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (colors.isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                )
                            }
                        }
                        Text(
                            text = summary.headline,
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges Overview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // P.E. Kit Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (summary.needsPeKit) {
                                if (colors.isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)
                            } else colors.chipBg
                        )
                        .border(
                            1.dp,
                            if (summary.needsPeKit) {
                                if (colors.isDark) Color(0xFF3B82F6) else Color(0xFF93C5FD)
                            } else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (summary.needsPeKit) "👟 P.E. KIT" else "👔 P.E. KIT",
                                fontFamily = Lexend,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.needsPeKit) (if (colors.isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)) else colors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (summary.needsPeKit) "Required Today" else "Not Needed",
                            fontFamily = Lexend,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.needsPeKit) (if (colors.isDark) Color(0xFFBAE6FD) else Color(0xFF1E3A8A)) else colors.textPrimary
                        )
                    }
                }

                // Core Subjects Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (summary.hasMaths || summary.hasEnglish) {
                                if (colors.isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFF0FDF4)
                            } else colors.chipBg
                        )
                        .border(
                            1.dp,
                            if (summary.hasMaths || summary.hasEnglish) {
                                if (colors.isDark) Color(0xFF10B981) else Color(0xFF86EFAC)
                            } else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "📚 CORE SUBJECTS",
                            fontFamily = Lexend,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.hasMaths || summary.hasEnglish) (if (colors.isDark) Color(0xFF86EFAC) else Color(0xFF15803D)) else colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val label = when {
                            summary.hasMaths && summary.hasEnglish -> "Maths & English"
                            summary.hasMaths -> "Maths Scheduled"
                            summary.hasEnglish -> "English Scheduled"
                            else -> "No Core Today"
                        }
                        Text(
                            text = label,
                            fontFamily = Lexend,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.hasMaths || summary.hasEnglish) (if (colors.isDark) Color(0xFFA7F3D0) else Color(0xFF14532D)) else colors.textPrimary
                        )
                    }
                }

                // Doubles Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (summary.doubles.isNotEmpty()) {
                                if (colors.isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFEF3C7)
                            } else colors.chipBg
                        )
                        .border(
                            1.dp,
                            if (summary.doubles.isNotEmpty()) {
                                if (colors.isDark) Color(0xFFF59E0B) else Color(0xFFFCD34D)
                            } else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "⚡ DOUBLES",
                            fontFamily = Lexend,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.doubles.isNotEmpty()) (if (colors.isDark) Color(0xFFFDE68A) else Color(0xFFB45309)) else colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (summary.doubles.isNotEmpty()) "${summary.doubles.size} Double(s)" else "None Today",
                            fontFamily = Lexend,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.doubles.isNotEmpty()) (if (colors.isDark) Color(0xFFFEF3C7) else Color(0xFF78350F)) else colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Bullets
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardSecondaryBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                summary.bulletPoints.forEach { pt ->
                    Text(
                        text = pt,
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = colors.textPrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accentCobalt.copy(alpha = 0.12f))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Dismiss Brief",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accentCobalt
                    )
                }
            }
        }
    }
}
