package com.example.simplebutton.wear

import com.example.simplebutton.model.PeriodState
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.UserProfile
import com.example.simplebutton.sync.TimetableSyncContract
import com.example.simplebutton.wear.data.WatchTimetableRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class WatchTimetableRepositoryTest {

    private lateinit var repository: WatchTimetableRepository

    @Before
    fun setUp() {
        repository = WatchTimetableRepository(null)
    }

    @Test
    fun testInitialState() {
        assertEquals(31, repository.periods.size)
        assertFalse(repository.hasTimetable)
    }

    @Test
    fun testLoadS1Preset() {
        repository.loadPreset("S1")
        assertTrue(repository.hasTimetable)
        assertEquals(31, repository.periods.size)

        val monPeriods = repository.getPeriodsForDay(DayOfWeek.MONDAY)
        assertEquals(7, monPeriods.size)
        assertEquals("Physical Education", monPeriods[0].subject)
        assertEquals("Mr Manuel", monPeriods[0].teacher)
        assertEquals("Games Hall", monPeriods[0].room)

        val wedPeriods = repository.getPeriodsForDay(DayOfWeek.WEDNESDAY)
        assertEquals(6, wedPeriods.size)
        assertEquals(LocalTime.of(8, 55), wedPeriods[0].startTime)
        assertEquals(LocalTime.of(9, 45), wedPeriods[0].endTime)
    }

    @Test
    fun testLoadJaydenPreset() {
        repository.loadPreset("Jayden")
        assertTrue(repository.hasTimetable)

        val monPeriods = repository.getPeriodsForDay(DayOfWeek.MONDAY)
        assertEquals(7, monPeriods.size)
        assertEquals("Graphic Comm", monPeriods[0].subject)
        assertEquals("Mr Imlay", monPeriods[0].teacher)
        assertEquals("Graphic Studio", monPeriods[0].room)
    }

    @Test
    fun testCurrentAndNextPeriodDuringClass() {
        repository.loadPreset("S1")

        // Monday 09:20 is during Period 1 (09:00 - 09:55)
        val (curr, next) = repository.getCurrentAndNextPeriod(
            currentTime = LocalTime.of(9, 20),
            currentDay = DayOfWeek.MONDAY
        )
        assertNotNull(curr)
        assertEquals("Physical Education", curr?.subject)
        assertEquals(1, curr?.periodIndex)

        assertNotNull(next)
        assertEquals("Art", next?.subject)
        assertEquals(2, next?.periodIndex)

        // Verify countdown status
        val countdown = repository.getCountdownStatus(curr!!, LocalTime.of(9, 20))
        assertEquals(PeriodState.IN_PROGRESS, countdown.state)
        assertEquals(35L, countdown.minutesDiff)
        assertEquals("35m left", countdown.badgeText)
    }

    @Test
    fun testUpcomingPeriodBeforeSchool() {
        repository.loadPreset("S1")

        // Monday 08:45 is 15 minutes before Period 1 starts
        val (curr, next) = repository.getCurrentAndNextPeriod(
            currentTime = LocalTime.of(8, 45),
            currentDay = DayOfWeek.MONDAY
        )
        assertNull(curr)
        assertNotNull(next)
        assertEquals(1, next?.periodIndex)

        val countdown = repository.getCountdownStatus(next!!, LocalTime.of(8, 45))
        assertEquals(PeriodState.UPCOMING, countdown.state)
        assertEquals(15L, countdown.minutesDiff)
        assertEquals("In 15m", countdown.badgeText)
    }

    @Test
    fun testSaveFromJsonContract() {
        val samplePeriods = listOf(
            TimetablePeriod(
                id = "MONDAY_P1",
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "Computing Science",
                teacher = "Miss MacDonald",
                room = "IT Suite 1",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            )
        )
        val timetableJson = TimetableSyncContract.periodsToJson(samplePeriods)
        val profileJson = TimetableSyncContract.profileToJson(
            UserProfile(name = "Test Student", year = "S4", house = "Grange", className = "4G1")
        )

        repository.saveFromJson(timetableJson, profileJson, 123456789L)

        assertEquals("Test Student", repository.userProfile.value.name)
        assertEquals("S4", repository.userProfile.value.year)
        assertEquals("Grange", repository.userProfile.value.house)
        assertEquals("4G1", repository.userProfile.value.className)
        assertEquals(123456789L, repository.lastSyncTime.value)

        val loadedMon = repository.getPeriodsForDay(DayOfWeek.MONDAY)
        val p1 = loadedMon.firstOrNull { it.periodIndex == 1 }
        assertNotNull(p1)
        assertEquals("Computing Science", p1?.subject)
        assertEquals("IT Suite 1", p1?.room)
    }
}
