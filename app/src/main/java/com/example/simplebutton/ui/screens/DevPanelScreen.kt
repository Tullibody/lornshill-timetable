package com.example.simplebutton.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.simplebutton.remote.AppUpdateInfo
import com.example.simplebutton.remote.RemoteAppNotification
import com.example.simplebutton.remote.RemoteConfigFetchResult
import com.example.simplebutton.remote.RemoteConfigManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.GoogleSheetsFacultyReader
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillButtonGradient
import com.example.simplebutton.ui.theme.LornshillCanvasBg
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillHeaderGradient
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillSuccess
import com.example.simplebutton.ui.theme.LornshillTextMuted
import com.example.simplebutton.ui.theme.LornshillTextSecondary
import com.example.simplebutton.ui.theme.Staatliches
import com.example.simplebutton.ui.theme.appColors
import java.time.DayOfWeek
import java.time.LocalTime

@Composable
fun DevPanelScreen(
    repository: TimetableRepository,
    onTestNotification: () -> Unit,
    onTestBriefNotification: () -> Unit = {},
    onSimulateInAppPopup: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = appColors()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var notificationSentMsg by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var selectedPresetIndex by remember { mutableStateOf(0) }

    // Remote Update & In-App Notification QA States
    var previewUpdateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var previewNotification by remember { mutableStateOf<RemoteAppNotification?>(null) }
    var showConfigUrlDialog by remember { mutableStateOf(false) }
    var configUrlInput by remember { mutableStateOf(RemoteConfigManager.getConfigUrl(context)) }
    var isCheckingRemote by remember { mutableStateOf(false) }
    var cooldownResetMsg by remember { mutableStateOf(false) }
    var notifClearedMsg by remember { mutableStateOf(false) }

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
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Settings",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DEVELOPER PANEL",
                            fontFamily = Staatliches,
                            fontSize = 20.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "INTERNAL QA",
                                fontFamily = Staatliches,
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Debug tools, time simulation & feature testing",
                        fontFamily = Lexend,
                        fontSize = 12.sp,
                        color = Color(0xFFBAE6FD)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Remote Updates & In-App Notifications QA Section
            Column {
                Text(
                    text = "REMOTE UPDATES & IN-APP NOTIFICATIONS QA",
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = LornshillTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accentCobalt.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🚀", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Remote APK & Notice Diagnostics",
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "No Google Play · Independent direct APK distribution",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        HorizontalDivider(color = colors.cardSecondaryBg)

                        // Diagnostics Info
                        val installedCode = RemoteConfigManager.getInstalledVersionCode(context)
                        val installedName = RemoteConfigManager.getInstalledVersionName(context)
                        val remoteConfig = RemoteConfigManager.latestRemoteConfig.value
                        val remoteUpdate = remoteConfig?.updateInfo
                        val lastCheckTs = RemoteConfigManager.lastCheckTimestamp.value
                        val lastShownVer = RemoteConfigManager.getLastShownVersionCode(context)
                        val lastShownTs = RemoteConfigManager.getLastShownTimestamp(context)
                        val dismissedCount = RemoteConfigManager.getDismissedNotificationIds(context).size
                        val currentConfigUrl = RemoteConfigManager.getConfigUrl(context)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.cardSecondaryBg)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: Installed Version
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Installed App Version:",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "v$installedName (code: $installedCode)",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }

                            // Row 2: Latest Remote Version
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Latest Remote Version:",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = if (remoteUpdate != null) {
                                        "v${remoteUpdate.latestVersionName} (code: ${remoteUpdate.latestVersionCode})"
                                    } else {
                                        "Not fetched yet"
                                    },
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remoteUpdate != null) colors.accentCobalt else colors.textMuted
                                )
                            }

                            // Row 3: Update Detected Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Update Available?",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                val statusText: String
                                val statusColor: Color
                                if (remoteUpdate != null) {
                                    if (installedCode < remoteUpdate.latestVersionCode) {
                                        statusText = "YES (Update Detected)"
                                        statusColor = LornshillSuccess
                                    } else if (installedCode == remoteUpdate.latestVersionCode) {
                                        statusText = "NO (Up to date)"
                                        statusColor = colors.accentCobalt
                                    } else {
                                        statusText = "NO (Installed is newer / dev)"
                                        statusColor = colors.textMuted
                                    }
                                } else {
                                    statusText = "Unknown (Check required)"
                                    statusColor = colors.textMuted
                                }
                                Text(
                                    text = statusText,
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }

                            // Row 4: Remote Config Loaded Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Remote Fetch Status:",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = RemoteConfigManager.lastCheckStatus.value,
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (RemoteConfigManager.lastCheckStatus.value == "Success") LornshillSuccess else Color(0xFFEA580C)
                                )
                            }

                            // Row 5: Last Checked Time
                            if (lastCheckTs != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Last Checked:",
                                        fontFamily = Lexend,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastCheckTs))
                                    Text(
                                        text = timeStr,
                                        fontFamily = Lexend,
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            // Row 6: Weekly Cooldown Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Weekly 7-Day Cooldown:",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                val cooldownStr: String
                                if (lastShownVer > 0L) {
                                    val elapsed = System.currentTimeMillis() - lastShownTs
                                    val isCooldownActive = elapsed < RemoteConfigManager.COOLDOWN_7_DAYS_MILLIS
                                    if (isCooldownActive) {
                                        val daysRemaining = ((RemoteConfigManager.COOLDOWN_7_DAYS_MILLIS - elapsed) / (24 * 60 * 60 * 1000L)) + 1
                                        cooldownStr = "Active (~${daysRemaining}d left for v$lastShownVer)"
                                    } else {
                                        cooldownStr = "Expired (Can show v$lastShownVer)"
                                    }
                                } else {
                                    cooldownStr = "Inactive (Never shown)"
                                }
                                Text(
                                    text = cooldownStr,
                                    fontFamily = Lexend,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }

                            // Row 7: Dismissed Notifications Count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Dismissed In-App Notices:",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "$dismissedCount recorded",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        // QA Action Buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Check for Updates Now
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LornshillButtonGradient)
                                    .clickable(enabled = !isCheckingRemote) {
                                        isCheckingRemote = true
                                        coroutineScope.launch {
                                            val res = RemoteConfigManager.fetchAndCheck(context)
                                            isCheckingRemote = false
                                            when (res) {
                                                is RemoteConfigFetchResult.Success -> {
                                                    val update = res.config.updateInfo
                                                    val updateMsg = if (update != null) {
                                                        if (installedCode < update.latestVersionCode) {
                                                            "Update available: v${update.latestVersionName}!"
                                                        } else {
                                                            "App is up to date (v$installedName)."
                                                        }
                                                    } else {
                                                        "Config fetched. No update block in JSON."
                                                    }
                                                    Toast.makeText(context, "Loaded remote config! $updateMsg", Toast.LENGTH_LONG).show()
                                                }
                                                is RemoteConfigFetchResult.Error -> {
                                                    Toast.makeText(context, "Fetch failed: ${res.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCheckingRemote) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Check for Updates & Notices Now",
                                            fontFamily = Lexend,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // 2. Test Update Popup Dialog
                            OutlinedButton(
                                onClick = {
                                    previewUpdateInfo = remoteUpdate ?: AppUpdateInfo(
                                        latestVersionCode = 12,
                                        latestVersionName = "1.2.0",
                                        apkUrl = "https://example.com/LornshillTimetable.apk",
                                        whatsNew = "Added timetable notifications, improved the timetable editor and fixed several bugs."
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Preview Update Popup Dialog",
                                    fontFamily = Lexend,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentCobalt
                                )
                            }

                            // 3. Test Custom Notification Popup (Standard)
                            OutlinedButton(
                                onClick = {
                                    val candidate = remoteConfig?.notifications?.firstOrNull()
                                    previewNotification = candidate ?: RemoteAppNotification(
                                        id = "qa-notice-standard",
                                        enabled = true,
                                        title = "Timetable Update",
                                        message = "Your timetable has been updated. Please check your periods and make sure everything looks correct.",
                                        buttonText = "Close"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Preview In-App Notice (Standard)",
                                    fontFamily = Lexend,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }

                            // 4. Test Custom Notification Popup (Long Scrollable Text)
                            OutlinedButton(
                                onClick = {
                                    previewNotification = RemoteAppNotification(
                                        id = "qa-notice-long-test",
                                        enabled = true,
                                        title = "Term 2 Schedule & Room Changes",
                                        message = "Important announcement for all students across S1–S6:\n\nDue to scheduled renovations in the Science wing and West Hall, the following room and class adjustments are taking effect starting next Monday:\n\n1. Science Lab 2 classes with Mr Richards are temporarily relocated to Tech Workshop 2.\n\n2. PE Core classes will assemble in the Main Games Hall rather than the Astro turf during wet weather.\n\n3. English Room 15 with Mr Ryder is temporarily relocated to Room 18 for periods 3 and 4.\n\n4. Digital Technologies IT Suite 1 has been upgraded with new software for Graphic Communication and Administration & IT classes.\n\nPlease double-check your timetable periods and reach out to your House Guidance base (Devon, Forebraes, Grange, Ochil) if you have any questions or period clashes.\n\nThank you for your cooperation and patience!",
                                        buttonText = "Understood",
                                        actionUrl = "https://example.com/school-notice",
                                        actionText = "View School Notice Online"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Preview In-App Notice (Long Scrollable Text)",
                                    fontFamily = Lexend,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }

                            // 5. Reset Weekly Cooldown
                            OutlinedButton(
                                onClick = {
                                    RemoteConfigManager.resetUpdateCooldown(context)
                                    cooldownResetMsg = true
                                    Toast.makeText(context, "Weekly update cooldown reset!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Reset Weekly Update Cooldown",
                                    fontFamily = Lexend,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFB45309)
                                )
                            }

                            if (cooldownResetMsg) {
                                Text(
                                    text = "Cooldown cleared! Update popup can now show on next app launch.",
                                    fontFamily = Lexend,
                                    fontSize = 11.5.sp,
                                    color = LornshillSuccess,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            // 6. Clear Dismissed Notifications
                            OutlinedButton(
                                onClick = {
                                    RemoteConfigManager.clearDismissedNotifications(context)
                                    notifClearedMsg = true
                                    Toast.makeText(context, "Dismissed notifications cleared!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Clear Dismissed Notices History",
                                    fontFamily = Lexend,
                                    fontSize = 12.5.sp,
                                    color = colors.textSecondary
                                )
                            }

                            if (notifClearedMsg) {
                                Text(
                                    text = "Dismissed notices cleared! Remote announcements will show again.",
                                    fontFamily = Lexend,
                                    fontSize = 11.5.sp,
                                    color = LornshillSuccess,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            // 7. Configure Remote JSON URL
                            OutlinedButton(
                                onClick = {
                                    configUrlInput = RemoteConfigManager.getConfigUrl(context)
                                    showConfigUrlDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = colors.accentCobalt
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Configure Remote JSON URL",
                                    fontFamily = Lexend,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.accentCobalt
                                )
                            }
                        }
                    }
                }
            }

            // Notification Testing Section
            Column {
                Text(
                    text = "NOTIFICATION TESTING",
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = LornshillTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accentCobalt.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = colors.accentCobalt,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Period Notifications",
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Fires system notification with tap intent.",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LornshillButtonGradient)
                                .clickable {
                                    onTestNotification()
                                    notificationSentMsg = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Test Next Period Notification",
                                    fontFamily = Lexend,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (notificationSentMsg) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LornshillSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Notification dispatched! Check system notification bar.",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = LornshillSuccess,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onTestBriefNotification,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Test Morning Daily Brief Notification",
                                fontFamily = Lexend,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accentCobalt
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onSimulateInAppPopup,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Preview In-App Popup Dialog Directly",
                                fontFamily = Lexend,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Time & Countdown Simulation Section
            Column {
                Text(
                    text = "TIME & COUNTDOWN SIMULATION",
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = LornshillTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Jump the clock to test period countdown badges, 'NOW' state, and break intervals live on the Home screen:",
                            fontFamily = Lexend,
                            fontSize = 12.5.sp,
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )

                        val currentSimTime = repository.simulatedTime.value
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.cardSecondaryBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = colors.accentCobalt,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentSimTime != null) {
                                        "Clock: ${String.format("%02d:%02d", currentSimTime.hour, currentSimTime.minute)} (Monday Schedule)"
                                    } else {
                                        "Clock: Real Device Time"
                                    },
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentCobalt
                                )
                            }
                        }

                        SimTimeButton(
                            label = "🌅 07:45 AM — 'Morning Daily Brief Active'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(7, 45), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "08:42 AM — 'Starts in 18 minutes'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(8, 42), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "08:55 AM — 'Starts in 5 minutes'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(8, 55), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "09:00 AM — 'Starting now'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(9, 0), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "09:20 AM — 'NOW: Mathematics (In Progress)'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(9, 20), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "10:45 AM — 'NOW: Morning Break'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(10, 45), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "12:50 PM — 'NOW: Lunch'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(12, 50), DayOfWeek.MONDAY) }
                        )

                        SimTimeButton(
                            label = "15:30 PM — 'All lessons completed today'",
                            onClick = { repository.setSimulatedTime(LocalTime.of(15, 30), DayOfWeek.MONDAY) }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { repository.resetSimulation() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentCobalt),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset to Real Device Clock", fontFamily = Lexend, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Onboarding Flow Testing Section
            Column {
                Text(
                    text = "ONBOARDING & FIRST RUN",
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = LornshillTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Reset the onboarding status and wipe all timetable data to test the smooth animated Welcome screen from a completely clean slate:",
                            fontFamily = Lexend,
                            fontSize = 12.5.sp,
                            color = colors.textSecondary
                        )
                        OutlinedButton(
                            onClick = {
                                repository.clearTimetable()
                                repository.resetOnboarding()
                                onBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Onboarding & Timetable Data", fontFamily = Lexend, color = colors.accentCobalt, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Faculty & Google Sheets QA
            Column {
                val context = LocalContext.current
                Text(
                    text = "FACULTY & GOOGLE SHEETS QA",
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = LornshillTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Current Database State:",
                            fontFamily = Lexend,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "• ${FacultyDatabase.faculties.size} Faculties loaded\n• ${FacultyDatabase.totalTeachersCount} Teachers loaded\n• Source: ${FacultyDatabase.syncSource.value}\n• Status: ${if (FacultyDatabase.isCustomLoaded.value) "Custom Sync Active" else "Default Preset"}",
                            fontFamily = Lexend,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = {
                                val faculties = GoogleSheetsFacultyReader.parseCsv(GoogleSheetsFacultyReader.SAMPLE_CSV)
                                FacultyDatabase.applyCustomFaculties(context, faculties, "QA Sample CSV")
                                Toast.makeText(context, "Loaded QA Sample Sheet (${faculties.size} faculties)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simulate Google Sheet Sync (Sample Roster)", fontFamily = Lexend, color = colors.accentCobalt, fontWeight = FontWeight.Bold)
                        }

                        if (FacultyDatabase.isCustomLoaded.value) {
                            OutlinedButton(
                                onClick = {
                                    FacultyDatabase.resetToDefaults(context)
                                    Toast.makeText(context, "Restored built-in default roster", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580C))
                            ) {
                                Text("Reset to Built-in Defaults", fontFamily = Lexend, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Preset Timetables QA & Restore
            Column {
                Text(
                    text = "PRESET TIMETABLES",
                    fontFamily = Staatliches,
                    fontSize = 14.sp,
                    color = LornshillTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.accentCobalt.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = colors.accentCobalt,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Restore Preset Timetable",
                                    fontFamily = Lexend,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Load pre-configured student schedule presets",
                                    fontFamily = Lexend,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Text(
                            text = "Restore verified student timetable data from school reports. This will populate all 31 periods and update student profile info.",
                            fontFamily = Lexend,
                            fontSize = 12.5.sp,
                            color = colors.textSecondary,
                            lineHeight = 18.sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LornshillButtonGradient)
                                .clickable { showPresetDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Restore to a Preset Timetable...",
                                    fontFamily = Lexend,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showPresetDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            title = {
                Text(
                    text = "RESTORE PRESET TIMETABLE",
                    fontFamily = Staatliches,
                    fontSize = 20.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Choose a preset timetable schedule to restore to your app:",
                        fontFamily = Lexend,
                        fontSize = 12.5.sp,
                        color = colors.textSecondary
                    )

                    // Option 0: Jayden Martin Tully (S3)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (colors.isDark) Color(0xFF1E3A8A).copy(alpha = 0.35f) else Color(0xFFEFF6FF))
                            .border(
                                width = 1.5.dp,
                                color = colors.accentCobalt,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedPresetIndex = 0 }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Jayden Martin Tully",
                                        fontFamily = Lexend,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Stage: S3 · Register Group: 3chil (Ochil House)",
                                        fontFamily = Lexend,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.accentCobalt
                                    )
                                }
                                RadioButton(
                                    selected = selectedPresetIndex == 0,
                                    onClick = { selectedPresetIndex = 0 },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = colors.accentCobalt
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• Full 31-period CfE schedule (Mon 7, Tue–Fri 6)\n• Core P.E: Mr. Green (Mon P4), Mr Hillis (Fri P5)\n• Administration & IT (Ms McCulloch), Practical Woodworking (Mr Watson)\n• Physics (Mr Munro-Faure), Mathematics (Mrs Young)\n• Graphic Communication (Mr Imlay), English (Mr Ryder)\n• Geography (Mrs Lawrence & Mr Ross), Personal & Social Education (Mrs Dixon)",
                                fontFamily = Lexend,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            if (selectedPresetIndex == 0) {
                                repository.loadJaydenPreset()
                                Toast.makeText(
                                    context,
                                    "Restored Jayden Tully's S3 Timetable!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            showPresetDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Restore Timetable",
                        fontFamily = Lexend,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = Lexend,
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = colors.cardBg
        )
    }

    // Preview App Update Dialog
    if (previewUpdateInfo != null) {
        AppUpdateDialog(
            updateInfo = previewUpdateInfo!!,
            onUpdateNow = {
                RemoteConfigManager.openApkDownload(context, previewUpdateInfo!!.apkUrl)
                previewUpdateInfo = null
            },
            onDismiss = { previewUpdateInfo = null }
        )
    }

    // Preview Custom In-App Notification Dialog
    if (previewNotification != null) {
        RemoteNotificationDialog(
            notification = previewNotification!!,
            onDismiss = { previewNotification = null }
        )
    }

    // Configure Remote JSON URL Dialog
    if (showConfigUrlDialog) {
        AlertDialog(
            onDismissRequest = { showConfigUrlDialog = false },
            title = {
                Text(
                    text = "REMOTE CONFIG URL",
                    fontFamily = Staatliches,
                    fontSize = 18.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the raw URL pointing to your online app-config.json file:",
                        fontFamily = Lexend,
                        fontSize = 12.5.sp,
                        color = colors.textSecondary
                    )
                    OutlinedTextField(
                        value = configUrlInput,
                        onValueChange = { configUrlInput = it },
                        label = { Text("Config JSON URL", fontFamily = Lexend, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(LornshillButtonGradient)
                        .clickable {
                            RemoteConfigManager.setConfigUrl(context, configUrlInput)
                            showConfigUrlDialog = false
                            Toast.makeText(context, "Config URL updated!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Save URL", fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            RemoteConfigManager.resetConfigUrl(context)
                            configUrlInput = RemoteConfigManager.getConfigUrl(context)
                            showConfigUrlDialog = false
                            Toast.makeText(context, "Reset to default URL", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Reset Default", fontFamily = Lexend, color = colors.textSecondary, fontSize = 12.sp)
                    }
                    TextButton(onClick = { showConfigUrlDialog = false }) {
                        Text("Cancel", fontFamily = Lexend, color = colors.textSecondary, fontSize = 12.sp)
                    }
                }
            },
            shape = RoundedCornerShape(22.dp),
            containerColor = colors.cardBg
        )
    }
}

@Composable
private fun SimTimeButton(
    label: String,
    onClick: () -> Unit
) {
    val colors = appColors()
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary)
    ) {
        Text(text = label, fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
