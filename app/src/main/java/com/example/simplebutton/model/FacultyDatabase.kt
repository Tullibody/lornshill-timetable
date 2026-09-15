package com.example.simplebutton.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Faculty(
    val id: String,
    val name: String,
    val emoji: String,
    val subjects: List<String>,
    val teachers: List<String>,
    val defaultRooms: List<String>
)

object FacultyDatabase {

    private const val PREFS_NAME = "lornshill_faculty_prefs"
    private const val KEY_CUSTOM_JSON = "custom_faculties_json"
    private const val KEY_IS_CUSTOM = "is_custom_faculty_loaded"
    private const val KEY_SYNC_SOURCE = "faculty_sync_source"
    private const val KEY_LAST_SYNC_TIME = "faculty_last_sync_time"
    private const val KEY_CONFIGURED_URL = "configured_sheet_url"
    private const val KEY_AUTO_SYNC = "auto_sync_on_launch"

    // S1 Stage Faculty & Subject Catalog (Jayden Tully Timetable)
    val s1Faculties = listOf(
        Faculty(
            id = "s1_expressive_arts",
            name = "Expressive Arts",
            emoji = "🎨",
            subjects = listOf("Art", "Creative", "Music"),
            teachers = listOf("Mrs Hilson", "Mrs Watson", "C Watson", "C Milne", "C Bennett", "M Taylor", "R Henry"),
            defaultRooms = listOf("Art Studio 1", "Music Room 1", "Drama Studio")
        ),
        Faculty(
            id = "s1_design_technology",
            name = "Design & Technology",
            emoji = "💻",
            subjects = listOf("Design & Technology"),
            teachers = listOf("Mr Mercer", "Mr Steel", "S Steel", "A Mercer", "E Grant", "L Imlay", "Mr. Watson", "P Kilanowska"),
            defaultRooms = listOf("Tech Workshop 1", "Tech Workshop 2", "IT Suite 1")
        ),
        Faculty(
            id = "s1_enterprise_digital",
            name = "Enterprise / Digital Technologies",
            emoji = "🍳",
            subjects = listOf("Digital Technologies", "Home Economics"),
            teachers = listOf("Miss MacDonald", "Miss Smith", "Ms McCulloch", "Miss Francis", "P Kilanowska", "S Steel"),
            defaultRooms = listOf("IT Suite 1", "HE Kitchen 1", "Tech Workshop 1")
        ),
        Faculty(
            id = "s1_english_literacy",
            name = "English & Literacy",
            emoji = "📚",
            subjects = listOf("English"),
            teachers = listOf("Miss Risk", "Miss Tait", "Mr Ryder", "A Risk", "E Keir", "D McGrouther", "M Lam", "N Cruickshanks", "T Roy"),
            defaultRooms = listOf("Room 3", "Room 4", "Room 15", "Room 16")
        ),
        Faculty(
            id = "s1_languages",
            name = "Languages",
            emoji = "🗣️",
            subjects = listOf("French"),
            teachers = listOf("Mrs Roy", "A Risk", "E Keir", "D McGrouther", "M Lam", "N Cruickshanks", "T Roy", "D Ryder"),
            defaultRooms = listOf("Room 4", "Room 5", "Room 15", "Room 17")
        ),
        Faculty(
            id = "s1_maths",
            name = "Mathematics & Numeracy",
            emoji = "📐",
            subjects = listOf("Mathematics"),
            teachers = listOf("Mr. Blackhall", "Mr Edgar", "G Nisbet", "A Wright", "G Blackhall", "L Rosbotham", "C Sinclair", "J Warnock", "G Young", "R Mearns"),
            defaultRooms = listOf("Room 10", "Room 11", "Room 12", "Room 13")
        ),
        Faculty(
            id = "s1_pe",
            name = "P.E",
            emoji = "👟",
            subjects = listOf("Physical Education"),
            teachers = listOf("Mr Manuel", "Mr Harvey", "Mr Hillis", "C Arbuckle", "M Green", "Miss Clear", "P Manuel", "S Harvey"),
            defaultRooms = listOf("Games Hall", "Small Gym", "Astro Turf")
        ),
        Faculty(
            id = "s1_science",
            name = "Sciences",
            emoji = "🔬",
            subjects = listOf("Science"),
            teachers = listOf("Mrs Imrie-Paterson", "Mrs Whitecross", "Mr Thomas", "L Labus", "R Richards", "G Munro-Faure", "Z Thomas", "C McInnes"),
            defaultRooms = listOf("Lab 1", "Lab 2", "Lab 3", "Lab 4")
        ),
        Faculty(
            id = "s1_social_subjects",
            name = "Social Subjects",
            emoji = "🌍",
            subjects = listOf("Social Subjects"),
            teachers = listOf("Mrs Lawrence", "Mrs O'Donnell", "Mr Simpson", "D Scruton", "G Lawrence", "G O'Donnell", "D Ross", "P Gillon", "F McLean", "S Anestikova", "L Nicol", "J Sandison", "C Simpson"),
            defaultRooms = listOf("Room 8", "Room 14", "Room 18", "Room 19")
        ),
        Faculty(
            id = "s1_other",
            name = "Other",
            emoji = "🤝",
            subjects = listOf("Personal & Social Education"),
            teachers = listOf("Miss Dodds", "Mrs Dixon", "Mr Rennie", "Pastoral Care", "Guidance"),
            defaultRooms = listOf("Guidance Base", "Study Hub")
        )
    )

