package com.example.simplebutton.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.PeriodCountdownStatus
import com.example.simplebutton.model.PeriodState
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillPillGradient
import com.example.simplebutton.ui.theme.LornshillSuccessPillGradient
import com.example.simplebutton.ui.theme.LornshillWarningPillGradient
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors

/**
 * Modern floating bar at the bottom displaying the current or next school period
 * with a live countdown ticker, semi-transparent frosted glass design, and quick
 * tap interaction to open the full timetable view.
 */
@Composable
fun FloatingNextPeriodBar(
    currentPeriod: TimetablePeriod?,
    nextPeriod: TimetablePeriod?,
    countdownStatus: PeriodCountdownStatus?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val isDark = colors.isDark

    // Live pulsing animation for active/upcoming status
    val infiniteTransition = rememberInfiniteTransition(label = "floatingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val displayedPeriod = currentPeriod ?: nextPeriod
    val isCurrentlyActive = currentPeriod != null

    // Modern glassmorphism background: semi-transparent, frosted surface with luminous border
    val glassBgColor = if (isDark) {
        Color(0xDF081A36)
    } else {
        Color(0xF0FFFFFF)
    }

    val glassBorderBrush = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF38BDF8).copy(alpha = 0.65f),
                Color(0xFF818CF8).copy(alpha = 0.45f),
                Color(0xFF38BDF8).copy(alpha = 0.65f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFCBD5E1).copy(alpha = 0.9f),
                Color(0xFF93C5FD).copy(alpha = 0.6f),
                Color(0xFFCBD5E1).copy(alpha = 0.9f)
            )
        )
    }

    val barShape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 14.dp else 10.dp,
                shape = barShape,
                spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color(0x33000000)
            )
            .border(
                width = 1.dp,
                brush = glassBorderBrush,
                shape = barShape
            )
            .clip(barShape)
            .clickable { onTap() },
        shape = barShape,
        color = glassBgColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Content: Period badge, emoji, subject name & detail
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayedPeriod != null) {
                    val deptEmoji = FacultyDatabase.getDepartmentEmojiForSubject(displayedPeriod.subject)

                    // Period Icon Box with indicator badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCurrentlyActive) {
                                    Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
                                } else if (isDark) {
                                    Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF2563EB)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deptEmoji,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Status tag (NOW / NEXT)
                            Text(
                                text = if (isCurrentlyActive) "NOW" else "NEXT",
                                fontFamily = Staatliches,
                                fontSize = 11.5.sp,
                                color = if (isCurrentlyActive) Color(0xFF10B981) else colors.accentCobalt,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "· P${displayedPeriod.periodIndex}",
                                fontFamily = Lexend,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = displayedPeriod.subject.ifBlank { "Free Period" },
                            fontFamily = Lexend,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    // Fallback state when no lessons remain
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = colors.accentCobalt,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIMETABLE",
                            fontFamily = Staatliches,
                            fontSize = 11.sp,
                            color = colors.accentCobalt,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "View Full Schedule",
                            fontFamily = Lexend,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Content: Live Countdown Pill & Forward Arrow
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (countdownStatus != null) {
                    val pillBrush = when {
                        countdownStatus.state == PeriodState.IN_PROGRESS -> LornshillSuccessPillGradient
                        countdownStatus.state == PeriodState.STARTING_NOW -> LornshillSuccessPillGradient
                        countdownStatus.minutesDiff <= 10 -> LornshillWarningPillGradient
                        else -> LornshillPillGradient
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(pillBrush)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCurrentlyActive || countdownStatus.state == PeriodState.STARTING_NOW) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = countdownStatus.badgeText,
                                fontFamily = Lexend,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else if (displayedPeriod != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.blueContainer)
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = displayedPeriod.formattedStartTime,
                            fontFamily = Lexend,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentCobalt
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Full Timetable",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
