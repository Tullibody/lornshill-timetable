package com.example.simplebutton.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillTextMuted
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors

@Composable
fun PeriodRowItem(
    period: TimetablePeriod,
    isActive: Boolean = false,
    isNext: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val isBreak = period.isBreakOrLunch ||
        period.subject.contains("Break", ignoreCase = true) ||
        period.subject.contains("Interval", ignoreCase = true) ||
        period.subject.contains("Lunch", ignoreCase = true)

    val cardBg = when {
        isActive -> colors.activeCardBg
        isBreak -> colors.breakCardBg
        else -> colors.cardBg
    }

    val elevationDp = if (isActive) 4.dp else 1.5.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isBreak) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = elevationDp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period Number Badge
            if (period.isBreakOrLunch) {
                val isLunch = period.subject.contains("Lunch", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.chipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLunch) "LCH" else "BRK",
                        fontFamily = Staatliches,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                val inactiveBadgeBrush = if (colors.isDark) {
                    androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF132F57), Color(0xFF1B4075)))
                } else {
                    androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE0EDFB), Color(0xFFD6E8FB)))
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) LornshillButtonGradient else inactiveBadgeBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P${period.periodIndex}",
                        fontFamily = Staatliches,
                        fontSize = 15.sp,
                        color = if (isActive || colors.isDark) Color.White else LornshillNavy,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Subject, Time & Teacher Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (period.isAssigned) {
                        val deptEmoji = FacultyDatabase.getDepartmentEmojiForSubject(period.subject)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.chipBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = deptEmoji,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(7.dp))
                    }

                    val subjectDisplay = when {
                        period.isAssigned -> period.subject
                        period.subject.isNotBlank() -> period.subject
                        else -> "Free / Study Period (Tap to set)"
                    }

                    Text(
                        text = subjectDisplay,
                        fontFamily = Lexend,
                        fontSize = 15.sp,
                        fontWeight = if (period.isAssigned) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            period.isBreakOrLunch -> colors.textSecondary
                            period.isAssigned -> colors.textPrimary
                            else -> colors.textMuted
                        }
                    )

                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.accentCobalt
                        ) {
                            Text(
                                text = "NOW",
                                fontFamily = Staatliches,
                                fontSize = 11.sp,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (isNext) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.blueContainer
                        ) {
                            Text(
                                text = "NEXT",
                                fontFamily = Staatliches,
                                fontSize = 11.sp,
                                color = colors.accentCobalt,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = period.formattedTime,
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accentCobalt
                    )

                    if (period.teacher.isNotBlank()) {
                        Text(
                            text = " · ",
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )

                        Text(
                            text = period.teacher,
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Edit Indicator (only shown if editable)
            if (!isBreak) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Lesson",
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.chipBg
                ) {
                    Text(
                        text = "LOCKED",
                        fontFamily = Staatliches,
                        fontSize = 10.sp,
                        color = colors.textMuted,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BreakRowItem(
    title: String,
    timeText: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.breakCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title.uppercase(),
                    fontFamily = Staatliches,
                    fontSize = 13.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.8.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeText,
                    fontFamily = Lexend,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accentCobalt
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (colors.isDark) Color(0xFF173866) else Color.White.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "FIXED BREAK",
                        fontFamily = Staatliches,
                        fontSize = 10.sp,
                        color = if (colors.isDark) Color(0xFFBAE6FD) else LornshillNavy,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
