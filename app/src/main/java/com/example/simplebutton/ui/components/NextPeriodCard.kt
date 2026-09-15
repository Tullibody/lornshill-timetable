package com.example.simplebutton.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.PeriodCountdownStatus
import com.example.simplebutton.model.PeriodState
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillActiveCardGradient
import com.example.simplebutton.ui.theme.LornshillCardGradient
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillPillGradient
import com.example.simplebutton.ui.theme.LornshillSky
import com.example.simplebutton.ui.theme.LornshillSuccess
import com.example.simplebutton.ui.theme.LornshillSuccessBg
import com.example.simplebutton.ui.theme.LornshillSuccessPillGradient
import com.example.simplebutton.ui.theme.LornshillWarning
import com.example.simplebutton.ui.theme.LornshillWarningBg
import com.example.simplebutton.ui.theme.LornshillWarningPillGradient
import com.example.simplebutton.ui.theme.Staatliches

@Composable
fun NextPeriodCard(
    currentPeriod: TimetablePeriod?,
    nextPeriod: TimetablePeriod?,
    countdownStatus: PeriodCountdownStatus?,
    modifier: Modifier = Modifier
) {
    if (currentPeriod != null) {
        ActivePeriodCard(
            period = currentPeriod,
            countdownStatus = countdownStatus,
            upcomingNext = nextPeriod,
            modifier = modifier
        )
    } else if (nextPeriod != null) {
        UpcomingPeriodCard(
            period = nextPeriod,
            countdownStatus = countdownStatus,
            modifier = modifier
        )
    } else {
        NoLessonsRemainingCard(modifier = modifier)
    }
}

@Composable
private fun ActivePeriodCard(
    period: TimetablePeriod,
    countdownStatus: PeriodCountdownStatus?,
    upcomingNext: TimetablePeriod?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillActiveCardGradient)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: NOW Badge & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981))))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = "NOW IN PROGRESS",
                                fontFamily = Staatliches,
                                fontSize = 13.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${period.formattedStartTime} – ${period.formattedEndTime}",
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subject & Department Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val deptEmoji = FacultyDatabase.getDepartmentEmojiForSubject(period.subject)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = deptEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = period.subject,
                        fontFamily = Lexend,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Countdown / Remaining Pill
                countdownStatus?.let { status ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(LornshillPillGradient)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = status.badgeText,
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Teacher Chip (Rooms removed)
                if (period.teacher.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFBAE6FD),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = period.teacher,
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE0F2FE)
                        )
                    }
                }

                // Next Period Sub-Strip
                if (upcomingNext != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NEXT",
                                    fontFamily = Staatliches,
                                    fontSize = 12.sp,
                                    color = Color(0xFF7DD3FC),
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val nextEmoji = FacultyDatabase.getDepartmentEmojiForSubject(upcomingNext.subject)
                                Text(text = nextEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = upcomingNext.subject,
                                    fontFamily = Lexend,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = upcomingNext.formattedStartTime,
                                fontFamily = Lexend,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingPeriodCard(
    period: TimetablePeriod,
    countdownStatus: PeriodCountdownStatus?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillCardGradient)
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row: NEXT PERIOD & Start Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NEXT PERIOD",
                        fontFamily = Staatliches,
                        fontSize = 13.sp,
                        color = Color(0xFF93C5FD),
                        letterSpacing = 1.2.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = period.formattedStartTime,
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subject Name & Department Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val deptEmoji = FacultyDatabase.getDepartmentEmojiForSubject(period.subject)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = deptEmoji, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = period.subject,
                        fontFamily = Lexend,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Distinct Glowing Countdown Pill
                countdownStatus?.let { status ->
                    val pillBrush = when {
                        status.state == PeriodState.STARTING_NOW -> LornshillSuccessPillGradient
                        status.minutesDiff <= 10 -> LornshillWarningPillGradient
                        else -> LornshillPillGradient
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBrush)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = status.badgeText,
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Teacher Chip (Rooms removed)
                if (period.teacher.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFBAE6FD),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = period.teacher,
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE0F2FE)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoLessonsRemainingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillCardGradient)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "ALL LESSONS COMPLETED TODAY",
                        fontFamily = Staatliches,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "You're done with all periods for today. Rest up!",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = Color(0xFFBAE6FD)
                    )
                }
            }
        }
    }
}
