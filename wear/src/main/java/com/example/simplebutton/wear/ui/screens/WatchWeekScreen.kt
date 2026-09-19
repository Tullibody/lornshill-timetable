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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.example.simplebutton.wear.ui.theme.WatchAmber
import com.example.simplebutton.wear.ui.theme.WatchCobalt
import com.example.simplebutton.wear.ui.theme.WatchCyan
import com.example.simplebutton.wear.ui.theme.WatchPureBlack
import com.example.simplebutton.wear.ui.theme.WatchSurfaceCard
import com.example.simplebutton.wear.ui.theme.WatchTextMuted
import com.example.simplebutton.wear.ui.theme.WatchTextPrimary
import com.example.simplebutton.wear.ui.theme.WatchTextSecondary
import java.time.DayOfWeek

@Composable
fun WatchWeekScreen(
    repository: WatchTimetableRepository,
    onSelectPeriod: (TimetablePeriod) -> Unit,
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val initialDay = when (repository.getEffectiveDay()) {
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> DayOfWeek.MONDAY
        else -> repository.getEffectiveDay()
    }
    var selectedDay by remember { mutableStateOf(initialDay) }

    val daysOfWeek = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY
    )

    val periodsForSelectedDay = repository.getPeriodsForDay(selectedDay)

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

            // Screen Header
            item {
                Text(
                    text = "WEEK SCHEDULE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WatchCyan,
                    letterSpacing = 1.sp
                )
            }

            // Day Selector Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    daysOfWeek.forEach { day ->
                        val isSelected = day == selectedDay
                        val dayLabel = when (day) {
                            DayOfWeek.MONDAY -> "M"
                            DayOfWeek.TUESDAY -> "Tu"
                            DayOfWeek.WEDNESDAY -> "W"
                            DayOfWeek.THURSDAY -> "Th"
                            DayOfWeek.FRIDAY -> "F"
                            else -> ""
                        }

                        CompactChip(
                            onClick = { selectedDay = day },
                            label = {
                                Text(
                                    text = dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else WatchTextSecondary
                                )
                            },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (isSelected) WatchCobalt else WatchSurfaceCard
                            )
                        )
                    }
                }
            }

            // Selected Day Label & Bell Info
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Text(
                        text = selectedDay.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WatchTextPrimary
                    )
                    if (selectedDay == DayOfWeek.WEDNESDAY) {
                        Text(
                            text = "08:55 — 14:40 (30m Lunch)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = WatchAmber
                        )
                    } else if (selectedDay == DayOfWeek.MONDAY) {
                        Text(
                            text = "09:00 — 15:45 (7 Periods)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = WatchTextMuted
                        )
                    } else if (selectedDay == DayOfWeek.FRIDAY) {
                        Text(
                            text = "09:00 — 15:00 (P6 ends 15:00)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = WatchTextMuted
                        )
                    } else {
                        Text(
                            text = "09:00 — 14:55 (6 Periods)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = WatchTextMuted
                        )
                    }
                }
            }

            // Periods List
            items(periodsForSelectedDay) { period ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (period.isAssigned) WatchSurfaceCard else WatchPureBlack.copy(alpha = 0.5f)
                        )
                        .clickable { onSelectPeriod(period) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Period Badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(
                                    if (period.isAssigned) WatchCobalt.copy(alpha = 0.3f) else WatchTextMuted.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "P${period.periodIndex}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (period.isAssigned) WatchCyan else WatchTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Subject & Room
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = period.subject.ifBlank { "Free Period" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (period.isAssigned) WatchTextPrimary else WatchTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (period.room.isNotBlank()) {
                                    Text(
                                        text = period.room,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchCyan
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "${period.formattedStartTime}–${period.formattedEndTime}",
                                    fontSize = 10.sp,
                                    color = WatchTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Back button
            item {
                CompactChip(
                    onClick = onBack,
                    label = { Text("Back to Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
