package com.example.simplebutton.wear.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.example.simplebutton.wear.sync.WatchSyncManager
import com.example.simplebutton.wear.ui.theme.WatchAmber
import com.example.simplebutton.wear.ui.theme.WatchCobalt
import com.example.simplebutton.wear.ui.theme.WatchCyan
import com.example.simplebutton.wear.ui.theme.WatchEmerald
import com.example.simplebutton.wear.ui.theme.WatchPureBlack
import com.example.simplebutton.wear.ui.theme.WatchRose
import com.example.simplebutton.wear.ui.theme.WatchSurfaceCard
import com.example.simplebutton.wear.ui.theme.WatchTextMuted
import com.example.simplebutton.wear.ui.theme.WatchTextPrimary
import com.example.simplebutton.wear.ui.theme.WatchTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WatchSettingsScreen(
    repository: WatchTimetableRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()

    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusMessage by remember { mutableStateOf<String?>(null) }
    var showPresetPicker by remember { mutableStateOf(false) }

    val profile = repository.userProfile.value
    val lastSync = repository.lastSyncTime.value

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
        modifier = Modifier.fillMaxSize().background(WatchPureBlack)
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "SETTINGS & SYNC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WatchCyan,
                    letterSpacing = 1.sp
                )
            }

            // Student Profile Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(WatchSurfaceCard)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = profile.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WatchTextPrimary
                        )
                        Text(
                            text = profile.formattedBadge.ifBlank { "Lornshill Academy" },
                            fontSize = 11.sp,
                            color = WatchCyan
                        )
                        if (lastSync > 0L) {
                            val timeStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(lastSync))
                            Text(
                                text = "Last synced: $timeStr",
                                fontSize = 9.sp,
                                color = WatchTextMuted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else {
                            Text(
                                text = "Not synced with phone yet",
                                fontSize = 9.sp,
                                color = WatchAmber,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            if (syncStatusMessage != null) {
                item {
                    Text(
                        text = syncStatusMessage!!,
                        fontSize = 10.sp,
                        color = WatchEmerald,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )
                }
            }

            // Action: Sync from Phone
            item {
                Chip(
                    onClick = {
                        if (!isSyncing) {
                            isSyncing = true
                            syncStatusMessage = "Connecting to phone..."
                            scope.launch {
                                val result = WatchSyncManager.requestSyncFromPhone(context)
                                isSyncing = false
                                if (result.isSuccess) {
                                    syncStatusMessage = "Sync request sent to phone!"
                                    Toast.makeText(context, "Requested timetable sync from phone", Toast.LENGTH_SHORT).show()
                                } else {
                                    syncStatusMessage = "Phone unreachable. Use Standalone Presets below."
                                    Toast.makeText(context, "Cannot connect to phone. Make sure Bluetooth is on.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    label = {
                        if (isSyncing) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(14.dp).width(14.dp),
                                    strokeWidth = 2.dp,
                                    indicatorColor = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", fontSize = 11.sp)
                            }
                        } else {
                            Text("Sync from Phone", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    secondaryLabel = {
                        Text("Fetch latest mobile timetable", fontSize = 9.sp, color = WatchTextSecondary)
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = WatchCobalt,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }

            // Action: Open Mobile App
            item {
                Chip(
                    onClick = {
                        scope.launch {
                            val result = WatchSyncManager.openAppOnPhone(context)
                            if (result.isSuccess) {
                                Toast.makeText(context, "Opening Lornshill on phone...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Could not reach phone", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    label = { Text("Open on Phone", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    secondaryLabel = { Text("Edit on larger screen", fontSize = 9.sp, color = WatchTextSecondary) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }

            // Action: Presets (Offline / Standalone)
            if (!showPresetPicker) {
                item {
                    Chip(
                        onClick = { showPresetPicker = true },
                        label = { Text("Load Preset Timetable", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        secondaryLabel = { Text("Offline S1, S2, S3+ presets", fontSize = 9.sp, color = WatchTextSecondary) },
                        colors = ChipDefaults.secondaryChipColors(
                            backgroundColor = WatchSurfaceCard,
                            contentColor = WatchTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )
                }
            } else {
                item {
                    Text(
                        text = "SELECT YEAR PRESET",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WatchCyan,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                listOf("S1", "S2", "S3+", "Jayden").forEach { presetKey ->
                    item {
                        CompactChip(
                            onClick = {
                                val target = if (presetKey == "S3+") "S3" else presetKey
                                repository.loadPreset(target)
                                showPresetPicker = false
                                Toast.makeText(context, "Loaded $presetKey Preset!", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("$presetKey Timetable", fontSize = 11.sp) },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = WatchCobalt.copy(alpha = 0.4f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
                        )
                    }
                }
            }

            // Action: Clear Timetable
            item {
                CompactChip(
                    onClick = {
                        repository.clearTimetable()
                        Toast.makeText(context, "Watch timetable cleared", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("Clear Timetable", fontSize = 10.sp, color = WatchRose) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchRose
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Back button
            item {
                CompactChip(
                    onClick = onBack,
                    label = { Text("Done", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = ChipDefaults.secondaryChipColors(
                        backgroundColor = WatchSurfaceCard,
                        contentColor = WatchTextPrimary
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
