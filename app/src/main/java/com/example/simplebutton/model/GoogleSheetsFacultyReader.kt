package com.example.simplebutton.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

sealed class SheetSyncResult {
    data class Success(
        val faculties: List<Faculty>,
        val totalTeachers: Int,
        val resolvedUrl: String
    ) : SheetSyncResult()

    data class Error(val message: String) : SheetSyncResult()
}

object GoogleSheetsFacultyReader {

    // The user's active Lornshill Academy Google Sheet URL
    const val DEFAULT_GOOGLE_SHEET_URL = "https://docs.google.com/spreadsheets/d/1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I/edit?usp=sharing"

    // Default sample CSV dataset reflecting the actual Lornshill Academy Google Sheet
    const val SAMPLE_CSV = """,Teacher Name,Department
,C Watson,Creative Arts
,C Milne,Creative Arts
,C Bennett,Creative Arts
,R Henry,Creative Arts
,M Taylor,Creative Arts
,C Hilson,Creative Arts
,S Steel,Design & Technology
,L Imlay,Design & Technology
,A Mercer,Design & Technology
,E Grant,Design & Technology
,P Kilanowska,Design & Technology
,Mr. Watson,Design & Technology
,A Risk,Languages
,E Keir,Languages
,D McGrouther,Languages
,M Lam,Languages
,N Tait,Languages
,H Macartney,Languages
,D Ryder,Languages
,N Cruickshanks,Languages
,T Roy,Languages
,G Nisbet,Maths
,G Blackhall,Maths
,A Wright,Maths
,G Edgar,Maths
,L Rosbotham,Maths
,C Sinclair,Maths
,J Warnock,Maths
,G Young,Maths
,R Mearns,Maths
,S Hillis,P.E
,C Arbuckle,P.E
,M Green,P.E
,S Harvey,P.E
,Miss Clear,P.E
,P Manuel,P.E
,L Labus,Science
,S Imrie-Paterson,Science
,R Richards,Science
,G Munro-Faure,Science
,Z Thomas,Science
,L Whitecross,Science
,C McInnes,Science
,D Scruton,Social Subjects
,G Lawrence,Social Subjects
,G O'Donnell,Social Subjects
,D Ross,Social Subjects
,P Gillon,Social Subjects
,F McLean,Social Subjects
,S Anestikova,Social Subjects
,L Nicol,Social Subjects
,J Sandison,Social Subjects
,C Simpson,Social Subjects"""

    /**
     * Converts various Google Sheets URLs, Published Links, or raw IDs
     * into a direct CSV download export URL.
     */
    fun normalizeToCsvUrl(rawInput: String): String {
        val trimmed = rawInput.trim()

        // Extract gid (sheet tab ID) if present in the URL
        val gidMatcher = Pattern.compile("[?&#]gid=([0-9]+)").matcher(trimmed)
        val gidParam = if (gidMatcher.find()) "&gid=${gidMatcher.group(1)}" else ""

        // Case 1: Published to web URL (pubhtml)
        // https://docs.google.com/spreadsheets/d/e/{ID}/pubhtml
        val pubhtmlMatcher = Pattern.compile("docs\\.google\\.com/spreadsheets/d/e/([a-zA-Z0-9_-]+)/pubhtml").matcher(trimmed)
        if (pubhtmlMatcher.find()) {
            val pubId = pubhtmlMatcher.group(1)
            val gidQuery = if (gidParam.isNotBlank()) gidParam.replace('&', '?') else ""
            return "https://docs.google.com/spreadsheets/d/e/$pubId/pub?output=csv$gidQuery"
        }

        // Case 2: Standard Google Sheets view/edit URL
        // https://docs.google.com/spreadsheets/d/{ID}/edit...
        val standardMatcher = Pattern.compile("docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9_-]+)").matcher(trimmed)
        if (standardMatcher.find()) {
            val sheetId = standardMatcher.group(1)
            return "https://docs.google.com/spreadsheets/d/$sheetId/export?format=csv$gidParam"
        }

        // Case 3: Already an export / CSV url
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }

        // Case 4: Pure Google Sheet ID provided (e.g. 1dBgqJ2Ug9eg1KDIaFmQjhiJ1-Cjnx-_p_n4XJozrk0I)
        if (trimmed.matches(Regex("^[a-zA-Z0-9_-]{20,}$"))) {
            return "https://docs.google.com/spreadsheets/d/$trimmed/export?format=csv"
        }