    // S2 Stage Faculty & Subject Catalog (Jack Nathan Rice Timetable)
    val s2Faculties = listOf(
        Faculty(
            id = "s2_expressive_arts",
            name = "Expressive Arts",
            emoji = "🎨",
            subjects = listOf("Art", "Music"),
            teachers = listOf("Mrs Hilson", "Mrs Watson", "C Watson", "C Milne", "C Bennett", "M Taylor", "R Henry"),
            defaultRooms = listOf("Art Studio 1", "Music Room 1", "Drama Studio")
        ),
        Faculty(
            id = "s2_design_technology",
            name = "Design & Technology",
            emoji = "💻",
            subjects = listOf("Design & Technology"),
            teachers = listOf("Mr Steel", "Mr Mercer", "S Steel", "A Mercer", "E Grant", "L Imlay", "Mr. Watson", "P Kilanowska"),
            defaultRooms = listOf("Tech Workshop 1", "Tech Workshop 2", "IT Suite 1")
        ),
        Faculty(
            id = "s2_enterprise_digital",
            name = "Enterprise / Digital Technologies",
            emoji = "🍳",
            subjects = listOf("Digital Technologies", "Home Economics"),
            teachers = listOf("Ms McCulloch", "Miss Francis", "Miss MacDonald", "Miss Smith", "P Kilanowska", "S Steel"),
            defaultRooms = listOf("IT Suite 1", "HE Kitchen 1", "Tech Workshop 1")
        ),
        Faculty(
            id = "s2_english_literacy",
            name = "English & Literacy",
            emoji = "📚",
            subjects = listOf("English"),
            teachers = listOf("Mr Ryder", "Miss Risk", "Miss Tait", "A Risk", "E Keir", "D McGrouther", "M Lam", "N Cruickshanks", "T Roy"),
            defaultRooms = listOf("Room 3", "Room 4", "Room 15", "Room 16")
        ),
        Faculty(
            id = "s2_languages",
            name = "Languages",
            emoji = "🗣️",
            subjects = listOf("French"),
            teachers = listOf("Mrs Roy", "A Risk", "E Keir", "D McGrouther", "M Lam", "N Cruickshanks", "T Roy", "D Ryder"),
            defaultRooms = listOf("Room 4", "Room 5", "Room 15", "Room 17")
        ),
        Faculty(
            id = "s2_maths",
            name = "Mathematics & Numeracy",
            emoji = "📐",
            subjects = listOf("Mathematics"),
            teachers = listOf("Mr Edgar", "Mr. Blackhall", "G Nisbet", "A Wright", "G Blackhall", "L Rosbotham", "C Sinclair", "J Warnock", "G Young", "R Mearns"),
            defaultRooms = listOf("Room 10", "Room 11", "Room 12", "Room 13")
        ),
        Faculty(
            id = "s2_pe",
            name = "P.E",
            emoji = "👟",
            subjects = listOf("Physical Education"),
            teachers = listOf("Mr Harvey", "Mr Hillis", "Mr Manuel", "C Arbuckle", "M Green", "Miss Clear", "P Manuel", "S Harvey"),
            defaultRooms = listOf("Games Hall", "Small Gym", "Astro Turf")
        ),
        Faculty(
            id = "s2_science",
            name = "Sciences",
            emoji = "🔬",
            subjects = listOf("Science"),
            teachers = listOf("Mrs Whitecross", "Mr Thomas", "Mrs Imrie-Paterson", "L Labus", "R Richards", "G Munro-Faure", "Z Thomas", "C McInnes"),
            defaultRooms = listOf("Lab 1", "Lab 2", "Lab 3", "Lab 4")
        ),
        Faculty(
            id = "s2_social_subjects",
            name = "Social Subjects",
            emoji = "🌍",
            subjects = listOf("Social Subjects"),
            teachers = listOf("Mrs Lawrence", "Mr Simpson", "Mrs O'Donnell", "D Scruton", "G Lawrence", "G O'Donnell", "D Ross", "P Gillon", "F McLean", "S Anestikova", "L Nicol", "J Sandison", "C Simpson"),
            defaultRooms = listOf("Room 8", "Room 14", "Room 18", "Room 19")
        ),
        Faculty(
            id = "s2_other",
            name = "Other",
            emoji = "🤝",
            subjects = listOf("Personal & Social Education"),
            teachers = listOf("Miss Dodds", "Mr Rennie", "Mrs Dixon", "Pastoral Care", "Guidance"),
            defaultRooms = listOf("Guidance Base", "Study Hub")
        )
    )

