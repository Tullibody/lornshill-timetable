package com.example.simplebutton.wear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.simplebutton.model.PeriodCountdownStatus
import com.example.simplebutton.model.PeriodState
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.wear.ui.theme.WatchAmber
import com.example.simplebutton.wear.ui.theme.WatchCobalt
import com.example.simplebutton.wear.ui.theme.WatchCyan
import com.example.simplebutton.wear.ui.theme.WatchEmerald
import com.example.simplebutton.wear.ui.theme.WatchPureBlack
import com.example.simplebutton.wear.ui.theme.WatchSurfaceCard
import com.example.simplebutton.wear.ui.theme.WatchTextMuted
import com.example.simplebutton.wear.ui.theme.WatchTextPrimary
import com.example.simplebutton.wear.ui.theme.WatchTextSecondary

@Composable
fun WatchPeriodDetailScreen(
    period: TimetablePeriod,
    countdownStatus: PeriodCountdownStatus,
    onDismiss: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        modifier = Modifier.fillMaxSize().background(WatchPureBlack)
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Period and Day Header
            item {
                Text(
                    text = "${period.formattedPeriodTitle.uppercase()} • ${period.dayOfWeek.name.take(3)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WatchCyan,
                    letterSpacing = 1.sp
                )
            }

            // Subject Title (Large, Clear)
            item {
                Text(
                    text = period.subject.ifBlank { "Free Period" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WatchTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }

            // Countdown Status Badge
            item {
                val badgeBg = when (countdownStatus.state) {
                    PeriodState.IN_PROGRESS -> WatchEmerald.copy(alpha = 0.2f)
                    PeriodState.STARTING_NOW -> WatchAmber.copy(alpha = 0.2f)
                    PeriodState.UPCOMING -> WatchCobalt.copy(alpha = 0.25f)
                    PeriodState.PASSED -> WatchTextMuted.copy(alpha = 0.2f)
                }
                val badgeColor = when (countdownStatus.state) {
                    PeriodState.IN_PROGRESS -> WatchEmerald
                    PeriodState.STARTING_NOW -> WatchAmber
                    PeriodState.UPCOMING -> WatchCyan
                    PeriodState.PASSED -> WatchTextSecondary
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = countdownStatus.badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Room & Teacher Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WatchSurfaceCard)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (period.room.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ROOM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WatchCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(WatchCobalt.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = period.room,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchTextPrimary
                                    )
                                }
                            }
                        }

                        if (period.teacher.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TEACHER",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WatchTextMuted
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = period.teacher,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WatchTextPrimary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "TIME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WatchTextMuted
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = period.formattedTime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WatchAmber
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Close / Done button
            item {
                CompactChip(
                    onClick = onDismiss,
                    label = { Text("Close", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchTextPrimary
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
