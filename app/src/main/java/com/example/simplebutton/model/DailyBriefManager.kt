package com.example.simplebutton.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class DailyBriefSummary(
    val hasLessons: Boolean,
    val needsPeKit: Boolean,
    val peDetail: String,
    val hasMaths: Boolean,
    val mathsDetail: String?,
    val hasEnglish: Boolean,
    val englishDetail: String?,
    val doubles: List<String>,
    val totalLessonsCount: Int,
    val dayStartTime: LocalTime?,
    val dayEndTime: LocalTime?,
    val headline: String,
    val bulletPoints: List<String>
)

object DailyBriefManager {

    fun isInBriefWindow(
        currentTime: LocalTime,
        startTime: LocalTime = LocalTime.of(7, 30),
        endTime: LocalTime = LocalTime.of(8, 55),
        enabled: Boolean = true
    ): Boolean {
        if (!enabled) return false
        return !currentTime.isBefore(startTime) && currentTime.isBefore(endTime)
    }

    fun generateBrief(todayPeriods: List<TimetablePeriod>): DailyBriefSummary {
        val lessons = todayPeriods.filter { it.isAssigned && !it.isBreakOrLunch }.sortedBy { it.startTime }

        if (lessons.isEmpty()) {
            return DailyBriefSummary(
                hasLessons = false,
                needsPeKit = false,
                peDetail = "No P.E. today — regular school uniform.",
                hasMaths = false,
                mathsDetail = null,
                hasEnglish = false,
                englishDetail = null,
                doubles = emptyList(),
                totalLessonsCount = 0,
                dayStartTime = null,
                dayEndTime = null,
                headline = "No classes scheduled today! Enjoy your day off.",
                bulletPoints = listOf("No scheduled periods for today.", "Standard school uniform if attending campus.")
            )
        }

        // 1. P.E. Kit Check (uses exact tokens and keywords to avoid matching 'Personal & Social Education')
        val peKeywords = listOf("p.e", "physical education", "games", "swimming", "athletics", "games hall", "team sports", "sports", "fitness", "gym", "dance")
        val peLessons = lessons.filter {
            val sub = it.subject.lowercase()
            peKeywords.any { kw -> sub.contains(kw) } ||
                sub.split(Regex("[^a-zA-Z0-9]+")).any { word -> word == "pe" }
        }
        val needsPe = peLessons.isNotEmpty()
        val peDetail = if (needsPe) {
            val periodNames = peLessons.map { "P${it.periodIndex}" }.joinToString(", ")
            "Bring your P.E. kit ($periodNames)"
        } else {
            "No P.E. today — regular school uniform"
        }

        // 2. Maths & English
        val mathsLessons = lessons.filter {
            val sub = it.subject.lowercase()
            sub.contains("math") || sub.contains("numeracy")
        }
        val englishLessons = lessons.filter {
            val sub = it.subject.lowercase()
            sub.contains("english") || sub.contains("literacy")
        }

        val mathsDetail = if (mathsLessons.isNotEmpty()) {
            val pStr = mathsLessons.map { "P${it.periodIndex}" }.joinToString(", ")
            val teacher = mathsLessons.first().teacher.ifBlank { "" }
            if (teacher.isNotBlank()) "Mathematics ($pStr with $teacher)" else "Mathematics ($pStr)"
        } else null

        val englishDetail = if (englishLessons.isNotEmpty()) {
            val pStr = englishLessons.map { "P${it.periodIndex}" }.joinToString(", ")
            val teacher = englishLessons.first().teacher.ifBlank { "" }
            if (teacher.isNotBlank()) "English ($pStr with $teacher)" else "English ($pStr)"
        } else null

        // 3. Double periods detection (must be non-blank and consecutive periods)
        val doublesList = mutableListOf<String>()
        var idx = 0
        while (idx < lessons.size - 1) {
            val curr = lessons[idx]
            val next = lessons[idx + 1]
            if (curr.subject.isNotBlank() &&
                curr.subject.equals(next.subject, ignoreCase = true) &&
                curr.periodIndex + 1 == next.periodIndex
            ) {
                doublesList.add("Double ${curr.subject} (P${curr.periodIndex} & P${next.periodIndex})")
                idx += 2 // skip consecutive
            } else {
                idx++
            }
        }

        // 4. Time range
        val dayStart = lessons.first().startTime
        val dayEnd = lessons.last().endTime
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
        val lessonCount = lessons.size
        val lessonWord = if (lessonCount == 1) "lesson" else "lessons"

        // Build bullet points
        val bullets = mutableListOf<String>()

        if (needsPe) {
            bullets.add("👟 $peDetail")
        } else {
            bullets.add("👔 $peDetail")
        }

        if (mathsDetail != null && englishDetail != null) {
            bullets.add("📚 You have both $mathsDetail and $englishDetail today.")
        } else if (mathsDetail != null) {
            bullets.add("📐 $mathsDetail today.")
        } else if (englishDetail != null) {
            bullets.add("📖 $englishDetail today.")
        } else {
            bullets.add("✨ Neither Maths nor English scheduled today.")
        }

        if (doublesList.isNotEmpty()) {
            bullets.add("⚡ " + doublesList.joinToString(" · "))
        }

        bullets.add("⏰ $lessonCount $lessonWord total · Starts at ${dayStart.format(timeFmt)}, finishes at ${dayEnd.format(timeFmt)}")

        val headline = if (needsPe) {
            "Pack your P.E. kit for today!"
        } else if (doublesList.isNotEmpty()) {
            "You have ${doublesList.first()} today!"
        } else {
            "You have $lessonCount $lessonWord scheduled today."
        }

        return DailyBriefSummary(
            hasLessons = true,
            needsPeKit = needsPe,
            peDetail = peDetail,
            hasMaths = mathsLessons.isNotEmpty(),
            mathsDetail = mathsDetail,
            hasEnglish = englishLessons.isNotEmpty(),
            englishDetail = englishDetail,
            doubles = doublesList,
            totalLessonsCount = lessonCount,
            dayStartTime = dayStart,
            dayEndTime = dayEnd,
            headline = headline,
            bulletPoints = bullets
        )
    }
}
