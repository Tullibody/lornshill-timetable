package com.example.simplebutton.wear.ui.screens

import androidx.compose.foundation.background
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
import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.wear.ui.theme.WatchAmber
import com.example.simplebutton.wear.ui.theme.WatchCobalt
import com.example.simplebutton.wear.ui.theme.WatchCyan
import com.example.simplebutton.wear.ui.theme.WatchEmerald
import com.example.simplebutton.wear.ui.theme.WatchPureBlack
import com.example.simplebutton.wear.ui.theme.WatchSurfaceCard
import com.example.simplebutton.wear.ui.theme.WatchTextMuted
import com.example.simplebutton.wear.ui.theme.WatchTextPrimary
import com.example.simplebutton.wear.ui.theme.WatchTextSecondary
import java.time.DayOfWeek

@Composable
fun WatchBellScheduleScreen(
    onBack: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    var selectedDay by remember { mutableStateOf(DayOfWeek.MONDAY) }

    val periodCount = SchoolSchedule.getPeriodCountForDay(selectedDay)
    val intervalTimes = SchoolSchedule.getIntervalTimes(selectedDay)
    val lunchTimes = SchoolSchedule.getLunchTimes(selectedDay)

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

            item {
                Text(
                    text = "BELL TIMES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WatchCyan,
                    letterSpacing = 1.sp
                )
            }

            // Day Switcher [Mon, Tue/Thu, Wed, Fri]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                    days.forEach { day ->
                        val isSelected = selectedDay == day
                        val label = when (day) {
                            DayOfWeek.MONDAY -> "Mon"
                            DayOfWeek.TUESDAY -> "Tue"
                            DayOfWeek.WEDNESDAY -> "Wed"
                            DayOfWeek.FRIDAY -> "Fri"
                            else -> ""
                        }
                        CompactChip(
                            onClick = { selectedDay = day },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (isSelected) WatchCobalt else WatchSurfaceCard
                            )
                        )
                    }
                }
            }

            // Bell Schedule Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(WatchSurfaceCard)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        (1..periodCount).forEach { pIdx ->
                            val times = SchoolSchedule.getStandardSlotTimes(selectedDay, pIdx)
                            if (times != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Period $pIdx",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WatchTextPrimary
                                    )
                                    Text(
                                        text = String.format("%02d:%02d–%02d:%02d", times.first.hour, times.first.minute, times.second.hour, times.second.minute),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = WatchCyan
                                    )
                                }

                                // Show Interval after P2
                                if (pIdx == 2) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(WatchAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Interval", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WatchAmber)
                                            Text(
                                                text = String.format("%02d:%02d–%02d:%02d", intervalTimes.first.hour, intervalTimes.first.minute, intervalTimes.second.hour, intervalTimes.second.minute),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WatchAmber
                                            )
                                        }
                                    }
                                }

                                // Show Lunch after P4
                                if (pIdx == 4) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(WatchEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Lunch", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WatchEmerald)
                                            Text(
                                                text = String.format("%02d:%02d–%02d:%02d", lunchTimes.first.hour, lunchTimes.first.minute, lunchTimes.second.hour, lunchTimes.second.minute),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WatchEmerald
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            item {
                CompactChip(
                    onClick = onBack,
                    label = { Text("Back", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
