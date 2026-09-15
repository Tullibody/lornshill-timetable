package com.example.simplebutton.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationManagerCompat
import com.example.simplebutton.notifications.TimetableNotificationScheduler
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class TimetableRepository(context: Context? = null) {

    private val appContext = context?.applicationContext
    private val prefs = context?.getSharedPreferences("lornshill_timetable_prefs", Context.MODE_PRIVATE)

    // Observable periods list
    val periods = mutableStateListOf<TimetablePeriod>()

    // Simulation overrides for testing
    val simulatedTime = mutableStateOf<LocalTime?>(null)
    val simulatedDay = mutableStateOf<DayOfWeek?>(null)

    // User Profile state
    val userProfile = mutableStateOf(
        UserProfile(
            name = prefs?.getString("student_name", "") ?: "",
            year = prefs?.getString("student_year", "S1") ?: "S1",
            house = prefs?.getString("student_house", "Devon") ?: "Devon",
            className = prefs?.getString("student_class", "") ?: "",
            completedOnboarding = prefs?.getBoolean("completed_onboarding", false) ?: false
        )
    )

    // Notification Settings
    val notificationsEnabled = mutableStateOf(prefs?.getBoolean("notifications_enabled", true) ?: true)

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled.value = enabled
        prefs?.edit()?.putBoolean("notifications_enabled", enabled)?.apply()
    }

    // Daily Brief Settings
    val dailyBriefEnabled = mutableStateOf(prefs?.getBoolean("daily_brief_enabled", true) ?: true)
    val dailyBriefStartHour = mutableStateOf(prefs?.getInt("daily_brief_start_hour", 7) ?: 7)
    val dailyBriefStartMin = mutableStateOf(prefs?.getInt("daily_brief_start_min", 30) ?: 30)
    val dailyBriefEndHour = mutableStateOf(prefs?.getInt("daily_brief_end_hour", 8) ?: 8)
    val dailyBriefEndMin = mutableStateOf(prefs?.getInt("daily_brief_end_min", 55) ?: 55)
    val forceDailyBriefPreview = mutableStateOf(false)

    // Theme Mode: "system", "light", "dark"
    val themeMode = mutableStateOf(prefs?.getString("theme_mode", "system") ?: "system")

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        prefs?.edit()?.putString("theme_mode", mode)?.apply()
    }

    val hasTimetable: Boolean
        get() = periods.any { it.isAssigned }

    init {
        loadSavedPeriods()
        FacultyDatabase.init(context)
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

    fun resetOnboarding() {
        val current = userProfile.value
        saveUserProfile(current.copy(completedOnboarding = false))
    }

    fun deleteAllData(context: Context? = null) {
        val ctx = context ?: appContext
        if (ctx != null) {
            try {
                TimetableNotificationScheduler.cancelAll(ctx, this)
                NotificationManagerCompat.from(ctx).cancelAll()
            } catch (_: Exception) {
                // Ignore if notification manager or permission issue
            }
            FacultyDatabase.resetToDefaults(ctx)
        }

        // Wipe all SharedPreferences for timetable and student profile
        prefs?.edit()?.clear()?.apply()

        // Reset user profile to blank initial state (onboarding incomplete)
        userProfile.value = UserProfile(
            name = "",
            year = "S1",
            house = "Devon",
            className = "",
            completedOnboarding = false
        )

        // Clear timetable back to empty unassigned standard slots
        val clearedSlots = SchoolSchedule.createAllWeeklySlots()
        periods.clear()
        periods.addAll(clearedSlots)

        // Reset Daily Brief settings to defaults
        dailyBriefEnabled.value = true
        notificationsEnabled.value = true
        dailyBriefStartHour.value = 7
        dailyBriefStartMin.value = 30
        dailyBriefEndHour.value = 8
        dailyBriefEndMin.value = 55
        forceDailyBriefPreview.value = false

        // Reset Theme mode to system
        themeMode.value = "system"

        // Reset Simulation overrides
        simulatedTime.value = null
        simulatedDay.value = null
    }

    fun updateDailyBriefSettings(enabled: Boolean, sHour: Int, sMin: Int, eHour: Int, eMin: Int) {
        dailyBriefEnabled.value = enabled
        dailyBriefStartHour.value = sHour
        dailyBriefStartMin.value = sMin
        dailyBriefEndHour.value = eHour
        dailyBriefEndMin.value = eMin

        prefs?.edit()?.apply {
            putBoolean("daily_brief_enabled", enabled)
            putInt("daily_brief_start_hour", sHour)
            putInt("daily_brief_start_min", sMin)
            putInt("daily_brief_end_hour", eHour)
            putInt("daily_brief_end_min", eMin)
            apply()
        }
    }

    fun getDailyBriefStartTime(): LocalTime {
        return LocalTime.of(dailyBriefStartHour.value, dailyBriefStartMin.value)
    }

    fun getDailyBriefEndTime(): LocalTime {
        return LocalTime.of(dailyBriefEndHour.value, dailyBriefEndMin.value)
    }

    fun isDailyBriefActive(): Boolean {
        if (forceDailyBriefPreview.value) return true
        if (!dailyBriefEnabled.value) return false
        val curr = getEffectiveTime()
        return !curr.isBefore(getDailyBriefStartTime()) && curr.isBefore(getDailyBriefEndTime())
    }

    private fun loadSavedPeriods() {
        val standardSlots = SchoolSchedule.createAllWeeklySlots()
            .associateBy { "${it.dayOfWeek.name}_P${it.periodIndex}" }
            .toMutableMap()

        val savedRaw = prefs?.getString("saved_periods_data", null)
        val hasSavedData = !savedRaw.isNullOrBlank()

        if (hasSavedData) {
            try {
                savedRaw!!.split(";;;").forEach { entry ->
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
            } catch (_: Exception) {
                // Ignore parse errors, fallback to standard slots
            }
        } else {
            // First run: Never use a preset timetable. Keep all slots unassigned.
            // standardSlots already initialized with blank slots via createAllWeeklySlots()
        }

        periods.clear()
        periods.addAll(standardSlots.values.sortedWith(compareBy({ it.dayOfWeek }, { it.periodIndex })))
    }

    private fun persistPeriods() {
        val raw = periods.joinToString(";;;") { p ->
            "${p.id}|||${p.dayOfWeek.name}|||${p.periodIndex}|||${p.subject}|||${p.teacher}|||${p.room}|||${p.startTime}|||${p.endTime}|||${p.isBreakOrLunch}"
        }
        prefs?.edit()?.putString("saved_periods_data", raw)?.apply()
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

    fun resetSimulation() {
        simulatedTime.value = null
        simulatedDay.value = null
    }

    fun clearTimetable() {
        // Reset all 31 standard periods to blank unassigned slots (preserving fixed time slots)
        val clearedSlots = SchoolSchedule.createAllWeeklySlots()
        periods.clear()
        periods.addAll(clearedSlots)
        prefs?.edit()?.putBoolean("has_loaded_timetable", false)?.remove("saved_periods_data")?.apply()
    }

    fun loadPresetTimetable(savePref: Boolean = true, stageYear: String? = null) {
        val targetStage = stageYear ?: userProfile.value.year
        val presetList = when (FacultyDatabase.getStageCategory(targetStage)) {
            "S1" -> createS1PresetTimetable()
            "S2" -> createS2PresetTimetable()
            else -> createS3PlusPresetTimetable()
        }
        periods.clear()
        periods.addAll(presetList)
        persistPeriods()
        if (savePref) {
            prefs?.edit()?.putBoolean("has_loaded_timetable", true)?.apply()
        }
    }

    fun loadS1Preset(savePref: Boolean = true) = loadPresetTimetable(savePref, "S1")
    fun loadS2Preset(savePref: Boolean = true) = loadPresetTimetable(savePref, "S2")
    fun loadS3PlusPreset(savePref: Boolean = true) = loadPresetTimetable(savePref, "S3+")

    fun loadJaydenPreset(savePref: Boolean = true) {
        val presetList = createJaydenPresetTimetable()
        periods.clear()
        periods.addAll(presetList)
        persistPeriods()
        saveUserProfile(
            UserProfile(
                name = "Jayden Martin Tully",
                year = "S3",
                house = "Ochil",
                className = "3chil",
                completedOnboarding = true
            )
        )
        if (savePref) {
            prefs?.edit()?.putBoolean("has_loaded_timetable", true)?.apply()
        }
    }

    /**
     * Looks up any teacher already assigned to the given subject anywhere else in the timetable.
     * Searches exact matches first, then normalized/abbreviated matches (e.g. Admin <-> Administration).
     */
    fun findDefaultTeacherForSubject(subjectName: String, excludePeriodId: String? = null): String? {
        val clean = subjectName.trim()
        if (clean.isBlank()) return null

        val searchPeriods = periods.filter {
            it.isAssigned &&
            it.teacher.isNotBlank() &&
            (excludePeriodId == null || it.id != excludePeriodId)
        }

        // 1. Exact case-insensitive match
        val exactMatch = searchPeriods.firstOrNull {
            it.subject.trim().equals(clean, ignoreCase = true)
        }
        if (exactMatch != null) return exactMatch.teacher

        // Helper to normalize subject strings for fuzzy/abbreviation matching
        fun normalize(s: String): String = s.lowercase()
            .replace("&", "and")
            .replace(".", "")
            .replace("-", "")
            .replace(" ", "")

        val normTarget = normalize(clean)
        if (normTarget.isBlank()) return null

        // 2. Normalized match or common abbreviations
        val normalizedMatch = searchPeriods.firstOrNull {
            val normExisting = normalize(it.subject)
            normExisting == normTarget ||
            (normTarget.length >= 3 && normExisting.startsWith(normTarget)) ||
            (normExisting.length >= 3 && normTarget.startsWith(normExisting)) ||
            (normTarget.contains("admin") && normExisting.contains("admin")) ||
            (normTarget.contains("math") && normExisting.contains("math")) ||
            (normTarget.contains("graphic") && normExisting.contains("graphic")) ||
            (normTarget.contains("woodwork") && normExisting.contains("woodwork")) ||
            (normTarget.contains("physic") && normExisting.contains("physic")) ||
            (normTarget.contains("geograph") && normExisting.contains("geograph")) ||
            (normTarget.contains("english") && normExisting.contains("english")) ||
            ((normTarget == "pe" || normTarget == "corepe") && (normExisting == "pe" || normExisting == "corepe" || normExisting.contains("physical"))) ||
            ((normTarget == "pse" || normTarget.contains("personalsocial")) && (normExisting == "pse" || normExisting.contains("personalsocial")))
        }

        return normalizedMatch?.teacher
    }

    fun addOrUpdatePeriod(updatedPeriod: TimetablePeriod) {
        val index = periods.indexOfFirst {
            it.id == updatedPeriod.id || (it.dayOfWeek == updatedPeriod.dayOfWeek && it.periodIndex == updatedPeriod.periodIndex)
        }
        val stdTimes = SchoolSchedule.getStandardSlotTimes(updatedPeriod.dayOfWeek, updatedPeriod.periodIndex)
            ?: (updatedPeriod.startTime to updatedPeriod.endTime)

        val standardized = updatedPeriod.copy(
            id = "${updatedPeriod.dayOfWeek.name}_P${updatedPeriod.periodIndex}",
            startTime = stdTimes.first,
            endTime = stdTimes.second
        )

        if (index >= 0) {
            periods[index] = standardized
        } else {
            periods.add(standardized)
        }
        persistPeriods()
    }

    fun deletePeriod(periodId: String) {
        // In standardized locked mode, clearing a period sets its subject/teacher/room empty
        val index = periods.indexOfFirst { it.id == periodId }
        if (index >= 0) {
            val old = periods[index]
            periods[index] = old.copy(subject = "", teacher = "", room = "")
            persistPeriods()
        }
    }

    fun getPeriodsForDay(day: DayOfWeek): List<TimetablePeriod> {
        val count = SchoolSchedule.getPeriodCountForDay(day)
        val dayPeriods = periods.filter { it.dayOfWeek == day }

        // If for any reason periods are missing for this day, ensure all slots exist
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

        // Check for period in progress
        val current = todayPeriods.firstOrNull {
            !it.startTime.isAfter(currentTime) && currentTime.isBefore(it.endTime)
        }

        // Find next period
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
                badgeText = "In progress · ${remaining}m left"
            )
        }

        if (currentTime.isBefore(period.startTime)) {
            val minutesUntil = Duration.between(currentTime, period.startTime).toMinutes()
            return when {
                minutesUntil <= 0 -> PeriodCountdownStatus(PeriodState.STARTING_NOW, 0, "Starting now")
                minutesUntil == 1L -> PeriodCountdownStatus(PeriodState.UPCOMING, 1, "Starts in 1 minute")
                minutesUntil < 60 -> PeriodCountdownStatus(PeriodState.UPCOMING, minutesUntil, "Starts in $minutesUntil minutes")
                else -> {
                    val hours = minutesUntil / 60
                    val mins = minutesUntil % 60
                    val text = if (mins == 0L) "Starts in ${hours}h" else "Starts in ${hours}h ${mins}m"
                    PeriodCountdownStatus(PeriodState.UPCOMING, minutesUntil, text)
                }
            }
        }

        return PeriodCountdownStatus(PeriodState.PASSED, 0, "Completed")
    }

    companion object {
        fun createRealisticPresetTimetable(): List<TimetablePeriod> = createS1PresetTimetable()

        fun createS1PresetTimetable(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )

            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Physical Education", "Mr Manuel", "Games Hall"),
                        Triple("Art", "Mrs Hilson", "Art Studio 1"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("Mathematics", "Mr. Blackhall", "Room 10"),
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("French", "Mrs Roy", "Room 4"),
                        Triple("Science", "Mrs Imrie-Paterson", "Lab 1")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("Digital Technologies", "Miss MacDonald", "IT Suite 1"),
                        Triple("Mathematics", "Mr. Blackhall", "Room 10"),
                        Triple("English", "Miss Tait", "Room 15"),
                        Triple("Home Economics", "Miss Smith", "HE Kitchen 1"),
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("Personal & Social Education", "Mrs Dixon", "Guidance Base")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("English", "Miss Tait", "Room 15"),
                        Triple("Mathematics", "Mr. Blackhall", "Room 10"),
                        Triple("Science", "Mrs Imrie-Paterson", "Lab 1"),
                        Triple("Design & Technology", "Mr Mercer", "Tech Workshop 1"),
                        Triple("Social Subjects", "Mrs O'Donnell", "Room 14"),
                        Triple("Physical Education", "Mr Manuel", "Games Hall")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Mathematics", "Mr. Blackhall", "Room 10"),
                        Triple("Music", "C Bennett", "Music Room 1"),
                        Triple("Social Subjects", "Mrs O'Donnell", "Room 14"),
                        Triple("English", "Miss Risk", "Room 15"),
                        Triple("French", "Mrs Roy", "Room 5"),
                        Triple("Digital Technologies", "Miss MacDonald", "IT Suite 1")
                    )
                    DayOfWeek.FRIDAY -> listOf(
                        Triple("Creative", "Mrs Watson", "Art Studio 1"),
                        Triple("English", "Miss Tait", "Room 15"),
                        Triple("Science", "Mrs Imrie-Paterson", "Lab 1"),
                        Triple("Social Subjects", "Mrs O'Donnell", "Room 14"),
                        Triple("Mathematics", "Mr. Blackhall", "Room 10"),
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

        fun createS2PresetTimetable(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )

            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Digital Technologies", "Ms McCulloch", "IT Suite 1"),
                        Triple("Home Economics", "Miss Francis", "HE Kitchen 1"),
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Physical Education", "Mr Harvey", "Games Hall"),
                        Triple("Design & Technology", "Mr Steel", "Tech Workshop 1")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("Science", "Mrs Whitecross", "Lab 1"),
                        Triple("Design & Technology", "Mr Steel", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Social Subjects", "Mr Simpson", "Room 18"),
                        Triple("French", "Mrs Roy", "Room 5")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Personal & Social Education", "Mr Rennie", "Guidance Base"),
                        Triple("Mathematics", "Mr Edgar", "Room 10"),
                        Triple("Social Subjects", "Mr Simpson", "Room 18"),
                        Triple("Art", "Mrs Hilson", "Art Studio 1"),
                        Triple("Science", "Mrs Whitecross", "Lab 1")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Social Subjects", "Mrs Lawrence", "Room 8"),
                        Triple("Music", "C Bennett", "Music Room 1"),
                        Triple("Digital Technologies", "Ms McCulloch", "IT Suite 1"),
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

        fun createS3PlusPresetTimetable(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )

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
                        Triple("Graphic Communication", "Mr Mercer", "Graphic Studio"),
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
                        Triple("Personal & Social Education", "Mr Rennie", "Guidance Base"),
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

        fun createJaydenPresetTimetable(): List<TimetablePeriod> {
            val list = mutableListOf<TimetablePeriod>()
            val days = listOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
            )

            for (day in days) {
                val subjects = when (day) {
                    DayOfWeek.MONDAY -> listOf(
                        Triple("Graphic Communication", "Mr Imlay", "Graphic Studio"),
                        Triple("Practical Woodworking", "Mr Watson", "Tech Workshop 1"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Core P.E", "Mr. Green", "Games Hall"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Geography", "Mrs Lawrence", "Room 8")
                    )
                    DayOfWeek.TUESDAY -> listOf(
                        Triple("Administration & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Geography", "Mrs Lawrence", "Room 8"),
                        Triple("Graphic Communication", "Mr Imlay", "Graphic Studio"),
                        Triple("Practical Woodworking", "Mr Watson", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15")
                    )
                    DayOfWeek.WEDNESDAY -> listOf(
                        Triple("Administration & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Administration & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Personal & Social Education", "Mrs Dixon", "Guidance Base"),
                        Triple("Geography", "Mr Ross", "Room 8"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1")
                    )
                    DayOfWeek.THURSDAY -> listOf(
                        Triple("Administration & IT", "Ms McCulloch", "IT Suite 1"),
                        Triple("Physics", "Mr Munro-Faure", "Lab 1"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Graphic Communication", "Mr Imlay", "Graphic Studio"),
                        Triple("Practical Woodworking", "Mr Watson", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15")
                    )
                    DayOfWeek.FRIDAY -> listOf(
                        Triple("Geography", "Mrs Lawrence", "Room 8"),
                        Triple("Mathematics", "Mrs Young", "Room 10"),
                        Triple("Practical Woodworking", "Mr Watson", "Tech Workshop 1"),
                        Triple("English", "Mr Ryder", "Room 15"),
                        Triple("Core P.E", "Mr Hillis", "Games Hall"),
                        Triple("Graphics", "Mr Imlay", "Graphic Studio")
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