    // S3+ Stage Faculty & Subject Catalog (User Specified Options)
    val s3PlusFaculties = listOf(
        Faculty(
            id = "s3_design_technology",
            name = "Design & Technology",
            emoji = "💻",
            subjects = listOf(
                "Design & Manufacture",
                "Engineering Science",
                "Graphic Communication",
                "Jewellery Making",
                "Practical Woodworking"
            ),
            teachers = listOf("Mr Imlay", "Mr Watson", "Mr. Watson", "A Mercer", "E Grant", "L Imlay", "Mr Mercer", "Mr Steel", "P Kilanowska", "S Steel"),
            defaultRooms = listOf("Tech Workshop 1", "Tech Workshop 2", "IT Suite 1", "Graphic Studio")
        ),
        Faculty(
            id = "s3_enterprise_digital",
            name = "Enterprise / Digital Technologies",
            emoji = "💼",
            subjects = listOf(
                "Administration & IT",
                "Business Management",
                "Computing Science",
                "Cyber Security",
                "Practical Cookery",
                "Retailing"
            ),
            teachers = listOf("Ms McCulloch", "Miss Francis", "Miss MacDonald", "Miss Smith", "P Kilanowska", "S Steel"),
            defaultRooms = listOf("IT Suite 1", "IT Suite 2", "HE Kitchen 1", "Tech Workshop 1")
        ),
        Faculty(
            id = "s3_pe",
            name = "P.E",
            emoji = "👟",
            subjects = listOf(
                "Core P.E",
                "Dance Leadership",
                "PE Aesthetics",
                "PE Games",
                "Sport & Fitness",
                "Team Sports"
            ),
            teachers = listOf("Mr. Green", "Mr Green", "Mr Hillis", "C Arbuckle", "M Green", "Miss Clear", "Mr Harvey", "Mr Manuel", "P Manuel", "S Harvey", "S Hillis"),
            defaultRooms = listOf("Games Hall", "Small Gym", "Astro Turf")
        ),
        Faculty(
            id = "s3_english_literacy",
            name = "English & Literacy",
            emoji = "📚",
            subjects = listOf("English"),
            teachers = listOf("Mr Ryder", "A Risk", "D McGrouther", "D Ryder", "E Keir", "H Macartney", "M Lam", "Miss Risk", "Miss Tait", "N Cruickshanks", "N Tait", "T Roy"),
            defaultRooms = listOf("Room 3", "Room 4", "Room 15", "Room 16")
        ),
        Faculty(
            id = "s3_maths",
            name = "Mathematics & Numeracy",
            emoji = "📐",
            subjects = listOf("Mathematics"),
            teachers = listOf("Mrs Young", "A Wright", "C Sinclair", "G Blackhall", "G Edgar", "G Nisbet", "G Young", "J Warnock", "L Rosbotham", "Mr Edgar", "Mr. Blackhall", "R Mearns"),
            defaultRooms = listOf("Room 10", "Room 11", "Room 12", "Room 13")
        ),
        Faculty(
            id = "s3_sciences",
            name = "Sciences",
            emoji = "🔬",
            subjects = listOf(
                "Biology",
                "Chemistry",
                "Health Sector",
                "Laboratory Science",
                "Physics"
            ),
            teachers = listOf("Mr Munro-Faure", "G Munro-Faure", "C McInnes", "L Labus", "L Whitecross", "Mr Thomas", "Mrs Imrie-Paterson", "Mrs Whitecross", "R Richards", "S Imrie-Paterson", "Z Thomas"),
            defaultRooms = listOf("Lab 1", "Lab 2", "Lab 3", "Lab 4")
        ),
        Faculty(
            id = "s3_social_subjects",
            name = "Social Subjects",
            emoji = "🌍",
            subjects = listOf(
                "Criminology",
                "Geography",
                "History",
                "Modern Studies",
                "Religious, Moral and Philosophical Studies",
                "Travel & Tourism"
            ),
            teachers = listOf("Mrs Lawrence", "Mr Ross", "D Ross", "C Simpson", "D Scruton", "F McLean", "G Lawrence", "G O'Donnell", "J Sandison", "L Nicol", "Mr Simpson", "Mrs O'Donnell", "P Gillon", "S Anestikova"),
            defaultRooms = listOf("Room 8", "Room 14", "Room 18", "Room 19")
        ),
        Faculty(
            id = "s3_expressive_arts",
            name = "Expressive Arts",
            emoji = "🎨",
            subjects = listOf(
                "Art & Design",
                "Creative Industries",
                "Media",
                "Music",
                "Music Technology",
                "Photography"
            ),
            teachers = listOf("C Bennett", "C Hilson", "C Milne", "C Watson", "M Taylor", "Mrs Hilson", "Mrs Watson", "R Henry"),
            defaultRooms = listOf("Art Studio 1", "Art Studio 3", "Music Room 1", "Drama Studio")
        ),
        Faculty(
            id = "s3_languages",
            name = "Languages",
            emoji = "🗣️",
            subjects = listOf(
                "French",
                "Languages for Life, Learning & Work",
                "Spanish"
            ),
            teachers = listOf("A Risk", "D McGrouther", "D Ryder", "E Keir", "H Macartney", "M Lam", "Mrs Roy", "N Cruickshanks", "N Tait", "T Roy"),
            defaultRooms = listOf("Room 4", "Room 5", "Room 15", "Room 16", "Room 17")
        ),
        Faculty(
            id = "s3_other",
            name = "Other",
            emoji = "🤝",
            subjects = listOf(
                "Personal Development Award",
                "Personal & Social Education"
            ),
            teachers = listOf("Miss Dodds", "Guidance Base", "Mr Rennie", "Mrs Dixon", "Pastoral Care"),
            defaultRooms = listOf("Guidance Base", "Study Hub")
        )
    )

