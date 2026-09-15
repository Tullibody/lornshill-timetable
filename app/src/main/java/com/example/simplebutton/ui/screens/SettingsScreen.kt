package com.example.simplebutton.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.GoogleSheetsFacultyReader
import com.example.simplebutton.model.SheetSyncResult
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.DailyBriefManager
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.model.UserProfile
import com.example.simplebutton.ui.components.DailyBriefCard
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCanvasBg
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillHeaderGradient
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillTextMuted
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors
import com.example.simplebutton.ui.theme.getHouseColor
import com.example.simplebutton.ui.theme.getHouseContentColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    repository: TimetableRepository,
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onOpenDevPanel: () -> Unit,
    showPermissionRationaleDialog: Boolean = false,
    onDismissPermissionRationale: () -> Unit = {},
    onDeleteAllData: () -> Unit = {},
    onReplayTour: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val context = LocalContext.current
    var showCodeDialog by remember { mutableStateOf(false) }
    var enteredCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showBriefTimeDialog by remember { mutableStateOf(false) }
    var showBriefPreviewDialog by remember { mutableStateOf(false) }
    var showDirectoryDialog by remember { mutableStateOf(false) }
    var showFacultyInfoDialog by remember { mutableStateOf(false) }
    var showSheetConfigDialog by remember { mutableStateOf(false) }
    var isSyncingSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val userProfile = repository.userProfile.value
    val dailyBriefEnabled = repository.dailyBriefEnabled.value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBg)
    ) {
        // Gradient Header extending into Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LornshillHeaderGradient)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = "SETTINGS",
                    fontFamily = Staatliches,
                    fontSize = 20.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Student Profile, Theme, Daily Brief & Preferences",
                    fontFamily = Lexend,
                    fontSize = 12.sp,
                    color = Color(0xFFBAE6FD)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Theme & Display Section
            SettingsSection(title = "THEME & DISPLAY") {
                val currentThemeMode = repository.themeMode.value
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accentCobalt.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (colors.isDark) "🌙" else "☀️",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "App Theme",
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Choose light, dark, or follow device system theme",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3-way Segmented Button: System, Light, Dark
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.cardSecondaryBg)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "system" to "📱 System",
                                "light" to "☀️ Light",
                                "dark" to "🌙 Dark"
                            ).forEach { (mode, label) ->
                                val isSelected = currentThemeMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            if (isSelected) {
                                                if (colors.isDark) Color(0xFF1E3A8A) else LornshillCobalt
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .clickable { repository.setThemeMode(mode) }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = Lexend,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Student Profile Section
            SettingsSection(title = "STUDENT PROFILE") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Person,
                            title = if (userProfile.name.isNotBlank()) userProfile.name else "Student Name (Optional)",
                            subtitle = if (userProfile.formattedBadge.isNotBlank()) {
                                "${userProfile.formattedBadge} · Editable at any time"
                            } else {
                                "Set your year group & house · Editable anytime"
                            },
                            onClick = { showProfileDialog = true }
                        )

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "Quick App Tour",
                            subtitle = "Replay the guided walkthrough for the timetable",
                            tintColor = colors.accentCobalt,
                            onClick = onReplayTour
                        )
                    }
                }
            }

            // Morning Daily Brief Section
            SettingsSection(title = "MORNING DAILY BRIEF") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Enable Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (colors.isDark) Color(0xFF78350F) else Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🌅", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Daily Morning Brief",
                                        fontFamily = Lexend,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Summary of lessons, P.E. kit, core classes & doubles.",
                                        fontFamily = Lexend,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = dailyBriefEnabled,
                                onCheckedChange = { isChecked ->
                                    repository.updateDailyBriefSettings(
                                        enabled = isChecked,
                                        sHour = repository.dailyBriefStartHour.value,
                                        sMin = repository.dailyBriefStartMin.value,
                                        eHour = repository.dailyBriefEndHour.value,
                                        eMin = repository.dailyBriefEndMin.value
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.accentCobalt
                                )
                            )
                        }

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // Brief Delivery Window Row
                        val startStr = String.format("%02d:%02d", repository.dailyBriefStartHour.value, repository.dailyBriefStartMin.value)
                        val endStr = String.format("%02d:%02d", repository.dailyBriefEndHour.value, repository.dailyBriefEndMin.value)
                        SettingsRow(
                            icon = Icons.Default.DateRange,
                            title = "Delivery Window",
                            subtitle = "Active between $startStr and $endStr every morning",
                            onClick = { showBriefTimeDialog = true }
                        )

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // Preview Daily Brief Row
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "Preview Daily Brief",
                            subtitle = "View current morning schedule overview now",
                            tintColor = Color(0xFF0284C7),
                            onClick = { showBriefPreviewDialog = true }
                        )
                    }
                }
            }

            // Notifications Section
            SettingsSection(title = "PERIOD NOTIFICATIONS") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.blueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = colors.accentCobalt,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Enable Notifications",
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Get notified about upcoming periods.",
                                    fontFamily = Lexend,
                                    fontSize = 12.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { onToggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.accentCobalt
                            )
                        )
                    }
                }
            }

            // Timetable & App Data Section
            SettingsSection(title = "TIMETABLE & APP DATA") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Delete,
                            title = "Clear All Lessons",
                            subtitle = "Remove scheduled lessons to start fresh",
                            tintColor = Color(0xFFEA580C),
                            onClick = { showClearConfirm = true }
                        )

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        SettingsRow(
                            icon = Icons.Default.Delete,
                            title = "Delete All Data",
                            subtitle = "Reset app, erase profile & return to onboarding",
                            tintColor = Color(0xFFDC2626),
                            onClick = { showDeleteAllConfirm = true }
                        )
                    }
                }
            }

            // Faculty & Teacher Directory Section
            SettingsSection(title = "FACULTY & TEACHER DIRECTORY") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Summary status row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.blueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🏛️", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "${FacultyDatabase.faculties.size} Faculties · ${FacultyDatabase.totalTeachersCount} Teachers",
                                        fontFamily = Lexend,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (FacultyDatabase.isCustomLoaded.value) "Synced: ${FacultyDatabase.syncSource.value} (${FacultyDatabase.lastSyncTime.value})" else "Built-in Scottish Secondary School roster",
                                        fontFamily = Lexend,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (FacultyDatabase.isCustomLoaded.value) colors.accentCobalt.copy(alpha = 0.15f) else colors.chipBg)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (FacultyDatabase.isCustomLoaded.value) "CUSTOM SYNCED" else "BUILT-IN",
                                    fontFamily = Lexend,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (FacultyDatabase.isCustomLoaded.value) colors.accentCobalt else colors.textMuted
                                )
                            }
                        }

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // 1-Tap Sync from Google Sheet
                        SettingsRow(
                            icon = Icons.Default.Refresh,
                            title = if (isSyncingSheet) "Syncing from Google Sheet..." else "Sync from Google Sheet",
                            subtitle = if (isSyncingSheet) "Downloading and parsing roster..." else "One-tap sync with the configured online sheet",
                            tintColor = colors.accentCobalt,
                            trailingContent = if (isSyncingSheet) {
                                {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.accentCobalt
                                    )
                                }
                            } else null,
                            onClick = {
                                if (!isSyncingSheet) {
                                    isSyncingSheet = true
                                    coroutineScope.launch {
                                        val result = FacultyDatabase.syncFromConfiguredSheet(context)
                                        isSyncingSheet = false
                                        when (result) {
                                            is SheetSyncResult.Success -> {
                                                Toast.makeText(
                                                    context,
                                                    "Successfully synced ${result.totalTeachers} teachers across ${result.faculties.size} departments!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                            is SheetSyncResult.Error -> {
                                                Toast.makeText(
                                                    context,
                                                    "Sync failed: ${result.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        )

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // Sync Settings & Sheet URL / CSV
                        SettingsRow(
                            icon = Icons.Default.Share,
                            title = "Sync Settings & Sheet URL",
                            subtitle = "Change sheet URL or import direct CSV data",
                            tintColor = Color(0xFF0284C7),
                            onClick = { showSheetConfigDialog = true }
                        )

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // Auto-sync on launch Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newVal = !FacultyDatabase.autoSyncOnLaunch.value
                                    FacultyDatabase.setAutoSyncEnabled(context, newVal)
                                }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-sync on Launch",
                                    fontFamily = Lexend,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Silently fetch updated teacher roster on startup",
                                    fontFamily = Lexend,
                                    fontSize = 11.5.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Switch(
                                checked = FacultyDatabase.autoSyncOnLaunch.value,
                                onCheckedChange = { newVal ->
                                    FacultyDatabase.setAutoSyncEnabled(context, newVal)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colors.accentCobalt
                                )
                            )
                        }

                        if (FacultyDatabase.isCustomLoaded.value) {
                            androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                            SettingsRow(
                                icon = Icons.Default.Delete,
                                title = "Reset to Built-in Roster",
                                subtitle = "Clear custom import and revert to default teachers",
                                tintColor = Color(0xFFEA580C),
                                onClick = {
                                    FacultyDatabase.resetToDefaults(context)
                                    Toast.makeText(context, "Reverted to built-in school roster", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // Information Button
                        SettingsRow(
                            icon = Icons.Default.Info,
                            title = "About Faculty & Teacher Data",
                            subtitle = "Data source, tweaks & accuracy disclaimer",
                            tintColor = colors.accentCobalt,
                            onClick = { showFacultyInfoDialog = true }
                        )

                        androidx.compose.material3.HorizontalDivider(color = colors.cardSecondaryBg)

                        // Browse Directory
                        SettingsRow(
                            icon = Icons.Default.Person,
                            title = "Browse Faculty Directory",
                            subtitle = "View all teachers and departments loaded",
                            tintColor = Color(0xFF0284C7),
                            onClick = { showDirectoryDialog = true }
                        )
                    }
                }
            }

            // Developer Section
            SettingsSection(title = "DEVELOPER") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    SettingsRow(
                        icon = Icons.Default.Build,
                        title = "Dev Panel",
                        subtitle = "Access internal testing tools & time simulation",
                        onClick = {
                            enteredCode = ""
                            codeError = null
                            showCodeDialog = true
                        }
                    )
                }
            }

            // About Section
            SettingsSection(title = "ABOUT") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Lornshill Timetable App",
                            fontFamily = Lexend,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.2.0 · Scottish Secondary School Companion",
                            fontFamily = Lexend,
                            fontSize = 12.5.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Profile Dialog
    if (showProfileDialog) {
        var editName by remember { mutableStateOf(userProfile.name) }
        var editYear by remember { mutableStateOf(userProfile.year) }
        var editHouse by remember { mutableStateOf(if (userProfile.house == "Custom" || userProfile.house.isBlank()) "Devon" else userProfile.house) }
        var editClass by remember { mutableStateOf(userProfile.className) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Text(
                    text = "EDIT STUDENT PROFILE",
                    fontFamily = Staatliches,
                    fontSize = 20.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Update your details below. You can change this at any time.",
                        fontFamily = Lexend,
                        fontSize = 12.5.sp,
                        color = colors.textSecondary
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Your Name (Optional)", fontFamily = Lexend) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentCobalt,
                            unfocusedBorderColor = colors.cardBorder
                        )
                    )

                    Text(
                        text = "Year Group",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UserProfile.YEARS.forEach { yr ->
                            val selected = editYear == yr
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) LornshillCobalt else colors.chipBg)
                                    .border(1.dp, if (selected) LornshillCobalt else colors.cardBorder, RoundedCornerShape(8.dp))
                                    .clickable { editYear = yr }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = yr,
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else colors.textPrimary
                                )
                            }
                        }
                    }

                    Text(
                        text = "House",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    val houseOptions = listOf("Devon", "Forebraes", "Grange", "Ochil")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (row in houseOptions.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { h ->
                                    val selected = editHouse == h
                                    val selectedBg = getHouseColor(h)
                                    val selectedText = getHouseContentColor(h)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) selectedBg else colors.chipBg)
                                            .border(1.dp, if (selected) selectedBg else colors.cardBorder, RoundedCornerShape(8.dp))
                                            .clickable { editHouse = h }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = h,
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) selectedText else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (editYear == "S1" || editYear == "S2") {
                        OutlinedTextField(
                            value = editClass,
                            onValueChange = { editClass = it },
                            label = { Text("Class Name (Optional)", fontFamily = Lexend) },
                            placeholder = { Text("e.g. 1C1, 2D1", fontFamily = Lexend, color = colors.textMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary,
                                focusedBorderColor = colors.accentCobalt,
                                unfocusedBorderColor = colors.cardBorder
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            repository.saveUserProfile(
                                userProfile.copy(
                                    name = editName.trim(),
                                    year = editYear,
                                    house = editHouse.trim(),
                                    className = if (editYear == "S1" || editYear == "S2") editClass.trim() else "",
                                    completedOnboarding = true
                                )
                            )
                            showProfileDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Save Profile", fontFamily = Lexend, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Edit Daily Brief Delivery Window Dialog
    if (showBriefTimeDialog) {
        var startH by remember { mutableStateOf(repository.dailyBriefStartHour.value.toString()) }
        var startM by remember { mutableStateOf(String.format("%02d", repository.dailyBriefStartMin.value)) }
        var endH by remember { mutableStateOf(repository.dailyBriefEndHour.value.toString()) }
        var endM by remember { mutableStateOf(String.format("%02d", repository.dailyBriefEndMin.value)) }

        AlertDialog(
            onDismissRequest = { showBriefTimeDialog = false },
            title = {
                Text(
                    text = "BRIEF DELIVERY WINDOW",
                    fontFamily = Staatliches,
                    fontSize = 18.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify the morning time window during which the Daily Brief should appear on your home screen.",
                        fontFamily = Lexend,
                        fontSize = 12.5.sp,
                        color = colors.textSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = startH,
                            onValueChange = { if (it.length <= 2) startH = it },
                            label = { Text("Start H", fontFamily = Lexend, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = startM,
                            onValueChange = { if (it.length <= 2) startM = it },
                            label = { Text("Start M", fontFamily = Lexend, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Text(text = "to", fontFamily = Lexend, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        OutlinedTextField(
                            value = endH,
                            onValueChange = { if (it.length <= 2) endH = it },
                            label = { Text("End H", fontFamily = Lexend, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endM,
                            onValueChange = { if (it.length <= 2) endM = it },
                            label = { Text("End M", fontFamily = Lexend, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            val sh = (startH.toIntOrNull() ?: 7).coerceIn(0, 23)
                            val sm = (startM.toIntOrNull() ?: 30).coerceIn(0, 59)
                            val eh = (endH.toIntOrNull() ?: 8).coerceIn(0, 23)
                            val em = (endM.toIntOrNull() ?: 55).coerceIn(0, 59)
                            repository.updateDailyBriefSettings(
                                enabled = repository.dailyBriefEnabled.value,
                                sHour = sh,
                                sMin = sm,
                                eHour = eh,
                                eMin = em
                            )
                            showBriefTimeDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Save Window", fontFamily = Lexend, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBriefTimeDialog = false }) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Preview Daily Brief Dialog
    if (showBriefPreviewDialog) {
        val todayLessons = repository.getPeriodsForDay(repository.getEffectiveDay())
        val brief = DailyBriefManager.generateBrief(todayLessons)

        AlertDialog(
            onDismissRequest = { showBriefPreviewDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌅 ", fontSize = 20.sp)
                    Text(
                        text = "DAILY BRIEF PREVIEW",
                        fontFamily = Staatliches,
                        fontSize = 18.sp,
                        color = colors.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DailyBriefCard(
                        summary = brief,
                        onDismiss = { showBriefPreviewDialog = false }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBriefPreviewDialog = false }) {
                    Text("Close Preview", fontFamily = Lexend, fontWeight = FontWeight.Bold, color = colors.accentCobalt)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Developer Code Prompt Dialog
    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = {
                Text(
                    text = "DEVELOPER ACCESS",
                    fontFamily = Staatliches,
                    fontSize = 20.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the developer access code to open the internal testing panel.",
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = {
                            enteredCode = it
                            codeError = null
                        },
                        label = { Text("Access Code", fontFamily = Lexend) },
                        isError = codeError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (codeError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = codeError!!,
                            color = Color(0xFFDC2626),
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            if (enteredCode.trim() == "dev1") {
                                showCodeDialog = false
                                onOpenDevPanel()
                            } else {
                                codeError = "Invalid access code. Please try again."
                            }
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Unlock",
                        fontFamily = Lexend,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCodeDialog = false }) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Permission Blocked Rationale Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionRationale,
            title = {
                Text(
                    text = "NOTIFICATIONS BLOCKED",
                    fontFamily = Staatliches,
                    fontSize = 18.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Text(
                    text = "Notification permission was blocked in system settings. To receive period countdown alerts, please enable notifications in Android App Settings.",
                    fontFamily = Lexend,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            onDismissPermissionRationale()
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Open App Settings",
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissPermissionRationale) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    text = "CLEAR ALL LESSONS?",
                    fontFamily = Staatliches,
                    fontSize = 18.sp,
                    color = Color(0xFFDC2626),
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Text(
                    text = "This will clear all scheduled periods from your timetable so you can start completely fresh.",
                    fontFamily = Lexend,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.clearTimetable()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All", fontFamily = Lexend, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Delete All Data Confirmation Dialog
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Text(
                        text = "DELETE ALL DATA?",
                        fontFamily = Staatliches,
                        fontSize = 20.sp,
                        color = Color(0xFFDC2626),
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to delete all data?",
                        fontFamily = Lexend,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "This will permanently delete your student profile, scheduled lessons, daily brief settings, and app preferences.\n\nThe app will forget everything about you and restore back to the onboarding process.",
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllConfirm = false
                        onDeleteAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete Everything", fontFamily = Lexend, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Teacher & Faculty Information Dialog
    if (showFacultyInfoDialog) {
        AlertDialog(
            onDismissRequest = { showFacultyInfoDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = colors.accentCobalt
                    )
                    Text(
                        text = "TEACHER DATA INFO",
                        fontFamily = Staatliches,
                        fontSize = 20.sp,
                        color = colors.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Where is teacher data gotten from?",
                        fontFamily = Lexend,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Teacher and department data is compiled from Lornshill Academy faculty lists, subject course catalogs, and staff directories.",
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "⚠️ Manual Tweaks & Accuracy Notice",
                        fontFamily = Lexend,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "This directory has been modified with manual tweaks to support class scheduling. Because teacher assignments and department staffing can change throughout the school year, this information may occasionally be wrong or out of date.\n\nYou can always customize the subject and teacher for any period directly by tapping the lesson in your timetable.",
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 19.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFacultyInfoDialog = false }) {
                    Text("Understood", fontFamily = Lexend, fontWeight = FontWeight.Bold, color = colors.accentCobalt)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Faculty Directory Dialog
    if (showDirectoryDialog) {
        var searchQuery by remember { mutableStateOf("") }
        val filteredFaculties = remember(searchQuery, FacultyDatabase.faculties.toList()) {
            val q = searchQuery.trim().lowercase()
            if (q.isBlank()) {
                FacultyDatabase.faculties.toList()
            } else {
                FacultyDatabase.faculties.filter { fac ->
                    fac.name.lowercase().contains(q) ||
                        fac.teachers.any { it.lowercase().contains(q) } ||
                        fac.subjects.any { it.lowercase().contains(q) }
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showDirectoryDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FACULTY DIRECTORY",
                        fontFamily = Staatliches,
                        fontSize = 20.sp,
                        color = colors.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(text = "🏛️", fontSize = 20.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search teachers, subjects, faculty", fontFamily = Lexend, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = colors.textMuted)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = colors.textMuted)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Text(
                        text = "${filteredFaculties.size} departments loaded",
                        fontFamily = Lexend,
                        fontSize = 11.5.sp,
                        color = colors.textSecondary
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (filteredFaculties.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No teachers or faculties matching \"$searchQuery\"",
                                    fontFamily = Lexend,
                                    fontSize = 13.sp,
                                    color = colors.textSecondary
                                )
                            }
                        } else {
                            filteredFaculties.forEach { fac ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.cardSecondaryBg)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = fac.emoji, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = fac.name,
                                                    fontFamily = Lexend,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(colors.chipBg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${fac.teachers.size} staff",
                                                    fontFamily = Lexend,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = colors.textPrimary
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Teachers: " + fac.teachers.joinToString(", "),
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 16.sp
                                        )

                                        if (fac.subjects.isNotEmpty()) {
                                            Text(
                                                text = "Subjects: " + fac.subjects.joinToString(", "),
                                                fontFamily = Lexend,
                                                fontSize = 11.5.sp,
                                                color = colors.textSecondary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDirectoryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentCobalt),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", fontFamily = Lexend, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

    // Google Sheets Sync Configuration Dialog
    if (showSheetConfigDialog) {
        var sheetUrlInput by remember { mutableStateOf(FacultyDatabase.configuredSheetUrl.value) }
        var csvInput by remember { mutableStateOf("") }
        var activeTab by remember { mutableIntStateOf(0) }
        var dialogError by remember { mutableStateOf<String?>(null) }
        var dialogLoading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!dialogLoading) showSheetConfigDialog = false },
            title = {
                Text(
                    text = "FACULTY SYNC OPTIONS",
                    fontFamily = Staatliches,
                    fontSize = 18.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Synchronise teacher names and department rosters directly from your school's live Google Sheet or CSV.",
                        fontFamily = Lexend,
                        fontSize = 12.5.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )

                    // Tab Selector: Google Sheet URL vs Direct CSV
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cardSecondaryBg)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == 0) colors.cardBg else Color.Transparent)
                                .clickable { activeTab = 0; dialogError = null }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Google Sheet URL",
                                fontFamily = Lexend,
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 0) colors.accentCobalt else colors.textSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (activeTab == 1) colors.cardBg else Color.Transparent)
                                .clickable { activeTab = 1; dialogError = null }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Direct CSV / Paste",
                                fontFamily = Lexend,
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (activeTab == 1) colors.accentCobalt else colors.textSecondary
                            )
                        }
                    }

                    if (activeTab == 0) {
                        OutlinedTextField(
                            value = sheetUrlInput,
                            onValueChange = {
                                sheetUrlInput = it
                                dialogError = null
                            },
                            label = { Text("Google Sheet Sharing URL", fontFamily = Lexend, fontSize = 12.sp) },
                            placeholder = { Text("https://docs.google.com/spreadsheets/...", fontFamily = Lexend) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3
                        )

                        // Quick Fill Button for Demo Secondary School Sheet
                        TextButton(
                            onClick = {
                                sheetUrlInput = GoogleSheetsFacultyReader.DEFAULT_GOOGLE_SHEET_URL
                                dialogError = null
                            },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Text(
                                text = "⚡ Fill Demo Secondary School Sheet",
                                fontFamily = Lexend,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accentCobalt
                            )
                        }

                        Text(
                            text = "Ensure the sheet link has 'Anyone with the link can view' permission so the app can export CSV data.",
                            fontFamily = Lexend,
                            fontSize = 11.sp,
                            color = colors.textMuted,
                            lineHeight = 15.sp
                        )
                    } else {
                        OutlinedTextField(
                            value = csvInput,
                            onValueChange = {
                                csvInput = it
                                dialogError = null
                            },
                            label = { Text("Paste CSV Data", fontFamily = Lexend, fontSize = 12.sp) },
                            placeholder = { Text(",Teacher Name,Department\n,C Watson,Creative Arts", fontFamily = Lexend) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        TextButton(
                            onClick = {
                                csvInput = GoogleSheetsFacultyReader.SAMPLE_CSV
                                dialogError = null
                            },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Text(
                                text = "⚡ Load Scottish Preset CSV",
                                fontFamily = Lexend,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accentCobalt
                            )
                        }
                    }

                    if (dialogError != null) {
                        Text(
                            text = dialogError!!,
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable(enabled = !dialogLoading) {
                            if (activeTab == 0) {
                                val url = sheetUrlInput.trim()
                                if (url.isBlank()) {
                                    dialogError = "Please enter a valid Google Sheet URL."
                                    return@clickable
                                }
                                FacultyDatabase.updateConfiguredSheetUrl(context, url)
                                dialogLoading = true
                                coroutineScope.launch {
                                    val result = FacultyDatabase.syncFromConfiguredSheet(context)
                                    dialogLoading = false
                                    when (result) {
                                        is SheetSyncResult.Success -> {
                                            showSheetConfigDialog = false
                                            Toast.makeText(
                                                context,
                                                "Synced ${result.totalTeachers} teachers across ${result.faculties.size} departments!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        is SheetSyncResult.Error -> {
                                            dialogError = "Sync error: ${result.message}"
                                        }
                                    }
                                }
                            } else {
                                val rawCsv = csvInput.trim()
                                if (rawCsv.isBlank()) {
                                    dialogError = "Please paste or load CSV content."
                                    return@clickable
                                }
                                val faculties = GoogleSheetsFacultyReader.parseCsv(rawCsv)
                                if (faculties.isEmpty()) {
                                    dialogError = "Could not parse any departments or teachers from the CSV."
                                    return@clickable
                                }
                                FacultyDatabase.applyCustomFaculties(context, faculties, "Direct CSV Input")
                                showSheetConfigDialog = false
                                val totalTeachers = faculties.sumOf { it.teachers.size }
                                Toast.makeText(
                                    context,
                                    "Loaded ${totalTeachers} teachers across ${faculties.size} departments!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (dialogLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (activeTab == 0) "Fetch & Sync" else "Load CSV",
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSheetConfigDialog = false },
                    enabled = !dialogLoading
                ) {
                    Text("Cancel", fontFamily = Lexend, color = colors.textSecondary)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = colors.cardBg
        )
    }

}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = Staatliches,
            fontSize = 14.sp,
            color = LornshillTextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tintColor: Color = LornshillCobalt,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = appColors()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tintColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = Lexend,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = Lexend,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
