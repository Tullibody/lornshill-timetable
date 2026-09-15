package com.example.simplebutton.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.simplebutton.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.DailyBriefManager
import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.ui.components.BreakRowItem
import com.example.simplebutton.ui.components.DailyBriefCard
import com.example.simplebutton.ui.components.FloatingNextPeriodBar
import com.example.simplebutton.ui.components.NextPeriodCard
import com.example.simplebutton.ui.components.PeriodRowItem
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillCanvasBg
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillHeroGradient
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    repository: TimetableRepository,
    onViewFullTimetable: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditPeriod: (TimetablePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val effectiveTime = repository.getEffectiveTime()
    val effectiveDay = repository.getEffectiveDay()
    val (currentPeriod, nextPeriod) = repository.getCurrentAndNextPeriod(effectiveTime, effectiveDay)
    val countdownStatus = (currentPeriod ?: nextPeriod)?.let {
        repository.getCountdownStatus(it, effectiveTime)
    }

    val todayPeriods = repository.getPeriodsForDay(effectiveDay)
    val intervalTimes = SchoolSchedule.getIntervalTimes(effectiveDay)
    val lunchTimes = SchoolSchedule.getLunchTimes(effectiveDay)
    val intervalStr = String.format("%02d:%02d — %02d:%02d", intervalTimes.first.hour, intervalTimes.first.minute, intervalTimes.second.hour, intervalTimes.second.minute)
    val lunchStr = String.format("%02d:%02d — %02d:%02d", lunchTimes.first.hour, lunchTimes.first.minute, lunchTimes.second.hour, lunchTimes.second.minute)

    val dateDisplay = try {
        val now = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
        now.format(formatter)
    } catch (e: Exception) {
        "Friday, 11 September"
    }

    val userProfile = repository.userProfile.value
    val greetingTitle = if (userProfile.name.isNotBlank()) {
        "HELLO, ${userProfile.name.uppercase()}"
    } else {
        "LORNSHILL TIMETABLE"
    }

    val subtitle = if (userProfile.formattedBadge.isNotBlank()) {
        "${userProfile.formattedBadge} · $dateDisplay"
    } else {
        dateDisplay
    }

    val briefSummary = remember(todayPeriods) {
        DailyBriefManager.generateBrief(todayPeriods)
    }

    var briefDismissed by remember { mutableStateOf(false) }
    val isBriefActive = repository.isDailyBriefActive()

    // Scroll state & scroll-away detection for floating bottom bar
    val listState = rememberLazyListState()
    var isFloatingBarVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -8f) {
                    // Scrolling down: hide floating bar
                    isFloatingBarVisible = false
                } else if (delta > 8f) {
                    // Scrolling up: reveal floating bar
                    isFloatingBarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Auto-reveal floating bar when resting at the top
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 20) {
            isFloatingBarVisible = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBg)
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Hero Gradient Header extending seamlessly into Android Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillHeroGradient)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_lornshill_logo),
                            contentDescription = "Lornshill Academy Logo",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = greetingTitle,
                            fontFamily = Staatliches,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = subtitle,
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Scrollable Body Content
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Morning Daily Brief Card (7:30 - 8:55 AM or when force previewed)
            if (isBriefActive && !briefDismissed) {
                item {
                    DailyBriefCard(
                        summary = briefSummary,
                        onDismiss = {
                            briefDismissed = true
                            repository.forceDailyBriefPreview.value = false
                        }
                    )
                }
            }

            // Centrepiece Next Period / NOW Card
            if (todayPeriods.any { it.isAssigned }) {
                item {
                    NextPeriodCard(
                        currentPeriod = currentPeriod,
                        nextPeriod = nextPeriod,
                        countdownStatus = countdownStatus
                    )
                }
            }

            // Today's Schedule Section Title
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TODAY'S SCHEDULE",
                            fontFamily = Staatliches,
                            fontSize = 17.sp,
                            color = colors.textPrimary,
                            letterSpacing = 0.8.sp
                        )
                        if (!isBriefActive || briefDismissed) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (colors.isDark) Color(0xFF78350F) else Color(0xFFFEF3C7))
                                    .clickable {
                                        briefDismissed = false
                                        repository.forceDailyBriefPreview.value = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🌅 Brief",
                                    fontFamily = Lexend,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (colors.isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = onViewFullTimetable
                    ) {
                        Text(
                            text = "View full →",
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentCobalt
                        )
                    }
                }
            }

            if (todayPeriods.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "WEEKEND / NO CLASSES TODAY",
                                fontFamily = Staatliches,
                                fontSize = 16.sp,
                                color = colors.textPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'View full' to check your upcoming Monday schedule.",
                                fontFamily = Lexend,
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(todayPeriods, key = { it.id }) { period ->
                    val isActive = currentPeriod?.id == period.id
                    val isNext = nextPeriod?.id == period.id && !isActive

                    PeriodRowItem(
                        period = period,
                        isActive = isActive,
                        isNext = isNext,
                        onClick = { onEditPeriod(period) }
                    )

                    // Render Interval separator after Period 2 (non-editable)
                    if (period.periodIndex == 2) {
                        Spacer(modifier = Modifier.height(6.dp))
                        BreakRowItem(
                            title = "Interval",
                            timeText = intervalStr,
                            emoji = "☕"
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Render Lunch separator after Period 4 (non-editable)
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
            }

            item {
                // Generous bottom spacer so floating bar does not occlude the last period
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }

    // Floating Next Period Bar at the bottom (hides on scroll down, opens full timetable on tap)
    AnimatedVisibility(
        visible = isFloatingBarVisible,
        enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        FloatingNextPeriodBar(
            currentPeriod = currentPeriod,
            nextPeriod = nextPeriod,
            countdownStatus = countdownStatus,
            onTap = onViewFullTimetable
        )
    }
}
}