    // Observable active faculties list
    val faculties = mutableStateListOf<Faculty>().apply {
        addAll(s1Faculties)
    }

    // Metadata observables for UI status
    val isCustomLoaded = mutableStateOf(false)
    val syncSource = mutableStateOf("Lornshill Google Sheet")
    val lastSyncTime = mutableStateOf<String?>("Live Official Sheet")
    val configuredSheetUrl = mutableStateOf(GoogleSheetsFacultyReader.DEFAULT_GOOGLE_SHEET_URL)
    val autoSyncOnLaunch = mutableStateOf(true)

    val totalTeachersCount: Int
        get() = faculties.sumOf { it.teachers.size }

    /**
     * Initializes the FacultyDatabase from SharedPreferences on app launch.
     */
    fun init(context: Context?) {
        if (context == null) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        configuredSheetUrl.value = prefs.getString(KEY_CONFIGURED_URL, GoogleSheetsFacultyReader.DEFAULT_GOOGLE_SHEET_URL)
            ?: GoogleSheetsFacultyReader.DEFAULT_GOOGLE_SHEET_URL
        autoSyncOnLaunch.value = prefs.getBoolean(KEY_AUTO_SYNC, true)

        val customJson = prefs.getString(KEY_CUSTOM_JSON, null)
        val isCustom = prefs.getBoolean(KEY_IS_CUSTOM, false)

        if (isCustom && !customJson.isNullOrBlank()) {
            try {
                val parsedList = deserializeFaculties(customJson)
                if (parsedList.isNotEmpty()) {
                    faculties.clear()
                    faculties.addAll(parsedList)
                    isCustomLoaded.value = true
                    syncSource.value = prefs.getString(KEY_SYNC_SOURCE, "Lornshill Google Sheet") ?: "Lornshill Google Sheet"
                    lastSyncTime.value = prefs.getString(KEY_LAST_SYNC_TIME, null)
                    return
                }
            } catch (_: Exception) {
                // Fallback to defaults if corrupted
            }
        }

        // Default state using the Lornshill Academy records
        faculties.clear()
        faculties.addAll(defaultFaculties)
        isCustomLoaded.value = false
        syncSource.value = "Lornshill Google Sheet"
        lastSyncTime.value = "Built-in Records"
    }

