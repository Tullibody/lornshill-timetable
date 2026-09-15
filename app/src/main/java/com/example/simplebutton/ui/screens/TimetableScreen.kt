package com.example.simplebutton.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.ui.components.BreakRowItem
import com.example.simplebutton.ui.components.DaySelector
import com.example.simplebutton.ui.components.PeriodRowItem
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillCanvasBg
import com.example.simplebutton.ui.theme.LornshillHeaderGradient
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors
import java.time.DayOfWeek

@Composable
fun TimetableScreen(
    repository: TimetableRepository,
    onEditPeriod: (TimetablePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val today = repository.getEffectiveDay()
    val initialDay = if (today in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) DayOfWeek.MONDAY else today
    var selectedDay by remember { mutableStateOf(initialDay) }

    val periodsForDay = repository.getPeriodsForDay(selectedDay)
    val effectiveTime = repository.getEffectiveTime()
    val (currentPeriod, nextPeriod) = repository.getCurrentAndNextPeriod(effectiveTime, today)

    val intervalTimes = SchoolSchedule.getIntervalTimes(selectedDay)
    val lunchTimes = SchoolSchedule.getLunchTimes(selectedDay)

    val intervalStr = String.format("%02d:%02d — %02d:%02d", intervalTimes.first.hour, intervalTimes.first.minute, intervalTimes.second.hour, intervalTimes.second.minute)
    val lunchStr = String.format("%02d:%02d — %02d:%02d", lunchTimes.first.hour, lunchTimes.first.minute, lunchTimes.second.hour, lunchTimes.second.minute)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBg)
    ) {
        // Gradient Header extending into Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillHeaderGradient)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FULL TIMETABLE",
                        fontFamily = Staatliches,
                        fontSize = 20.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Standardized Schedule · Mon – Fri",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = Color(0xFFBAE6FD)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (selectedDay == DayOfWeek.MONDAY) "7 Periods" else "6 Periods",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                DaySelector(
                    selectedDay = selectedDay,
                    today = today,
                    onDaySelected = { selectedDay = it }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(periodsForDay, key = { it.id }) { period ->
                val isToday = selectedDay == today
                val isActive = isToday && currentPeriod?.id == period.id
                val isNext = isToday && nextPeriod?.id == period.id && !isActive

                PeriodRowItem(
                    period = period,
                    isActive = isActive,
                    isNext = isNext,
                    onClick = { onEditPeriod(period) }
                )

                // Render Interval separator after Period 2
                if (period.periodIndex == 2) {
                    Spacer(modifier = Modifier.height(6.dp))
                    BreakRowItem(
                        title = "Interval",
                        timeText = intervalStr,
                        emoji = "☕"
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Render Lunch separator after Period 4
                if (period.periodIndex == 4) {
                    Spacer(modifier = Modifier.height(6.dp))
                    BreakRowItem(
                        title = "Lunch",
                        timeText = lunchStr,
                        emoji = "🥪"
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
