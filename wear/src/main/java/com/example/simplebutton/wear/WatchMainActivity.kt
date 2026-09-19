package com.example.simplebutton.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.example.simplebutton.wear.ui.screens.WatchBellScheduleScreen
import com.example.simplebutton.wear.ui.screens.WatchHomeScreen
import com.example.simplebutton.wear.ui.screens.WatchPeriodDetailScreen
import com.example.simplebutton.wear.ui.screens.WatchSettingsScreen
import com.example.simplebutton.wear.ui.screens.WatchWeekScreen
import com.example.simplebutton.wear.ui.theme.LornshillWatchTheme

enum class WatchScreen {
    HOME,
    WEEK,
    BELL_TIMES,
    SETTINGS,
    PERIOD_DETAIL
}

class WatchMainActivity : ComponentActivity() {

    private lateinit var repository: WatchTimetableRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = WatchTimetableRepository(applicationContext)

        setContent {
            LornshillWatchTheme {
                WatchApp(repository = repository)
            }
        }
    }
}

@Composable
fun WatchApp(repository: WatchTimetableRepository) {
    var currentScreen by remember { mutableStateOf(WatchScreen.HOME) }
    var selectedPeriod by remember { mutableStateOf<TimetablePeriod?>(null) }

    when (currentScreen) {
        WatchScreen.HOME -> WatchHomeScreen(
            repository = repository,
            onOpenPeriodDetail = { period ->
                selectedPeriod = period
                currentScreen = WatchScreen.PERIOD_DETAIL
            },
            onOpenWeekView = { currentScreen = WatchScreen.WEEK },
            onOpenBellTimes = { currentScreen = WatchScreen.BELL_TIMES },
            onOpenSettings = { currentScreen = WatchScreen.SETTINGS }
        )

        WatchScreen.WEEK -> WatchWeekScreen(
            repository = repository,
            onSelectPeriod = { period ->
                selectedPeriod = period
                currentScreen = WatchScreen.PERIOD_DETAIL
            },
            onBack = { currentScreen = WatchScreen.HOME }
        )

        WatchScreen.BELL_TIMES -> WatchBellScheduleScreen(
            onBack = { currentScreen = WatchScreen.HOME }
        )

        WatchScreen.SETTINGS -> WatchSettingsScreen(
            repository = repository,
            onBack = { currentScreen = WatchScreen.HOME }
        )

        WatchScreen.PERIOD_DETAIL -> {
            val period = selectedPeriod
            if (period != null) {
                val countdown = repository.getCountdownStatus(period)
                WatchPeriodDetailScreen(
                    period = period,
                    countdownStatus = countdown,
                    onDismiss = {
                        selectedPeriod = null
                        currentScreen = WatchScreen.HOME
                    }
                )
            } else {
                currentScreen = WatchScreen.HOME
            }
        }
    }
}
