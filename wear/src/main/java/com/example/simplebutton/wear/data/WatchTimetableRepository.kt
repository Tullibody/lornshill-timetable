package com.example.simplebutton.wear.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.simplebutton.model.PeriodCountdownStatus
import com.example.simplebutton.model.PeriodState
import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.UserProfile
import com.example.simplebutton.sync.TimetableSyncContract
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class WatchTimetableRepository(context: Context? = null) {

    private val prefs = context?.getSharedPreferences("lornshill_watch_prefs", Context.MODE_PRIVATE)

    // Observable periods list for Compose UI
    val periods = mutableStateListOf<TimetablePeriod>()

    // Simulation overrides for testing / QA
    val simulatedTime = mutableStateOf<LocalTime?>(null)
    val simulatedDay = mutableStateOf<DayOfWeek?>(null)

    // User Profile state
    val userProfile = mutableStateOf(
        UserProfile(
            name = prefs?.getString("student_name", "") ?: "",
            year = prefs?.getString("student_year", "S1") ?: "S1",
            house = prefs?.getString("student_house", "Devon") ?: "Devon",
            className = prefs?.getString("student_class", "") ?: "",
            completedOnboarding = prefs?.getBoolean("completed_onboarding", true) ?: true
        )
    )

    // Last sync timestamp (epoch millis)
    val lastSyncTime = mutableStateOf(prefs?.getLong("last_sync_timestamp", 0L) ?: 0L)

    val hasTimetable: Boolean
        get() = periods.any { it.isAssigned }

    init {
        loadSavedPeriods()
    }

    private fun loadSavedPeriods() {
        val savedJson = prefs?.getString("saved_periods_json", null)
        if (!savedJson.isNullOrBlank()) {
            val loaded = TimetableSyncContract.jsonToPeriods(savedJson)
            if (loaded.isNotEmpty()) {
                periods.clear()
                periods.addAll(loaded.sortedWith(compareBy({ it.dayOfWeek }, { it.periodIndex })))
                return
            }
        }

        // Fallback: check legacy format if any
        val legacyRaw = prefs?.getString("saved_periods_data", null)
        if (!legacyRaw.isNullOrBlank()) {
            val standardSlots = SchoolSchedule.createAllWeeklySlots()
                .associateBy { "${it.dayOfWeek.name}_P${it.periodIndex}" }
                .toMutableMap()
            try {
                legacyRaw.split(";;;").forEach { entry ->
                    if (entry.isNotBlank()) {
                        val parts = entry.split("|||")
                        if (parts.size >= 6) {
                            val day = DayOfWeek.valueOf(parts[1])
                            val pIdx = parts[2].toIntOrNull() ?: 1
                            val key = "${day.name}_P$pIdx"
                            val stdTimes = SchoolSchedule.getStandardSlotTimes(day, pIdx)
                            if (stdTimes != null) {
                                standardSlots[key] = TimetablePeriod(
                                    id = key,
                                    dayOfWeek = day,
                                    periodIndex = pIdx,
                                    subject = parts[3],
                                    teacher = parts.getOrNull(4) ?: "",
                                    room = parts.getOrNull(5) ?: "",
                                    startTime = stdTimes.first,
                                    endTime = stdTimes.second
                                )
                            }
                        }
                    }
                }
                periods.clear()
                periods.addAll(standardSlots.values.sortedWith(compareBy({ it.dayOfWeek }, { it.periodIndex })))
                return
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }

        // Default initial state: empty unassigned slots
        val emptySlots = SchoolSchedule.createAllWeeklySlots()
        periods.clear()
        periods.addAll(emptySlots)
    }

    fun saveFromJson(timetableJson: String, profileJson: String? = null, timestamp: Long = System.currentTimeMillis()) {
        val parsedPeriods = TimetableSyncContract.jsonToPeriods(timetableJson)
        if (parsedPeriods.isNotEmpty()) {
            periods.clear()
            periods.addAll(parsedPeriods.sortedWith(compareBy({ it.dayOfWeek }, { it.periodIndex })))

            prefs?.edit()?.putString("saved_periods_json", timetableJson)?.apply()
        }

        if (!profileJson.isNullOrBlank()) {
            val parsedProfile = TimetableSyncContract.jsonToProfile(profileJson)
            userProfile.value = parsedProfile
            prefs?.edit()?.apply {
                putString("student_name", parsedProfile.name)
                putString("student_year", parsedProfile.year)
                putString("student_house", parsedProfile.house)
                putString("student_class", parsedProfile.className)
                putBoolean("completed_onboarding", parsedProfile.completedOnboarding)
                apply()
            }
        }

        lastSyncTime.value = timestamp
        prefs?.edit()?.putLong("last_sync_timestamp", timestamp)?.apply()
    }

    fun savePeriodsList(list: List<TimetablePeriod>) {
        periods.clear()
        periods.addAll(list)
        val json = TimetableSyncContract.periodsToJson(list)
        prefs?.edit()?.putString("saved_periods_json", json)?.apply()
    }

    fun saveUserProfile(profile: UserProfile) {
        userProfile.value = profile
        prefs?.edit()?.apply {
            putString("student_name", profile.name)
            putString("student_year", profile.year)
            putString("student_house", profile.house)
            putString("student_class", profile.className)
            putBoolean("completed_onboarding", profile.completedOnboarding)
            apply()
        }
    }

    fun clearTimetable() {
        val emptySlots = SchoolSchedule.createAllWeeklySlots()
        periods.clear()
        periods.addAll(emptySlots)
        prefs?.edit()?.remove("saved_periods_json")?.remove("saved_periods_data")?.apply()
    }

    // Standalone preset loaders for offline use
    fun loadPreset(stageYear: String) {
        val preset = when (stageYear) {
            "S1" -> createS1Preset()
            "S2" -> createS2Preset()
            "S3", "S4", "S5", "S6" -> createS3PlusPreset()
            "Jayden" -> createJaydenPreset()
            else -> createS1Preset()
        }
        savePeriodsList(preset)
    }

    fun getEffectiveTime(): LocalTime {
        return simulatedTime.value ?: LocalTime.now()
    }

    fun getEffectiveDay(): DayOfWeek {
        return simulatedDay.value ?: LocalDate.now().dayOfWeek
    }

    fun setSimulatedTime(time: LocalTime?, day: DayOfWeek? = null) {
        simulatedTime.value = time
        simulatedDay.value = day
    }

    fun getPeriodsForDay(day: DayOfWeek): List<TimetablePeriod> {
        val count = SchoolSchedule.getPeriodCountForDay(day)
        val dayPeriods = periods.filter { it.dayOfWeek == day }
        if (dayPeriods.size < count) {
            val standardSlots = SchoolSchedule.createDefaultSlotsForDay(day).toMutableList()
            for (p in dayPeriods) {
                val idx = standardSlots.indexOfFirst { it.periodIndex == p.periodIndex }
                if (idx >= 0) standardSlots[idx] = p
            }
            return standardSlots.sortedBy { it.periodIndex }
        }
        return dayPeriods.sortedBy { it.periodIndex }
    }

    fun getCurrentAndNextPeriod(
        currentTime: LocalTime = getEffectiveTime(),
        currentDay: DayOfWeek = getEffectiveDay()
    ): Pair<TimetablePeriod?, TimetablePeriod?> {
        val todayPeriods = getPeriodsForDay(currentDay).filter { it.isAssigned }
        if (todayPeriods.isEmpty()) return Pair(null, null)

        val current = todayPeriods.firstOrNull {
            !it.startTime.isAfter(currentTime) && currentTime.isBefore(it.endTime)
        }

        val next = if (current != null) {
            todayPeriods.firstOrNull { it.startTime.isAfter(current.startTime) && !it.startTime.isBefore(currentTime) }
        } else {
            todayPeriods.firstOrNull { it.startTime.isAfter(currentTime) }
        }

        return Pair(current, next)
    }

    fun getCountdownStatus(period: TimetablePeriod, currentTime: LocalTime = getEffectiveTime()): PeriodCountdownStatus {
        if (!period.startTime.isAfter(currentTime) && currentTime.isBefore(period.endTime)) {
            val remaining = Duration.between(currentTime, period.endTime).toMinutes().coerceAtLeast(1)
            return PeriodCountdownStatus(
                state = PeriodState.IN_PROGRESS,
                minutesDiff = remaining,
                badgeText = "${remaining}m left"
            )
        }

        if (currentTime.isBefore(period.startTime)) {
            val minutesUntil = Duration.between(currentTime, period.startTime).toMinutes()
            return when {
                minutesUntil <= 0 -> PeriodCountdownStatus(PeriodState.STARTING_NOW, 0, "Now")
                minutesUntil == 1L -> PeriodCountdownStatus(PeriodState.UPCOMING, 1, "In 1m")
                minutesUntil < 60 -> PeriodCountdownStatus(PeriodState.UPCOMING, minutesUntil, "In ${minutesUntil}m")
                else -> {
                    val hours = minutesUntil / 60
                    val mins = minutesUntil % 60
                    val text = if (mins == 0L) "In ${hours}h" else "In ${hours}h ${mins}m"
                    PeriodCountdownStatus(PeriodState.UPCOMING, minutesUntil, text)
                }
            }
        }

        return PeriodCountdownStatus(PeriodState.PASSED, 0, "Done")
    }

    companion object {
        fun createS1Preset(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            )
            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Physical Education", "Mr Manuel", "Games Hall"),
                        Triple("Art", "Mrs Hilson", "Art Studio 1"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("Mathematics", "Mr Blackhall", "Room 10"),
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("French", "Mrs Roy", "Room 4"),
                        Triple("Science", "Mrs Imrie-Paterson", "Lab 1")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("Digital Tech", "Miss MacDonald", "IT Suite 1"),
                        Triple("Mathematics", "Mr Blackhall", "Room 10"),
                        Triple("English", "Miss Tait", "Room 15"),
                        Triple("Home Economics", "Miss Smith", "HE Kitchen 1"),
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("PSE", "Mrs Dixon", "Guidance Base")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("English", "Miss Tait", "Room 15"),
                        Triple("Mathematics", "Mr Blackhall", "Room 10"),
                        Triple("Science", "Mrs Imrie-Paterson", "Lab 1"),
                        Triple("Design & Tech", "Mr Mercer", "Tech Workshop 1"),
                        Triple("Social Subjects", "Mrs O'Donnell", "Room 14"),
                        Triple("Physical Education", "Mr Manuel", "Games Hall")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Mathematics", "Mr Blackhall", "Room 10"),
                        Triple("Music", "C Bennett", "Music Room 1"),
                        Triple("Social Subjects", "Mrs O'Donnell", "Room 14"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("French", "Mrs Roy", "Room 5"),
                        Triple("Digital Tech", "Miss MacDonald", "IT Suite 1")
                    )
                    DayOfWeek.FRIDAY -> listOf(
                        Triple("Creative", "Mrs Watson", "Art Studio 1"),
                        Triple("English", "Miss Tait", "Room 15"),
                        Triple("Science", "Mrs Imrie-Paterson", "Lab 1"),
                        Triple("Social Subjects", "Mrs O'Donnell", "Room 14"),
                        Triple("Mathematics", "Mr Blackhall", "Room 10"),
                        Triple("Physical Education", "Mr Manuel", "Games Hall")
                    )
                    else -> emptyList()
                }
                subjects.forEachIndexed { idx, (sub, teach, rm) ->
                    val times = SchoolSchedule.getStandardSlotTimes(day, idx + 1) ?: return@forEachIndexed
                    list.add(
                        TimetablePeriod(
                            id = "${day.name}_P${idx + 1}",
                            dayOfWeek = day,
                            periodIndex = idx + 1,
                            subject = sub,
                            teacher = teach,
                            room = rm,
                            startTime = times.first,
                            endTime = times.second
                        )
                    )
                }
            }
            return list
        }

        fun createS2Preset(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Digital Tech", "Ms McCulloch", "IT Suite 1"),
                        Triple("Home Economics", "Miss Francis", "HE Kitchen 1"),
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Physical Education", "Mr Harvey", "Games Hall"),
                        Triple("Design & Tech", "Mr Steel", "Tech Workshop 1")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("Science", "Mrs Whitecross", "Lab 1"),
                        Triple("Design & Tech", "Mr Steel", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Social Subjects", "Mr Simpson", "Room 18"),
                        Triple("French", "Mrs Roy", "Room 5")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("PSE", "Mr Rennie", "Guidance Base"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Social Subjects", "Mr Simpson", "Room 18"),
                        Triple("Art", "Mrs Hilson", "Art Studio 1"),
                        Triple("Science", "Mrs Whitecross", "Lab 1")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("Music", "C Bennett", "Music Room 1"),
                        Triple("Digital Tech", "Ms McCulloch", "IT Suite 1"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Physical Education", "Mr Hillis", "Games Hall")
                    )
                    DayOfWeek.FRIDAY -> listOf(
                        Triple("Science", "Mr Thomas", "Lab 2"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("French", "Mrs Roy", "Room 4"),
                        Triple("Social Subjects", "Mr Simpson", "Room 18"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Physical Education", "Mr Hillis", "Games Hall")
                    )
                    else -> emptyList()
                }
                subjects.forEachIndexed { idx, (sub, teach, rm) ->
                    val times = SchoolSchedule.getStandardSlotTimes(day, idx + 1) ?: return@forEachIndexed
                    list.add(
                        TimetablePeriod(
                            id = "${day.name}_P${idx + 1}",
                            dayOfWeek = day,
                            periodIndex = idx + 1,
                            subject = sub,
                            teacher = teach,
                            room = rm,
                            startTime = times.first,
                            endTime = times.second
                        )
                    )
                }
            }
            return list
        }

        fun createS3PlusPreset(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Mathematics", "A Wright", "Room 10"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("Biology", "Mrs Imrie-Paterson", "Lab 1"),
                        Triple("Design & Manufacture", "Mr Steel", "Tech Workshop 1"),
                        Triple("History", "D Scruton", "Room 14"),
                        Triple("Core P.E", "Mr Manuel", "Games Hall"),
                        Triple("Art & Design", "Mrs Hilson", "Art Studio 1")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("Chemistry", "R Richards", "Lab 2"),
                        Triple("Computing Science", "Miss MacDonald", "IT Suite 1"),
                        Triple("Mathematics", "A Wright", "Room 10"),
                        Triple("Modern Studies", "Mrs Lawrence", "Room 8"),
                        Triple("Spanish", "Mrs Roy", "Room 5")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("Graphic Comm", "Mr Mercer", "Graphic Studio"),
                        Triple("Mathematics", "A Wright", "Room 10"),
                        Triple("Physics", "Z Thomas", "Lab 3"),
                        Triple("Criminology", "Mr Simpson", "Room 18"),
                        Triple("Core P.E", "Mr Harvey", "Games Hall"),
                        Triple("Practical Cookery", "Miss Francis", "HE Kitchen 1")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Biology", "Mrs Imrie-Paterson", "Lab 1"),
                        Triple("Music", "C Bennett", "Music Room 1"),
                        Triple("Geography", "G Lawrence", "Room 8"),
                        Triple("Mathematics", "A Wright", "Room 10"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("Cyber Security", "Ms McCulloch", "IT Suite 2")
                    )
                    DayOfWeek.FRIDAY -> listOf(
                        Triple("French", "Mrs Roy", "Room 4"),
                        Triple("PSE", "Mr Rennie", "Guidance Base"),
                        Triple("Mathematics", "A Wright", "Room 10"),
                        Triple("Chemistry", "R Richards", "Lab 2"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("Team Sports", "Mr Hillis", "Games Hall")
                    )
                    else -> emptyList()
                }
                subjects.forEachIndexed { idx, (sub, teach, rm) ->
                    val times = SchoolSchedule.getStandardSlotTimes(day, idx + 1) ?: return@forEachIndexed
                    list.add(
                        TimetablePeriod(
                            id = "${day.name}_P${idx + 1}",
                            dayOfWeek = day,
                            periodIndex = idx + 1,
                            subject = sub,
                            teacher = teach,
                            room = rm,
                            startTime = times.first,
                            endTime = times.second
                        )
                    )
                }
            }
            return list
        }

        fun createJaydenPreset(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Graphic Comm", "Mr Imlay", "Graphic Studio"),
                        Triple("Practical Woodwork", "Mr Watson", "Tech Workshop 1"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Core P.E", "Mr Green", "Games Hall"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Geography", "Mrs Lawrence", "Room 8")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("Admin & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Geography", "Mrs Lawrence", "Room 8"),
                        Triple("Graphic Comm", "Mr Imlay", "Graphic Studio"),
                        Triple("Practical Woodwork", "Mr Watson", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("Admin & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Admin & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("PSE", "Mrs Dixon", "Guidance Base"),
                        Triple("Geography", "Mr Ross", "Room 8"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Admin & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Graphic Comm", "Mr Imlay", "Graphic Studio"),
                        Triple("Practical Woodwork", "Mr Watson", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15")
                    )
                    DayOfWeek.FRIDAY -> listOf(
                        Triple("Geography", "Mrs Lawrence", "Room 8"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Practical Woodwork", "Mr Watson", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Core P.E", "Mr Hillis", "Games Hall"),
                        Triple("Graphic Comm", "Mr Imlay", "Graphic Studio")
                    )
                    else -> emptyList()
                }
                subjects.forEachIndexed { idx, (sub, teach, rm) ->
                    val times = SchoolSchedule.getStandardSlotTimes(day, idx + 1) ?: return@forEachIndexed
                    list.add(
                        TimetablePeriod(
                            id = "${day.name}_P${idx + 1}",
                            dayOfWeek = day,
                            periodIndex = idx + 1,
                            subject = sub,
                            teacher = teach,
                            room = rm,
                            startTime = times.first,
                            endTime = times.second
                        )
                    )
                }
            }
            return list
        }
    }
}
