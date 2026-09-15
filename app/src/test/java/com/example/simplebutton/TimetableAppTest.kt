package com.example.simplebutton

import com.example.simplebutton.model.DailyBriefManager
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.GoogleSheetsFacultyReader
import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.notifications.AppLifecycleTracker
import com.example.simplebutton.notifications.TimetableNotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class TimetableAppTest {

    // ==========================================
    // 1. SchoolSchedule Tests (31 Periods)
    // ==========================================

    @Test
    fun testTotalPeriodsInWeeklySchedule() {
        val slots = SchoolSchedule.createAllWeeklySlots()
        // Monday (7) + Tue (6) + Wed (6) + Thu (6) + Fri (6) = 31 periods
        assertEquals(31, slots.size)
    }

    @Test
    fun testDayPeriodCounts() {
        assertEquals(7, SchoolSchedule.getPeriodCountForDay(DayOfWeek.MONDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.TUESDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.WEDNESDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.THURSDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.FRIDAY))
        assertEquals(0, SchoolSchedule.getPeriodCountForDay(DayOfWeek.SATURDAY))
        assertEquals(0, SchoolSchedule.getPeriodCountForDay(DayOfWeek.SUNDAY))
    }

    @Test
    fun testWednesdayScheduleTimings() {
        // Wednesday starts at 08:55
        val p1 = SchoolSchedule.getStandardSlotTimes(DayOfWeek.WEDNESDAY, 1)
        assertNotNull(p1)
        assertEquals(LocalTime.of(8, 55), p1!!.first)
        assertEquals(LocalTime.of(9, 45), p1.second)

        // Wednesday Interval: 10:35 - 10:50
        val interval = SchoolSchedule.getIntervalTimes(DayOfWeek.WEDNESDAY)
        assertEquals(LocalTime.of(10, 35), interval.first)
        assertEquals(LocalTime.of(10, 50), interval.second)

        // Wednesday Lunch: 12:30 - 13:00 (30 minutes)
        val lunch = SchoolSchedule.getLunchTimes(DayOfWeek.WEDNESDAY)
        assertEquals(LocalTime.of(12, 30), lunch.first)
        assertEquals(LocalTime.of(13, 0), lunch.second)

        // Wednesday Period 6 ends at 14:40
        val p6 = SchoolSchedule.getStandardSlotTimes(DayOfWeek.WEDNESDAY, 6)
        assertNotNull(p6)
        assertEquals(LocalTime.of(13, 50), p6!!.first)
        assertEquals(LocalTime.of(14, 40), p6.second)
    }

    @Test
    fun testStandardDayScheduleTimings() {
        // Tuesday starts at 09:00
        val p1 = SchoolSchedule.getStandardSlotTimes(DayOfWeek.TUESDAY, 1)
        assertNotNull(p1)
        assertEquals(LocalTime.of(9, 0), p1!!.first)
        assertEquals(LocalTime.of(9, 55), p1.second)

        // Tuesday Interval: 10:45 - 11:00
        val interval = SchoolSchedule.getIntervalTimes(DayOfWeek.TUESDAY)
        assertEquals(LocalTime.of(10, 45), interval.first)
        assertEquals(LocalTime.of(11, 0), interval.second)

        // Tuesday Lunch: 12:40 - 13:15
        val lunch = SchoolSchedule.getLunchTimes(DayOfWeek.TUESDAY)
        assertEquals(LocalTime.of(12, 40), lunch.first)
        assertEquals(LocalTime.of(13, 15), lunch.second)

        // Tuesday ends at 14:55
        val p6 = SchoolSchedule.getStandardSlotTimes(DayOfWeek.TUESDAY, 6)
        assertNotNull(p6)
        assertEquals(LocalTime.of(14, 5), p6!!.first)
        assertEquals(LocalTime.of(14, 55), p6.second)
    }

    // ==========================================
    // 2. DailyBriefManager Tests
    // ==========================================

    @Test
    fun testEmptyTimetableProducesNoLessonsBrief() {
        // Unassigned slots or Study Period
        val emptySlots = listOf(
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            ),
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 2,
                subject = "Study Period",
                startTime = LocalTime.of(9, 55),
                endTime = LocalTime.of(10, 45)
            )
        )
        val brief = DailyBriefManager.generateBrief(emptySlots)
        assertFalse(brief.hasLessons)
        assertEquals(0, brief.totalLessonsCount)
        assertTrue(brief.headline.contains("No classes scheduled today"))
        assertTrue(brief.peDetail.contains("No P.E. today"))
    }

    @Test
    fun testPEDetectionExcludesPSE() {
        val pseLesson = listOf(
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "Personal & Social Education",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            )
        )
        val brief = DailyBriefManager.generateBrief(pseLesson)
        assertTrue(brief.hasLessons)
        assertFalse("PSE should not trigger P.E. kit reminder", brief.needsPeKit)
        assertTrue(brief.peDetail.contains("No P.E. today"))
    }

    @Test
    fun testPEDetectionMatchesActualPE() {
        val peLessons = listOf("Physical Education", "P.E", "PE", "Games", "Swimming")
        for (peSubject in peLessons) {
            val lessons = listOf(
                TimetablePeriod(
                    dayOfWeek = DayOfWeek.MONDAY,
                    periodIndex = 1,
                    subject = peSubject,
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(9, 55)
                )
            )
            val brief = DailyBriefManager.generateBrief(lessons)
            assertTrue("Expected needsPeKit to be true for subject '$peSubject'", brief.needsPeKit)
            assertTrue(brief.peDetail.contains("Bring your P.E. kit"))
        }
    }

    @Test
    fun testConsecutiveDoublePeriodDetection() {
        val lessons = listOf(
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "English",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            ),
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 2,
                subject = "English",
                startTime = LocalTime.of(9, 55),
                endTime = LocalTime.of(10, 45)
            ),
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 3,
                subject = "Mathematics",
                startTime = LocalTime.of(11, 0),
                endTime = LocalTime.of(11, 50)
            )
        )
        val brief = DailyBriefManager.generateBrief(lessons)
        assertEquals(1, brief.doubles.size)
        assertEquals("Double English (P1 & P2)", brief.doubles.first())
        assertEquals(3, brief.totalLessonsCount)
        assertTrue(brief.headline.contains("Double English (P1 & P2)"))
    }

    @Test
    fun testNonConsecutiveSameSubjectIsNotDoublePeriod() {
        val lessons = listOf(
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "English",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            ),
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 2,
                subject = "Science",
                startTime = LocalTime.of(9, 55),
                endTime = LocalTime.of(10, 45)
            ),
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 3,
                subject = "English",
                startTime = LocalTime.of(11, 0),
                endTime = LocalTime.of(11, 50)
            )
        )
        val brief = DailyBriefManager.generateBrief(lessons)
        assertTrue("Non-consecutive periods should not be marked as a double", brief.doubles.isEmpty())
    }

    @Test
    fun testLessonCountPluralization() {
        val singleLesson = listOf(
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "Art",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            )
        )
        val brief = DailyBriefManager.generateBrief(singleLesson)
        assertEquals(1, brief.totalLessonsCount)
        assertTrue(brief.bulletPoints.any { it.contains("1 lesson total") })

        val multiLessons = listOf(
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "Art",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            ),
            TimetablePeriod(
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 2,
                subject = "Music",
                startTime = LocalTime.of(9, 55),
                endTime = LocalTime.of(10, 45)
            )
        )
        val briefMulti = DailyBriefManager.generateBrief(multiLessons)
        assertEquals(2, briefMulti.totalLessonsCount)
        assertTrue(briefMulti.bulletPoints.any { it.contains("2 lessons total") })
    }

    // ==========================================
    // 3. GoogleSheetsFacultyReader Tests
    // ==========================================

    @Test
    fun testNormalizeToCsvUrlStandard() {
        val input = "https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/edit?usp=sharing"
        val normalized = GoogleSheetsFacultyReader.normalizeToCsvUrl(input)
        assertEquals("https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/export?format=csv", normalized)
    }

    @Test
    fun testNormalizeToCsvUrlWithGid() {
        val input = "https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/edit#gid=123456789"
        val normalized = GoogleSheetsFacultyReader.normalizeToCsvUrl(input)
        assertEquals("https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/export?format=csv&gid=123456789", normalized)
    }

    @Test
    fun testNormalizeToCsvUrlPubHtml() {
        val input = "https://docs.google.com/spreadsheets/d/e/2PACX-1vT_SamplePubId1234567890/pubhtml"
        val normalized = GoogleSheetsFacultyReader.normalizeToCsvUrl(input)
        assertEquals("https://docs.google.com/spreadsheets/d/e/2PACX-1vT_SamplePubId1234567890/pub?output=csv", normalized)
    }

    @Test
    fun testNormalizeToCsvUrlRawId() {
        val input = "1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I"
        val normalized = GoogleSheetsFacultyReader.normalizeToCsvUrl(input)
        assertEquals("https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/export?format=csv", normalized)
    }

    @Test
    fun testParseCsvSampleContent() {
        val csv = """,Teacher Name,Department
,C Watson,Creative Arts
,C Milne,Creative Arts
,S Steel,Design & Technology
,G Nisbet,Maths
,L Labus,Science"""

        val faculties = GoogleSheetsFacultyReader.parseCsv(csv)
        assertEquals(4, faculties.size)

        val creativeArts = faculties.find { it.name.contains("Creative Arts", ignoreCase = true) }
        assertNotNull(creativeArts)
        assertEquals(2, creativeArts!!.teachers.size)
        assertTrue(creativeArts.teachers.contains("C Watson"))
        assertTrue(creativeArts.teachers.contains("C Milne"))
    }

    // ==========================================
    // 4. FacultyDatabase Tests
    // ==========================================

    @Test
    fun testStageCategorisation() {
        assertEquals("S1", FacultyDatabase.getStageCategory("S1"))
        assertEquals("S1", FacultyDatabase.getStageCategory("s1"))
        assertEquals("S2", FacultyDatabase.getStageCategory("S2"))
        assertEquals("S3+", FacultyDatabase.getStageCategory("S3"))
        assertEquals("S3+", FacultyDatabase.getStageCategory("S4"))
        assertEquals("S3+", FacultyDatabase.getStageCategory("S5"))
        assertEquals("S3+", FacultyDatabase.getStageCategory("S6"))
    }

    @Test
    fun testFacultySubjectLookup() {
        val mathsFaculty = FacultyDatabase.getFacultyForSubject("Mathematics")
        assertNotNull(mathsFaculty)
        assertTrue(mathsFaculty!!.name.contains("Math", ignoreCase = true))

        val peFaculty = FacultyDatabase.getFacultyForSubject("Physical Education")
        assertNotNull(peFaculty)
        assertTrue(peFaculty!!.name.contains("P.E", ignoreCase = true) || peFaculty.name.contains("PE", ignoreCase = true))

        val room = FacultyDatabase.suggestRoomForSubject("Mathematics")
        assertTrue(room.isNotBlank())
    }

    // ==========================================
    // 5. Notification Lifecycle & Scheduling Tests
    // ==========================================

    @Test
    fun testCalculateNextOccurrenceBeforePeriodStartsToday() {
        // Today is Monday at 08:30. Lesson is Monday at 09:00.
        val nowMonday = LocalDateTime.of(2026, 9, 14, 8, 30) // 2026-09-14 is Monday
        val (eventTime, alertTime) = TimetableNotificationScheduler.calculateNextOccurrence(
            targetDay = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(9, 0),
            now = nowMonday,
            leadMinutes = 5
        )

        // Event should be today (Monday) at 09:00, alert at 08:55
        assertEquals(DayOfWeek.MONDAY, eventTime.dayOfWeek)
        assertEquals(LocalTime.of(9, 0), eventTime.toLocalTime())
        assertEquals(LocalTime.of(8, 55), alertTime.toLocalTime())
        assertTrue("Alert must be strictly after now", alertTime.isAfter(nowMonday))
    }

    @Test
    fun testCalculateNextOccurrenceAfterAlertTimeElapsedToday() {
        // Today is Monday at 08:56. Lesson is Monday at 09:00 (5m alert was at 08:55).
        // Since 08:55 has already elapsed, it MUST schedule for NEXT Monday.
        val nowMonday = LocalDateTime.of(2026, 9, 14, 8, 56)
        val (eventTime, alertTime) = TimetableNotificationScheduler.calculateNextOccurrence(
            targetDay = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(9, 0),
            now = nowMonday,
            leadMinutes = 5
        )

        assertEquals(DayOfWeek.MONDAY, eventTime.dayOfWeek)
        assertEquals(21, eventTime.dayOfMonth) // Next Monday is Sept 21
        assertEquals(LocalTime.of(9, 0), eventTime.toLocalTime())
        assertEquals(LocalTime.of(8, 55), alertTime.toLocalTime())
        assertTrue("Alert must be strictly after now (next week)", alertTime.isAfter(nowMonday))
    }

    @Test
    fun testCalculateNextOccurrenceExactlyAtAlertTimeAdvancesToNextWeek() {
        // Exactly at 08:55:00.000. Alert should never schedule for the past/now, so it advances to next week.
        val nowMonday = LocalDateTime.of(2026, 9, 14, 8, 55, 0)
        val (eventTime, alertTime) = TimetableNotificationScheduler.calculateNextOccurrence(
            targetDay = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(9, 0),
            now = nowMonday,
            leadMinutes = 5
        )

        assertEquals(21, eventTime.dayOfMonth) // Next week
        assertTrue("Alert must be strictly after now", alertTime.isAfter(nowMonday))
    }

    @Test
    fun testDailyBriefOccurrenceStrictlyInFuture() {
        // Today is Monday at 07:31. Brief is at 07:30. Must advance to next week.
        val nowMonday = LocalDateTime.of(2026, 9, 14, 7, 31)
        val (_, briefTime) = TimetableNotificationScheduler.calculateNextOccurrence(
            targetDay = DayOfWeek.MONDAY,
            targetTime = LocalTime.of(7, 30),
            now = nowMonday,
            leadMinutes = 0
        )

        assertEquals(21, briefTime.dayOfMonth)
        assertTrue(briefTime.isAfter(nowMonday))
    }

    @Test
    fun testAppLifecycleTrackerState() {
        AppLifecycleTracker.isAppInForeground = false
        assertFalse(AppLifecycleTracker.isAppInForeground)

        AppLifecycleTracker.isAppInForeground = true
        assertTrue(AppLifecycleTracker.isAppInForeground)

        AppLifecycleTracker.isAppInForeground = false
        assertFalse(AppLifecycleTracker.isAppInForeground)
    }
}

