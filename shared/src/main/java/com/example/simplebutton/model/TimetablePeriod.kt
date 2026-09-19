package com.example.simplebutton.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

data class TimetablePeriod(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: DayOfWeek,
    val periodIndex: Int = 1,
    val subject: String,
    val teacher: String = "",
    val room: String = "",
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isBreakOrLunch: Boolean = false
) {
    val isAssigned: Boolean
        get() = subject.isNotBlank() &&
            !subject.equals("Free Period", ignoreCase = true) &&
            !subject.equals("Study Period", ignoreCase = true)

    val formattedPeriodTitle: String
        get() = if (periodIndex > 0) "Period $periodIndex" else subject

    val formattedTime: String
        get() = String.format("%02d:%02d — %02d:%02d", startTime.hour, startTime.minute, endTime.hour, endTime.minute)

    val formattedStartTime: String
        get() = String.format("%02d:%02d", startTime.hour, startTime.minute)

    val formattedEndTime: String
        get() = String.format("%02d:%02d", endTime.hour, endTime.minute)
}

enum class PeriodState {
    IN_PROGRESS,
    STARTING_NOW,
    UPCOMING,
    PASSED
}

data class PeriodCountdownStatus(
    val state: PeriodState,
    val minutesDiff: Long,
    val badgeText: String
)

/**
 * Official Lornshill Academy standardized bell schedule.
 * - Monday: 7 periods (09:00 - 15:45)
 * - Tuesday & Thursday: 6 periods (09:00 - 14:55)
 * - Wednesday: 6 periods (08:55 - 14:40) with shifted morning & 30-minute lunch
 * - Friday: 6 periods (09:00 - 15:00) with period 6 ending at 15:00
 */
object SchoolSchedule {

    fun getPeriodCountForDay(day: DayOfWeek): Int = when (day) {
        DayOfWeek.MONDAY -> 7
        DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY -> 6
        else -> 0
    }

    fun getStandardSlotTimes(day: DayOfWeek, periodIndex: Int): Pair<LocalTime, LocalTime>? {
        return when (day) {
            DayOfWeek.MONDAY -> when (periodIndex) {
                1 -> LocalTime.of(9, 0) to LocalTime.of(9, 55)
                2 -> LocalTime.of(9, 55) to LocalTime.of(10, 45)
                3 -> LocalTime.of(11, 0) to LocalTime.of(11, 50)
                4 -> LocalTime.of(11, 50) to LocalTime.of(12, 40)
                5 -> LocalTime.of(13, 15) to LocalTime.of(14, 5)
                6 -> LocalTime.of(14, 5) to LocalTime.of(14, 55)
                7 -> LocalTime.of(14, 55) to LocalTime.of(15, 45)
                else -> null
            }
            DayOfWeek.WEDNESDAY -> when (periodIndex) {
                1 -> LocalTime.of(8, 55) to LocalTime.of(9, 45)
                2 -> LocalTime.of(9, 45) to LocalTime.of(10, 35)
                3 -> LocalTime.of(10, 50) to LocalTime.of(11, 40)
                4 -> LocalTime.of(11, 40) to LocalTime.of(12, 30)
                5 -> LocalTime.of(13, 0) to LocalTime.of(13, 50)
                6 -> LocalTime.of(13, 50) to LocalTime.of(14, 40)
                else -> null
            }
            DayOfWeek.FRIDAY -> when (periodIndex) {
                1 -> LocalTime.of(9, 0) to LocalTime.of(9, 55)
                2 -> LocalTime.of(9, 55) to LocalTime.of(10, 45)
                3 -> LocalTime.of(11, 0) to LocalTime.of(11, 50)
                4 -> LocalTime.of(11, 50) to LocalTime.of(12, 40)
                5 -> LocalTime.of(13, 15) to LocalTime.of(14, 5)
                6 -> LocalTime.of(14, 5) to LocalTime.of(15, 0) // Friday ends at 15:00
                else -> null
            }
            DayOfWeek.TUESDAY, DayOfWeek.THURSDAY -> when (periodIndex) {
                1 -> LocalTime.of(9, 0) to LocalTime.of(9, 55)
                2 -> LocalTime.of(9, 55) to LocalTime.of(10, 45)
                3 -> LocalTime.of(11, 0) to LocalTime.of(11, 50)
                4 -> LocalTime.of(11, 50) to LocalTime.of(12, 40)
                5 -> LocalTime.of(13, 15) to LocalTime.of(14, 5)
                6 -> LocalTime.of(14, 5) to LocalTime.of(14, 55)
                else -> null
            }
            else -> null
        }
    }

    fun getIntervalTimes(day: DayOfWeek): Pair<LocalTime, LocalTime> {
        return if (day == DayOfWeek.WEDNESDAY) {
            LocalTime.of(10, 35) to LocalTime.of(10, 50)
        } else {
            LocalTime.of(10, 45) to LocalTime.of(11, 0)
        }
    }

    fun getLunchTimes(day: DayOfWeek): Pair<LocalTime, LocalTime> {
        return if (day == DayOfWeek.WEDNESDAY) {
            LocalTime.of(12, 30) to LocalTime.of(13, 0)
        } else {
            LocalTime.of(12, 40) to LocalTime.of(13, 15)
        }
    }

    fun createDefaultSlotsForDay(day: DayOfWeek): List<TimetablePeriod> {
        val count = getPeriodCountForDay(day)
        return (1..count).mapNotNull { pIdx ->
            val times = getStandardSlotTimes(day, pIdx) ?: return@mapNotNull null
            TimetablePeriod(
                id = "${day.name}_P$pIdx",
                dayOfWeek = day,
                periodIndex = pIdx,
                subject = "",
                teacher = "",
                room = "",
                startTime = times.first,
                endTime = times.second
            )
        }
    }

    fun createAllWeeklySlots(): List<TimetablePeriod> {
        val days = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
        return days.flatMap { createDefaultSlotsForDay(it) }
    }
}