    /**
     * Updates and saves the configured Google Sheet URL.
     */
    fun updateConfiguredSheetUrl(context: Context?, url: String) {
        val trimmed = url.trim()
        configuredSheetUrl.value = trimmed
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_CONFIGURED_URL, trimmed).apply()
        }
    }

    /**
     * Updates and saves the auto-sync preference.
     */
    fun setAutoSyncEnabled(context: Context?, enabled: Boolean) {
        autoSyncOnLaunch.value = enabled
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
        }
    }

    /**
     * Fetches and syncs from the currently configured Google Sheet URL.
     */
    suspend fun syncFromConfiguredSheet(context: Context?): SheetSyncResult {
        val result = GoogleSheetsFacultyReader.fetchAndParse(configuredSheetUrl.value)
        if (result is SheetSyncResult.Success) {
            applyCustomFaculties(context, result.faculties, "Lornshill Google Sheet")
        }
        return result
    }

    /**
     * Quietly syncs from the configured Google Sheet in the background on app startup.
     */
    suspend fun autoSyncIfConfigured(context: Context?) {
        if (!autoSyncOnLaunch.value) return
        try {
            syncFromConfiguredSheet(context)
        } catch (_: Exception) {
            // Silently retain cached roster on offline startup
        }
    }

    /**
     * Applies and persists custom faculties parsed from Google Sheets or CSV.
     */
    fun applyCustomFaculties(context: Context?, newFaculties: List<Faculty>, source: String) {
        if (newFaculties.isEmpty()) return

        faculties.clear()
        faculties.addAll(newFaculties)

        val timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM, HH:mm"))
        isCustomLoaded.value = true
        syncSource.value = source
        lastSyncTime.value = timeStr

        if (context != null) {
            val jsonStr = serializeFaculties(newFaculties)
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_CUSTOM_JSON, jsonStr)
                .putBoolean(KEY_IS_CUSTOM, true)
                .putString(KEY_SYNC_SOURCE, source)
                .putString(KEY_LAST_SYNC_TIME, timeStr)
                .apply()
        }
    }

    val defaultFaculties: List<Faculty>
        get() = s1Faculties

    /**
     * Maps year strings like "S1", "S2", "S3", "S4", "S5", "S6" into "S1", "S2", or "S3+".
     */
    fun getStageCategory(yearGroup: String): String {
        return when (yearGroup.trim().uppercase()) {
            "S1" -> "S1"
            "S2" -> "S2"
            else -> "S3+"
        }
    }

    /**
     * Returns strictly the faculties and subject offerings configured for the specified year group.
     */
    fun getFacultiesForStage(yearGroup: String): List<Faculty> {
        return when (getStageCategory(yearGroup)) {
            "S1" -> s1Faculties
            "S2" -> s2Faculties
            else -> s3PlusFaculties
        }
    }

    /**
     * Clears custom roster and reverts back to Lornshill Academy defaults.
     */
    fun resetToDefaults(context: Context?) {
        faculties.clear()
        faculties.addAll(s1Faculties)
        isCustomLoaded.value = false
        syncSource.value = "Lornshill Google Sheet"
        lastSyncTime.value = "Default Roster"
        configuredSheetUrl.value = GoogleSheetsFacultyReader.DEFAULT_GOOGLE_SHEET_URL

        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_CUSTOM_JSON)
                .putBoolean(KEY_IS_CUSTOM, false)
                .remove(KEY_SYNC_SOURCE)
                .remove(KEY_LAST_SYNC_TIME)
                .remove(KEY_CONFIGURED_URL)
                .apply()
        }
    }

    fun getAllSubjects(): List<String> {
        return faculties.flatMap { it.subjects }.distinct().sorted()
    }

    fun getAllTeachers(): List<String> {
        return faculties.flatMap { it.teachers }.distinct().sorted()
    }

    fun getFacultyForSubject(subjectName: String, stageYear: String? = null): Faculty? {
        val clean = subjectName.trim().lowercase()
        if (clean.isBlank()) return null

        val stageList = if (!stageYear.isNullOrBlank()) getFacultiesForStage(stageYear) else emptyList()
        val stageMatch = stageList.firstOrNull { faculty ->
            faculty.subjects.any { it.lowercase() == clean } ||
                faculty.subjects.any { clean.contains(it.lowercase()) || it.lowercase().contains(clean) } ||
                faculty.name.lowercase() == clean
        }
        if (stageMatch != null) return stageMatch

        val activeMatch = faculties.firstOrNull { faculty ->
            faculty.subjects.any { it.lowercase() == clean } ||
                faculty.subjects.any { clean.contains(it.lowercase()) || it.lowercase().contains(clean) } ||
                faculty.name.lowercase() == clean
        }
        if (activeMatch != null) return activeMatch

        // Fallback across all standard faculties (S1, S2, S3+)
        val allCatalog = s1Faculties + s2Faculties + s3PlusFaculties
        val catalogMatch = allCatalog.firstOrNull { faculty ->
            faculty.subjects.any { it.lowercase() == clean } ||
                faculty.subjects.any { clean.contains(it.lowercase()) || it.lowercase().contains(clean) } ||
                faculty.name.lowercase() == clean
        }
        if (catalogMatch != null) return catalogMatch

        // Heuristic keyword matching for custom or abbreviated names
        return when {
            clean.contains("math") || clean.contains("numeracy") || clean.contains("calc") ->
                s1Faculties.find { it.id == "s1_maths" }
            clean.contains("art") || clean.contains("music") || clean.contains("drama") || clean.contains("photo") || clean.contains("creative") || clean.contains("media") ->
                s1Faculties.find { it.id == "s1_expressive_arts" }
            clean.contains("sci") || clean.contains("bio") || clean.contains("chem") || clean.contains("phys") || clean.contains("lab") ->
                s1Faculties.find { it.id == "s1_science" }
            clean.contains("p.e") || clean.contains("pe") || clean.contains("sport") || clean.contains("gym") || clean.contains("fitness") || clean.contains("dance") ->
                s1Faculties.find { it.id == "s1_pe" }
            clean.contains("eng") || clean.contains("lit") ->
                s1Faculties.find { it.id == "s1_english_literacy" }
            clean.contains("french") || clean.contains("spanish") || clean.contains("german") || clean.contains("lang") ->
                s1Faculties.find { it.id == "s1_languages" }
            clean.contains("tech") || clean.contains("wood") || clean.contains("design") || clean.contains("manufacture") || clean.contains("graphic") ->
                s1Faculties.find { it.id == "s1_design_technology" }
            clean.contains("comput") || clean.contains("cyber") || clean.contains("digital") || clean.contains("business") || clean.contains("cook") || clean.contains("food") || clean.contains("enterprise") || clean.contains("admin") ->
                s3PlusFaculties.find { it.id == "s3_enterprise_digital" } ?: s1Faculties.find { it.id == "s1_enterprise_digital" }
            clean.contains("geog") || clean.contains("hist") || clean.contains("modern") || clean.contains("rmps") || clean.contains("social") || clean.contains("crimin") ->
                s1Faculties.find { it.id == "s1_social_subjects" }
            clean.contains("pse") || clean.contains("guidance") || clean.contains("pastoral") || clean.contains("personal") ->
                s1Faculties.find { it.id == "s1_other" }
            else -> null
        }
    }

    /**
     * Returns the department emoji corresponding to a subject.
     */
    fun getDepartmentEmojiForSubject(subjectName: String, stageYear: String? = null): String {
        return getFacultyForSubject(subjectName, stageYear)?.emoji ?: "📖"
    }

    fun getTeachersForSubject(subjectName: String, stageYear: String? = null): List<String> {
        val faculty = getFacultyForSubject(subjectName, stageYear)
        return faculty?.teachers ?: emptyList()
    }

    fun suggestRoomForSubject(subjectName: String, stageYear: String? = null): String {
        val faculty = getFacultyForSubject(subjectName, stageYear)
        return faculty?.defaultRooms?.firstOrNull() ?: "Room 1"
    }

    private fun serializeFaculties(list: List<Faculty>): String {
        val array = JSONArray()
        for (fac in list) {
            val obj = JSONObject().apply {
                put("id", fac.id)
                put("name", fac.name)
                put("emoji", fac.emoji)

                val subs = JSONArray()
                fac.subjects.forEach { subs.put(it) }
                put("subjects", subs)

                val teachers = JSONArray()
                fac.teachers.forEach { teachers.put(it) }
                put("teachers", teachers)

                val rooms = JSONArray()
                fac.defaultRooms.forEach { rooms.put(it) }
                put("defaultRooms", rooms)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeFaculties(jsonStr: String): List<Faculty> {
        val list = mutableListOf<Faculty>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.optString("id", "fac_$i")
            val name = obj.optString("name", "Faculty")
            val emoji = obj.optString("emoji", "🏛️")

            val subjects = mutableListOf<String>()
            val subsArr = obj.optJSONArray("subjects")
            if (subsArr != null) {
                for (j in 0 until subsArr.length()) {
                    subjects.add(subsArr.getString(j))
                }
            }

            val teachers = mutableListOf<String>()
            val teachArr = obj.optJSONArray("teachers")
            if (teachArr != null) {
                for (j in 0 until teachArr.length()) {
                    teachers.add(teachArr.getString(j))
                }
            }

            val rooms = mutableListOf<String>()
            val roomArr = obj.optJSONArray("defaultRooms")
            if (roomArr != null) {
                for (j in 0 until roomArr.length()) {
                    rooms.add(roomArr.getString(j))
                }
            }

            list.add(
                Faculty(
                    id = id,
                    name = name,
                    emoji = emoji,
                    subjects = subjects,
                    teachers = teachers,
                    defaultRooms = rooms
                )
            )
        }
        return list
    }
}
