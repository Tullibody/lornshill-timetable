package com.example.simplebutton

import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.UserProfile
import com.example.simplebutton.sync.TimetableSyncContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime

class TimetableSyncContractTest {

    @Test
    fun testPeriodsJsonSerializationAndDeserialization() {
        val originalPeriods = listOf(
            TimetablePeriod(
                id = "MONDAY_P1",
                dayOfWeek = DayOfWeek.MONDAY,
                periodIndex = 1,
                subject = "Mathematics",
                teacher = "Mrs Young",
                room = "Room 10",
                startTime = LocalTime.of(9, 0),
                endTime = LocalTime.of(9, 55)
            ),
            TimetablePeriod(
                id = "WEDNESDAY_P3",
                dayOfWeek = DayOfWeek.WEDNESDAY,
                periodIndex = 3,
                subject = "Physics",
                teacher = "Mr Munro-Faure",
                room = "Lab 1",
                startTime = LocalTime.of(10, 50),
                endTime = LocalTime.of(11, 40)
            )
        )

        val json = TimetableSyncContract.periodsToJson(originalPeriods)
        assertNotNull(json)
        assertTrue(json.contains("Mathematics"))
        assertTrue(json.contains("Mrs Young"))
        assertTrue(json.contains("Physics"))

        val restored = TimetableSyncContract.jsonToPeriods(json)
        assertEquals(2, restored.size)
        assertEquals(originalPeriods[0].id, restored[0].id)
        assertEquals(originalPeriods[0].subject, restored[0].subject)
        assertEquals(originalPeriods[0].teacher, restored[0].teacher)
        assertEquals(originalPeriods[0].room, restored[0].room)
        assertEquals(originalPeriods[0].startTime, restored[0].startTime)
        assertEquals(originalPeriods[0].endTime, restored[0].endTime)

        assertEquals(originalPeriods[1].id, restored[1].id)
        assertEquals(originalPeriods[1].subject, restored[1].subject)
    }

    @Test
    fun testProfileJsonSerializationAndDeserialization() {
        val profile = UserProfile(
            name = "Jayden Martin Tully",
            year = "S3",
            house = "Ochil",
            className = "3chil",
            completedOnboarding = true
        )

        val json = TimetableSyncContract.profileToJson(profile)
        val restored = TimetableSyncContract.jsonToProfile(json)

        assertEquals("Jayden Martin Tully", restored.name)
        assertEquals("S3", restored.year)
        assertEquals("Ochil", restored.house)
        assertEquals("3chil", restored.className)
        assertTrue(restored.completedOnboarding)
    }

    @Test
    fun testSchoolScheduleSlots() {
        assertEquals(7, SchoolSchedule.getPeriodCountForDay(DayOfWeek.MONDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.TUESDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.WEDNESDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.THURSDAY))
        assertEquals(6, SchoolSchedule.getPeriodCountForDay(DayOfWeek.FRIDAY))

        val allSlots = SchoolSchedule.createAllWeeklySlots()
        assertEquals(31, allSlots.size)
    }
}
