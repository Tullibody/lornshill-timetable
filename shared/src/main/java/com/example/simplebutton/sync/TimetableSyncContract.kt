package com.example.simplebutton.sync

import com.example.simplebutton.model.SchoolSchedule
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime

object TimetableSyncContract {
    const val PATH_TIMETABLE_DATA = "/lornshill/timetable"
    const val PATH_REQUEST_SYNC = "/lornshill/request_sync"
    const val PATH_OPEN_PHONE_APP = "/lornshill/open_phone_app"

    const val KEY_TIMETABLE_JSON = "timetable_json"
    const val KEY_PROFILE_JSON = "profile_json"
    const val KEY_STUDENT_NAME = "student_name"
    const val KEY_STUDENT_YEAR = "student_year"
    const val KEY_STUDENT_HOUSE = "student_house"
    const val KEY_STUDENT_CLASS = "student_class"
    const val KEY_TIMESTAMP = "timestamp"
    const val KEY_ACTION = "action"

    fun periodsToJson(periods: List<TimetablePeriod>): String {
        val array = JSONArray()
        for (period in periods) {
            val obj = JSONObject()
            obj.put("id", period.id)
            obj.put("dayOfWeek", period.dayOfWeek.name)
            obj.put("periodIndex", period.periodIndex)
            obj.put("subject", period.subject)
            obj.put("teacher", period.teacher)
            obj.put("room", period.room)
            obj.put("startTime", period.startTime.toString())
            obj.put("endTime", period.endTime.toString())
            obj.put("isBreakOrLunch", period.isBreakOrLunch)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToPeriods(jsonString: String): List<TimetablePeriod> {
        if (jsonString.isBlank()) return emptyList()
        val result = mutableListOf<TimetablePeriod>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val day = DayOfWeek.valueOf(obj.getString("dayOfWeek"))
                val periodIndex = obj.getInt("periodIndex")
                val stdTimes = SchoolSchedule.getStandardSlotTimes(day, periodIndex)

                val start = if (obj.has("startTime")) {
                    LocalTime.parse(obj.getString("startTime"))
                } else stdTimes?.first ?: LocalTime.of(9, 0)

                val end = if (obj.has("endTime")) {
                    LocalTime.parse(obj.getString("endTime"))
                } else stdTimes?.second ?: LocalTime.of(9, 55)

                result.add(
                    TimetablePeriod(
                        id = obj.optString("id", "${day.name}_P$periodIndex"),
                        dayOfWeek = day,
                        periodIndex = periodIndex,
                        subject = obj.optString("subject", ""),
                        teacher = obj.optString("teacher", ""),
                        room = obj.optString("room", ""),
                        startTime = start,
                        endTime = end,
                        isBreakOrLunch = obj.optBoolean("isBreakOrLunch", false)
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return result
    }

    fun profileToJson(profile: UserProfile): String {
        val obj = JSONObject()
        obj.put("name", profile.name)
        obj.put("year", profile.year)
        obj.put("house", profile.house)
        obj.put("className", profile.className)
        obj.put("completedOnboarding", profile.completedOnboarding)
        return obj.toString()
    }

    fun jsonToProfile(jsonString: String): UserProfile {
        if (jsonString.isBlank()) return UserProfile()
        return try {
            val obj = JSONObject(jsonString)
            UserProfile(
                name = obj.optString("name", ""),
                year = obj.optString("year", "S1"),
                house = obj.optString("house", "Devon"),
                className = obj.optString("className", ""),
                completedOnboarding = obj.optBoolean("completedOnboarding", false)
            )
        } catch (_: Exception) {
            UserProfile()
        }
    }
}
