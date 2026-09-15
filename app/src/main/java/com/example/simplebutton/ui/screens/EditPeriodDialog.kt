package com.example.simplebutton.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors
import java.time.DayOfWeek
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPeriodDialog(
    period: TimetablePeriod,
    studentYear: String = "S1",
    allPeriods: List<TimetablePeriod> = emptyList(),
    findDefaultTeacher: ((String) -> String?)? = null,
    onDismiss: () -> Unit,
    onSave: (TimetablePeriod) -> Unit,
    onClear: () -> Unit
) {
    val colors = appColors()
    if (period.isBreakOrLunch) {
        onDismiss()
        return
    }

    val stageCategory = FacultyDatabase.getStageCategory(studentYear)
    val stageFaculties = remember(studentYear, FacultyDatabase.faculties.size) {
        FacultyDatabase.getFacultiesForStage(studentYear)
    }
    val stageSubjects = remember(stageFaculties) {
        stageFaculties.flatMap { it.subjects }.distinct().sorted()
    }

    val resolveDefaultTeacher: (String) -> String? = { sub ->
        val fromRepo = findDefaultTeacher?.invoke(sub)
        if (!fromRepo.isNullOrBlank()) {
            fromRepo
        } else {
            val clean = sub.trim()
            if (clean.isBlank()) null
            else {
                allPeriods.firstOrNull {
                    it.id != period.id && it.isAssigned && it.teacher.isNotBlank() &&
                    it.subject.trim().equals(clean, ignoreCase = true)
                }?.teacher
            }
        }
    }

    val initialTeacher = if (period.teacher.isNotBlank()) {
        period.teacher
    } else if (period.subject.isNotBlank()) {
        resolveDefaultTeacher(period.subject) ?: ""
    } else {
        ""
    }

    val initialIsCustom = period.subject.isNotBlank() &&
        !stageSubjects.any { it.equals(period.subject, ignoreCase = true) }

    var isCustomMode by remember { mutableStateOf(initialIsCustom) }

    var subject by remember { mutableStateOf(period.subject) }
    var teacher by remember { mutableStateOf(initialTeacher) }
    var isTeacherAutoFilled by remember {
        mutableStateOf(period.teacher.isBlank() && initialTeacher.isNotBlank())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val timetableSuggestions = remember(allPeriods, period.id) {
        allPeriods
            .filter { it.id != period.id && it.isAssigned && it.teacher.isNotBlank() }
            .map { it.subject.trim() to it.teacher.trim() }
            .distinctBy { it.first.lowercase() }
            .sortedBy { it.first.lowercase() }
    }

    // Dropdown expanded states
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    val currentFaculty = FacultyDatabase.getFacultyForSubject(subject, studentYear)
    val availableTeachers = remember(subject, studentYear, FacultyDatabase.faculties.size, allPeriods) {
        val dbTeachers = if (subject.isNotBlank()) {
            FacultyDatabase.getTeachersForSubject(subject, studentYear)
        } else {
            stageFaculties.flatMap { it.teachers }.distinct()
        }.toMutableList()

        val defaultTeacher = if (subject.isNotBlank()) resolveDefaultTeacher(subject) else null
        if (!defaultTeacher.isNullOrBlank() && !dbTeachers.contains(defaultTeacher)) {
            dbTeachers.add(0, defaultTeacher)
        }
        dbTeachers
    }

    val dayName = period.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(26.dp),
            color = colors.cardBg,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "EDIT PERIOD ${period.periodIndex}",
                            fontFamily = Staatliches,
                            fontSize = 20.sp,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$dayName · ${period.formattedTime}",
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accentCobalt
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Custom Mode Toggle Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCustomMode) (if (colors.isDark) Color(0xFF1E3A8A).copy(alpha = 0.5f) else LornshillBlueContainer) else colors.chipBg)
                                .clickable { isCustomMode = !isCustomMode }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCustomMode) Icons.Default.List else Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = if (isCustomMode) colors.accentCobalt else colors.textPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCustomMode) "Preset DB" else "Custom",
                                    fontFamily = Lexend,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCustomMode) colors.accentCobalt else colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel",
                                tint = colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stage Info Tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STAGE: $stageCategory CURRICULUM",
                        fontFamily = Staatliches,
                        fontSize = 12.sp,
                        color = LornshillCobalt,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${stageSubjects.size} classes available",
                        fontFamily = Lexend,
                        fontSize = 11.sp,
                        color = LornshillTextSecondary
                    )
                }

                // Subject Selection
                if (!isCustomMode) {
                    // Dropdown for Subject
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Subject ($stageCategory)", fontFamily = Lexend) },
                            placeholder = { Text("Choose a subject...", fontFamily = Lexend) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LornshillCobalt,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            stageFaculties.forEach { faculty ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${faculty.emoji} ${faculty.name.uppercase()}",
                                            fontFamily = Staatliches,
                                            fontSize = 12.5.sp,
                                            color = LornshillCobalt,
                                            letterSpacing = 0.5.sp
                                        )
                                    },
                                    onClick = {},
                                    enabled = false
                                )
                                faculty.subjects.forEach { subjName ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "    $subjName",
                                                fontFamily = Lexend,
                                                fontSize = 13.5.sp,
                                                color = colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            subject = subjName
                                            subjectDropdownExpanded = false
                                            val defaultTeacher = resolveDefaultTeacher(subjName)
                                            if (!defaultTeacher.isNullOrBlank()) {
                                                teacher = defaultTeacher
                                                isTeacherAutoFilled = true
                                            } else {
                                                val teachers = FacultyDatabase.getTeachersForSubject(subjName, studentYear)
                                                if (teacher.isBlank() || !teachers.contains(teacher)) {
                                                    teacher = teachers.firstOrNull() ?: ""
                                                    isTeacherAutoFilled = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom freeform text field for Subject
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {
                            subject = it
                            val defaultTeacher = resolveDefaultTeacher(it)
                            if (!defaultTeacher.isNullOrBlank()) {
                                teacher = defaultTeacher
                                isTeacherAutoFilled = true
                            }
                        },
                        label = { Text("Subject Name (Custom)", fontFamily = Lexend) },
                        placeholder = { Text("e.g. Administration & IT, Graphics or Physics", fontFamily = Lexend) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Timetable Quick Pick Suggestions (shows subjects already in timetable)
                if (timetableSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TIMETABLE PRESETS / QUICK PICKS",
                        fontFamily = Staatliches,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(timetableSuggestions) { (subSuggestion, teachSuggestion) ->
                            val isSelected = subject.equals(subSuggestion, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) (if (colors.isDark) Color(0xFF1E3A8A) else LornshillBlueContainer) else colors.chipBg)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) colors.accentCobalt else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        subject = subSuggestion
                                        teacher = teachSuggestion
                                        isTeacherAutoFilled = true
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$subSuggestion · $teachSuggestion",
                                    fontFamily = Lexend,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.accentCobalt else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Faculty Tag Indicator
                if (currentFaculty != null && !isCustomMode) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (colors.isDark) colors.cardSecondaryBg else Color(0xFFEFF6FF))
                            .border(1.dp, if (colors.isDark) colors.accentCobalt.copy(alpha = 0.5f) else Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentFaculty.emoji} Faculty: ${currentFaculty.name}",
                            fontFamily = Lexend,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accentCobalt
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Teacher Selection
                if (!isCustomMode) {
                    // Dropdown for Teacher (filtered by faculty of selected subject)
                    ExposedDropdownMenuBox(
                        expanded = teacherDropdownExpanded,
                        onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = teacher,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    if (currentFaculty != null) "Teacher (${currentFaculty.name})" else "Teacher",
                                    fontFamily = Lexend
                                )
                            },
                            placeholder = { Text("Choose a teacher...", fontFamily = Lexend) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accentCobalt,
                                unfocusedBorderColor = colors.cardBorder
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = teacherDropdownExpanded,
                            onDismissRequest = { teacherDropdownExpanded = false }
                        ) {
                            if (availableTeachers.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Select a subject first", fontFamily = Lexend, color = colors.textSecondary) },
                                    onClick = { teacherDropdownExpanded = false }
                                )
                            } else {
                                availableTeachers.forEach { teacherName ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "👤 $teacherName",
                                                    fontFamily = Lexend,
                                                    fontSize = 13.5.sp,
                                                    fontWeight = if (teacher == teacherName) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (teacher == teacherName) colors.accentCobalt else colors.textPrimary
                                                )
                                            }
                                        },
                                        onClick = {
                                            teacher = teacherName
                                            isTeacherAutoFilled = false
                                            teacherDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Custom freeform text field for Teacher
                    OutlinedTextField(
                        value = teacher,
                        onValueChange = {
                            teacher = it
                            isTeacherAutoFilled = false
                        },
                        label = { Text("Teacher (Custom)", fontFamily = Lexend) },
                        placeholder = { Text("e.g. Ms McCulloch or Mr Watson", fontFamily = Lexend) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Auto-default indicator when teacher matches timetable default
                val matchedTimetableTeacher = resolveDefaultTeacher(subject)
                if (teacher.isNotBlank() && matchedTimetableTeacher.equals(teacher, ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (colors.isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFD1FAE5))
                            .border(1.dp, if (colors.isDark) Color(0xFF059669).copy(alpha = 0.6f) else Color(0xFFA7F3D0), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colors.isDark) Color(0xFF34D399) else Color(0xFF059669),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Defaulted to $teacher (from your timetable)",
                                fontFamily = Lexend,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (colors.isDark) Color(0xFF34D399) else Color(0xFF065F46)
                            )
                        }
                    }
                }

                // Error message if attempting to set Break or Lunch
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Prominent Primary "Save" Button at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            val cleanSubject = subject.trim()
                            if (cleanSubject.equals("Break", ignoreCase = true) ||
                                cleanSubject.equals("Interval", ignoreCase = true) ||
                                cleanSubject.equals("Morning Break", ignoreCase = true) ||
                                cleanSubject.equals("Lunch", ignoreCase = true)
                            ) {
                                errorMessage = "Break and Lunch are fixed school periods and cannot be set as class lessons."
                                return@clickable
                            }
                            val updated = period.copy(
                                subject = cleanSubject.ifBlank { "Study Period" },
                                teacher = teacher.trim(),
                                room = period.room.ifBlank {
                                    if (cleanSubject.isNotBlank()) {
                                        FacultyDatabase.suggestRoomForSubject(cleanSubject, studentYear)
                                    } else ""
                                }
                            )
                            onSave(updated)
                            onDismiss()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save",
                            fontFamily = Lexend,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Actions: Set Free Period and Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onClear()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Lesson",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set as Free Period",
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFDC2626)
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontFamily = Lexend, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
