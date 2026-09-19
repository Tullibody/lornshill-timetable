package com.example.simplebutton.wear.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.simplebutton.model.PeriodState
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.example.simplebutton.wear.ui.theme.WatchAmber
import com.example.simplebutton.wear.ui.theme.WatchCobalt
import com.example.simplebutton.wear.ui.theme.WatchCobaltDark
import com.example.simplebutton.wear.ui.theme.WatchCyan
import com.example.simplebutton.wear.ui.theme.WatchEmerald
import com.example.simplebutton.wear.ui.theme.WatchPureBlack
import com.example.simplebutton.wear.ui.theme.WatchSurfaceCard
import com.example.simplebutton.wear.ui.theme.WatchTextMuted
import com.example.simplebutton.wear.ui.theme.WatchTextPrimary
import com.example.simplebutton.wear.ui.theme.WatchTextSecondary
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WatchHomeScreen(
    repository: WatchTimetableRepository,
    onOpenPeriodDetail: (TimetablePeriod) -> Unit,
    onOpenWeekView: () -> Unit,
    onOpenBellTimes: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val listState = rememberScalingLazyListState()

    // 5-second ticker to keep countdowns accurate
    var ticker by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            ticker++
        }
    }

    val effectiveTime = repository.getEffectiveTime()
    val effectiveDay = repository.getEffectiveDay()
    val (currentPeriod, nextPeriod) = repository.getCurrentAndNextPeriod(effectiveTime, effectiveDay)
    val todayPeriods = repository.getPeriodsForDay(effectiveDay)

    val isWeekend = effectiveDay == DayOfWeek.SATURDAY || effectiveDay == DayOfWeek.SUNDAY
    val dayFormatter = DateTimeFormatter.ofPattern("EEE d MMM")
    val formattedDate = LocalDate.now().format(dayFormatter)

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        modifier = Modifier.fillMaxSize().background(WatchPureBlack)
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top Header: Date & Lornshill Brand
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "LORNSHILL ACADEMY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WatchCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isWeekend) "Weekend" else formattedDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = WatchTextSecondary
                    )
                }
            }

            // HERO CARD: Current or Next Period
            if (currentPeriod != null) {
                val countdown = repository.getCountdownStatus(currentPeriod, effectiveTime)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(WatchCobaltDark)
                            .clickable { onOpenPeriodDetail(currentPeriod) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .height(8.dp)
                                            .width(8.dp)
                                            .background(WatchEmerald, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentPeriod.formattedPeriodTitle.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchCyan
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(WatchEmerald.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = countdown.badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchEmerald
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = currentPeriod.subject,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WatchTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (currentPeriod.room.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .background(WatchPureBlack.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = currentPeriod.room,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                if (currentPeriod.teacher.isNotBlank()) {
                                    Text(
                                        text = currentPeriod.teacher,
                                        fontSize = 10.sp,
                                        color = WatchTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (nextPeriod != null) {
                val countdown = repository.getCountdownStatus(nextPeriod, effectiveTime)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(WatchSurfaceCard)
                            .clickable { onOpenPeriodDetail(nextPeriod) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NEXT: ${nextPeriod.formattedPeriodTitle.uppercase()}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WatchCyan
                                )
                                Box(
                                    modifier = Modifier
                                        .background(WatchCobalt.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = countdown.badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchCyan
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = nextPeriod.subject,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WatchTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (nextPeriod.room.isNotBlank()) {
                                    Text(
                                        text = nextPeriod.room,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchCyan
                                    )
                                }
                                Text(
                                    text = nextPeriod.formattedStartTime,
                                    fontSize = 10.sp,
                                    color = WatchTextMuted
                                )
                            }
                        }
                    }
                }
            } else if (!repository.hasTimetable) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(WatchSurfaceCard)
                            .clickable { onOpenSettings() }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No Timetable Loaded",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WatchTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap to sync with phone or select preset",
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                color = WatchAmber
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(WatchSurfaceCard)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isWeekend) "Enjoy your weekend!" else "Classes finished for today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WatchTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Check Week Timetable below",
                                fontSize = 10.sp,
                                color = WatchTextSecondary
                            )
                        }
                    }
                }
            }

            // Today's Schedule Section Title
            if (!isWeekend && todayPeriods.any { it.isAssigned }) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "TODAY'S SCHEDULE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WatchTextSecondary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }

                // Today's Periods List
                items(todayPeriods) { period ->
                    val countdown = repository.getCountdownStatus(period, effectiveTime)
                    val isCurrent = countdown.state == PeriodState.IN_PROGRESS
                    val isPassed = countdown.state == PeriodState.PASSED

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isCurrent -> WatchCobalt.copy(alpha = 0.35f)
                                    period.isAssigned -> WatchSurfaceCard
                                    else -> WatchPureBlack.copy(alpha = 0.4f)
                                }
                            )
                            .clickable { onOpenPeriodDetail(period) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .background(
                                        when {
                                            isCurrent -> WatchEmerald.copy(alpha = 0.3f)
                                            isPassed -> WatchTextMuted.copy(alpha = 0.2f)
                                            else -> WatchCobalt.copy(alpha = 0.25f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "P${period.periodIndex}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isCurrent -> WatchEmerald
                                        isPassed -> WatchTextMuted
                                        else -> WatchCyan
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = period.subject.ifBlank { "Free Period" },
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPassed) WatchTextMuted else WatchTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (period.room.isNotBlank()) {
                                        Text(
                                            text = period.room,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPassed) WatchTextMuted else WatchCyan
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "${period.formattedStartTime}–${period.formattedEndTime}",
                                        fontSize = 9.sp,
                                        color = WatchTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Navigation Chips
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Chip(
                    onClick = onOpenWeekView,
                    label = { Text("Week Timetable", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    secondaryLabel = { Text("Mon — Fri schedule", fontSize = 9.sp, color = WatchTextSecondary) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }

            item {
                Chip(
                    onClick = onOpenBellTimes,
                    label = { Text("Bell Times & Lunch", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    secondaryLabel = { Text("Interval & lunch schedule", fontSize = 9.sp, color = WatchTextSecondary) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }

            item {
                Chip(
                    onClick = onOpenSettings,
                    label = { Text("Sync & Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    secondaryLabel = { Text("Phone sync & presets", fontSize = 9.sp, color = WatchTextSecondary) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