        return trimmed
    }

    /**
     * Fetches CSV text from a URL using HttpURLConnection with redirect following.
     */
    suspend fun fetchCsvText(urlStr: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            var currentUrl = urlStr
            var redirectCount = 0
            val maxRedirects = 6

            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 15000
                    instanceFollowRedirects = false
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Android; Mobile; rv:109.0) Gecko/109.0 Firefox/119.0"
                    )
                    setRequestProperty("Accept", "text/csv,text/plain,*/*")
                }

                val responseCode = connection.responseCode

                // Handle Redirects
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                        ?: return@withContext Result.failure(Exception("Redirect without Location header (HTTP $responseCode)"))
                    currentUrl = if (location.startsWith("http")) location else URL(url, location).toString()
                    redirectCount++
                    connection.disconnect()
                    continue
                }

                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    return@withContext Result.failure(
                        Exception("Spreadsheet is private. In Google Sheets, click Share and set General Access to 'Anyone with the link can view'.")
                    )
                }

                if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    return@withContext Result.failure(
                        Exception("Spreadsheet not found (HTTP 404). Please verify your Google Sheet link or ID.")
                    )
                }

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
                        Exception("Failed to download spreadsheet (HTTP $responseCode).")
                    )
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8))
                val content = reader.use { it.readText() }

                // Detect if Google returned a sign-in HTML page instead of CSV
                if (content.contains("<html") && (content.contains("accounts.google.com") || content.contains("Sign in - Google Accounts"))) {
                    return@withContext Result.failure(
                        Exception("Google requires sign-in to view this sheet. Set General Access to 'Anyone with the link can view'.")
                    )
                }

                return@withContext Result.success(content)
            }

            Result.failure(Exception("Too many redirects while accessing spreadsheet."))
        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: "Network error connecting to Google Sheets. Check your connection.")
            )
        }
    }

    /**
     * Parses a CSV/TSV table into a grouped List of [Faculty] models.
     */
    fun parseCsv(csvText: String): List<Faculty> {
        val rawRows = parseRawCsvRows(csvText)
        if (rawRows.isEmpty()) return emptyList()

        // 1. Identify Header Row & Column Mappings (search up to 30 rows for banners/titles)
        var headerIndex = -1
        var facultyCol = -1
        var teacherCol = -1
        var subjectCol = -1
        var roomCol = -1
        var emojiCol = -1

        for (i in 0 until minOf(rawRows.size, 30)) {
            val row = rawRows[i].map { it.trim().lowercase() }
            val fCol = row.indexOfFirst { it.contains("faculty") || it.contains("department") || it.contains("dept") }
            val tCol = row.indexOfFirst {
                it.contains("teacher") || it.contains("staff") || it.contains("educator") || it == "name" || it.contains("teacher name")
            }

            if (fCol != -1 && tCol != -1) {
                headerIndex = i
                facultyCol = fCol
                teacherCol = tCol
                subjectCol = row.indexOfFirst { it.contains("subject") || it.contains("course") || it.contains("class") }
                roomCol = row.indexOfFirst { it.contains("room") || it.contains("location") || it.contains("classroom") }
                emojiCol = row.indexOfFirst { it.contains("emoji") || it.contains("icon") }
                break
            }
        }

        val dataRows = if (headerIndex != -1) {
            rawRows.subList(headerIndex + 1, rawRows.size)
        } else {
            // Heuristic detection: if col 0 looks like a teacher honorific, col 0=teacher, 1=faculty
            val firstRow = rawRows[0]
            val firstColLooksLikeTeacher = firstRow.firstOrNull()?.trim()?.let {
                it.startsWith("Mr") || it.startsWith("Mrs") || it.startsWith("Miss") || it.startsWith("Dr") || it.startsWith("Ms")
            } ?: false

            if (firstColLooksLikeTeacher) {
                teacherCol = 0
                facultyCol = if (firstRow.size > 1) 1 else 0
                subjectCol = if (firstRow.size > 2) 2 else -1
                roomCol = if (firstRow.size > 3) 3 else -1
            } else {
                facultyCol = 0
                teacherCol = if (firstRow.size > 1) 1 else 0
                subjectCol = if (firstRow.size > 2) 2 else -1
                roomCol = if (firstRow.size > 3) 3 else -1
            }
            rawRows
        }

        // 2. Aggregate data by Faculty name
        data class FacultyBuilder(
            val name: String,
            var emoji: String = "",
            val subjects: MutableSet<String> = mutableSetOf(),
            val teachers: MutableSet<String> = mutableSetOf(),
            val rooms: MutableSet<String> = mutableSetOf()
        )

        val builders = linkedMapOf<String, FacultyBuilder>()

        for (row in dataRows) {
            if (row.all { it.isBlank() }) continue

            val facultyName = (if (facultyCol in row.indices) row[facultyCol] else "").trim()
            val teacherName = (if (teacherCol in row.indices) row[teacherCol] else "").trim()
            val subjectVal = (if (subjectCol != -1 && subjectCol in row.indices) row[subjectCol] else "").trim()
            val roomVal = (if (roomCol != -1 && roomCol in row.indices) row[roomCol] else "").trim()
            val emojiVal = (if (emojiCol != -1 && emojiCol in row.indices) row[emojiCol] else "").trim()

            if (facultyName.isBlank() && teacherName.isBlank()) continue

            val effectiveFaculty = if (facultyName.isNotBlank()) facultyName else "General Faculty"
            val builder = builders.getOrPut(effectiveFaculty) {
                FacultyBuilder(name = effectiveFaculty)
            }

            if (emojiVal.isNotBlank() && builder.emoji.isBlank()) {
                builder.emoji = emojiVal
            }

            // Parse teachers (can be comma, semicolon, or slash separated if multiple in one cell)
            if (teacherName.isNotBlank()) {
                val teacherParts = teacherName.split(Regex("[,;/]"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                builder.teachers.addAll(teacherParts)
            }

            // Parse subjects
            if (subjectVal.isNotBlank()) {
                val subParts = subjectVal.split(Regex("[,;/]"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                builder.subjects.addAll(subParts)
            }

            // Parse rooms
            if (roomVal.isNotBlank()) {
                val roomParts = roomVal.split(Regex("[,;/]"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                builder.rooms.addAll(roomParts)
            }
        }

        // 3. Finalize Faculty models
        return builders.values.map { b ->
            val cleanId = b.name.lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "faculty_${System.currentTimeMillis()}" }

            val emoji = if (b.emoji.isNotBlank()) {
                b.emoji
            } else {
                guessFacultyEmoji(b.name)
            }

            val subjectsList = if (b.subjects.isNotEmpty()) {
                b.subjects.toList().sorted()
            } else {
                guessDepartmentSubjects(b.name)
            }

            val defaultRooms = if (b.rooms.isNotEmpty()) {
                b.rooms.toList().sorted()
            } else {
                guessDepartmentRooms(b.name)
            }

            Faculty(
                id = cleanId,
                name = b.name,
                emoji = emoji,
                subjects = subjectsList,
                teachers = b.teachers.toList().sorted(),
                defaultRooms = defaultRooms
            )
        }.filter { it.teachers.isNotEmpty() || it.subjects.isNotEmpty() }
    }

    /**
     * Complete fetch and parse pipeline.
     */
    suspend fun fetchAndParse(input: String = DEFAULT_GOOGLE_SHEET_URL): SheetSyncResult {
        val resolvedUrl = normalizeToCsvUrl(input)
        val downloadResult = fetchCsvText(resolvedUrl)

        return downloadResult.fold(
            onSuccess = { csvText ->
                val faculties = parseCsv(csvText)
                if (faculties.isEmpty()) {
                    SheetSyncResult.Error("No teacher or faculty records could be extracted from the sheet. Check column headers.")
                } else {
                    val totalTeachers = faculties.sumOf { it.teachers.size }
                    SheetSyncResult.Success(
                        faculties = faculties,
                        totalTeachers = totalTeachers,
                        resolvedUrl = resolvedUrl
                    )
                }
            },
            onFailure = { error ->
                SheetSyncResult.Error(error.message ?: "Failed to fetch spreadsheet.")
            }
        )
    }

    /**
     * Splits CSV/TSV content into rows and tokens while respecting quotes and escapes.
     */
    private fun parseRawCsvRows(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val lines = csvText.lines()
        var currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false

        for (line in lines) {
            var i = 0
            while (i < line.length) {
                val c = line[i]
                if (c == '"') {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        currentField.append('"')
                        i++ // skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                } else if (!inQuotes && (c == ',' || c == '\t')) {
                    currentRow.add(currentField.toString().trim())
                    currentField.clear()
                } else {
                    currentField.append(c)
                }
                i++
            }

            if (!inQuotes) {
                currentRow.add(currentField.toString().trim())
                currentField.clear()
                if (currentRow.any { it.isNotBlank() }) {
                    rows.add(currentRow)
                }
                currentRow = mutableListOf()
            } else {
                currentField.append('\n')
            }
        }

        if (currentRow.isNotEmpty() || currentField.isNotEmpty()) {
            currentRow.add(currentField.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow)
            }
        }

        return rows
    }

    /**
     * Intelligent emoji guesser for faculties based on common keywords.
     */
    fun guessFacultyEmoji(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("creative") || lower.contains("art") || lower.contains("music") || lower.contains("drama") || lower.contains("photo") -> "🎨"
            lower.contains("design") || lower.contains("tech") || lower.contains("computing") || lower.contains("it") || lower.contains("craft") -> "💻"
            lower.contains("math") || lower.contains("numeracy") || lower.contains("algebra") -> "📐"
            lower.contains("science") || lower.contains("physics") || lower.contains("chemistry") || lower.contains("biology") -> "🔬"
            lower.contains("english") || lower.contains("literacy") || lower.contains("book") -> "📚"
            lower.contains("social") || lower.contains("history") || lower.contains("geography") || lower.contains("modern") || lower.contains("rme") || lower.contains("rmps") || lower.contains("politics") -> "🌍"
            lower.contains("pe") || lower.contains("p.e") || lower.contains("sport") || lower.contains("health") || lower.contains("gym") -> "👟"
            lower.contains("language") || lower.contains("french") || lower.contains("spanish") || lower.contains("german") || lower.contains("gaelic") -> "🗣️"
            lower.contains("guidance") || lower.contains("support") || lower.contains("pastoral") || lower.contains("pupil") -> "🤝"
            else -> "🏛️"
        }
    }

    /**
     * Default Scottish secondary school subjects for departments when not specified in sheet.
     */
    fun guessDepartmentSubjects(department: String): List<String> {
        val lower = department.lowercase()
        return when {
            lower.contains("creative") || lower.contains("art") || lower.contains("music") || lower.contains("drama") ->
                listOf("Art & Design", "Music", "Drama", "Photography", "Creative Arts")
            lower.contains("design") || lower.contains("tech") || lower.contains("craft") || lower.contains("woodwork") ->
                listOf("Design & Technology", "Graphic Communication", "Practical Woodworking", "Computing Science", "Technologies")
            lower.contains("language") || lower.contains("french") || lower.contains("spanish") || lower.contains("german") ->
                listOf("English", "French", "Spanish", "German", "Media Studies", "Languages")
            lower.contains("math") || lower.contains("numeracy") ->
                listOf("Mathematics", "Applications of Maths", "Lifeskills Maths", "Advanced Higher Maths")
            lower.contains("pe") || lower.contains("p.e") || lower.contains("sport") || lower.contains("physical") ->
                listOf("Physical Education", "Core P.E.", "Sports Studies", "Health & Wellbeing")
            lower.contains("science") || lower.contains("biology") || lower.contains("chemistry") || lower.contains("physics") ->
                listOf("Science", "Biology", "Chemistry", "Physics", "Laboratory Science")
            lower.contains("social") || lower.contains("history") || lower.contains("geography") || lower.contains("modern") ->
                listOf("Geography", "History", "Modern Studies", "Religious, Moral and Philosophical Studies", "Politics", "Sociology")
            lower.contains("guidance") || lower.contains("support") ->
                listOf("Personal & Social Education", "Guidance", "Study Support", "Pastoral Care")
            else -> listOf(department)
        }.sorted()
    }

    /**
     * Default room locations for departments when not specified in sheet.
     */
    fun guessDepartmentRooms(department: String): List<String> {
        val lower = department.lowercase()
        return when {
            lower.contains("creative") || lower.contains("art") || lower.contains("music") || lower.contains("drama") ->
                listOf("Art Studio 1", "Art Studio 3", "Music Room 1", "Drama Studio")
            lower.contains("design") || lower.contains("tech") ->
                listOf("Tech Workshop 1", "Tech Workshop 2", "IT Suite 1", "Graphic Studio")
            lower.contains("language") ->
                listOf("Room 3", "Room 4", "Room 5", "Room 15", "Room 16")
            lower.contains("math") ->
                listOf("Room 10", "Room 11", "Room 12", "Room 13")
            lower.contains("pe") || lower.contains("p.e") || lower.contains("sport") ->
                listOf("Games Hall", "Small Gym", "Astro Turf")
            lower.contains("science") ->
                listOf("Lab 1", "Lab 2", "Lab 3", "Lab 4")
            lower.contains("social") ->
                listOf("Room 8", "Room 14", "Room 18", "Room 19")
            lower.contains("guidance") ->
                listOf("Guidance Base", "Study Hub")
            else -> listOf("Room 1")
        }
    }
}
