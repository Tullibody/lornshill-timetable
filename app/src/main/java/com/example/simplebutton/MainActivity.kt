package com.example.simplebutton

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.simplebutton.notifications.AppLifecycleTracker
import com.example.simplebutton.notifications.TimetableNotificationReceiver
import com.example.simplebutton.notifications.TimetableNotificationScheduler
import com.example.simplebutton.model.FacultyDatabase
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.ui.components.OnboardingTourOverlay
import com.example.simplebutton.ui.components.TourStep
import com.example.simplebutton.remote.RemoteConfigManager
import com.example.simplebutton.ui.screens.AppUpdateDialog
import com.example.simplebutton.ui.screens.DevPanelScreen
import com.example.simplebutton.ui.screens.EditPeriodDialog
import com.example.simplebutton.ui.screens.HomeScreen
import com.example.simplebutton.ui.screens.LunchScreen
import com.example.simplebutton.ui.screens.NotificationDialog
import com.example.simplebutton.ui.screens.RemoteNotificationDialog
import com.example.simplebutton.ui.screens.SettingsScreen
import com.example.simplebutton.ui.screens.TimetableScreen
import com.example.simplebutton.ui.screens.WelcomeOnboardingScreen
import com.example.simplebutton.ui.theme.Lexend
import com.example.simplebutton.ui.theme.LornshillBackground
import com.example.simplebutton.ui.theme.LornshillBlueContainer
import com.example.simplebutton.ui.theme.LornshillCanvasBg
import com.example.simplebutton.ui.theme.LornshillCobalt
import com.example.simplebutton.ui.theme.LornshillNavy
import com.example.simplebutton.ui.theme.LornshillTextMuted
import com.example.simplebutton.ui.theme.LornshillTheme
import com.example.simplebutton.ui.theme.appColors
import kotlinx.coroutines.delay
import java.time.DayOfWeek

private const val EXTRA_SHOW_NOTIFICATION_POPUP = "EXTRA_SHOW_POPUP"

enum class AppTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    TIMETABLE("Timetable", Icons.Default.DateRange),
    LUNCH("Lunch", Icons.Default.Star),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: TimetableRepository
    private val showNotificationPopup = mutableStateOf(false)
    private val notificationPopupSubject = mutableStateOf<String?>(null)
    private val notificationPopupTeacher = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure edge-to-edge status bar so dark blue gradient headers flow behind the status bar
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        TimetableNotificationReceiver.createNotificationChannels(applicationContext)
        repository = TimetableRepository(applicationContext)

        // Silently sync from Google Sheet if auto-sync on launch is enabled
        lifecycleScope.launch {
            FacultyDatabase.autoSyncIfConfigured(applicationContext)
        }

        // Asynchronously check for remote APK update and in-app notices
        lifecycleScope.launch {
            RemoteConfigManager.fetchAndCheck(applicationContext)
        }

        // Schedule background notifications only if notifications are enabled
        if (repository.notificationsEnabled.value) {
            TimetableNotificationScheduler.scheduleAll(applicationContext, repository)
        }

        checkNotificationIntent(intent)

        setContent {
            val themeMode by repository.themeMode
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> systemDark
            }

            androidx.compose.runtime.SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = !isDark
                }
            }

            LornshillTheme(darkTheme = isDark) {
                MainAppContainer(
                    activity = this,
                    repository = repository,
                    showNotificationPopup = showNotificationPopup.value,
                    popupSubject = notificationPopupSubject.value,
                    popupTeacher = notificationPopupTeacher.value,
                    onDismissNotificationPopup = {
                        showNotificationPopup.value = false
                        notificationPopupSubject.value = null
                        notificationPopupTeacher.value = null
                    },
                    onSendTestNotification = { sendTestNotification() },
                    onSendTestBrief = { sendTestBriefNotification() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLifecycleTracker.isAppInForeground = true
        // When the user opens the app, clear any existing period alert notification
        NotificationManagerCompat.from(this).cancel(TimetableNotificationReceiver.NOTIFICATION_ID_PERIOD)
    }

    override fun onStop() {
        super.onStop()
        AppLifecycleTracker.isAppInForeground = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkNotificationIntent(intent)
    }

    private fun checkNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_SHOW_NOTIFICATION_POPUP, false) == true) {
            notificationPopupSubject.value = intent.getStringExtra(TimetableNotificationReceiver.EXTRA_SUBJECT)
            notificationPopupTeacher.value = intent.getStringExtra(TimetableNotificationReceiver.EXTRA_TEACHER)
            showNotificationPopup.value = true

            // Clear the extras so recreating or reopening the activity does not re-trigger the popup
            intent.removeExtra(EXTRA_SHOW_NOTIFICATION_POPUP)
            intent.removeExtra(TimetableNotificationReceiver.EXTRA_SUBJECT)
            intent.removeExtra(TimetableNotificationReceiver.EXTRA_TEACHER)
        }
    }

    fun sendTestNotification() {
        val (currentPeriod, nextPeriod) = repository.getCurrentAndNextPeriod(
            repository.getEffectiveTime(),
            repository.getEffectiveDay()
        )
        val target = nextPeriod ?: currentPeriod
        val subject = target?.subject?.takeIf { it.isNotBlank() } ?: "Biology"
        val teacher = target?.teacher?.takeIf { it.isNotBlank() } ?: "Mr Thomas"

        val alertIntent = Intent(this, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_TRIGGER_PERIOD
            putExtra(TimetableNotificationReceiver.EXTRA_SUBJECT, subject)
            putExtra(TimetableNotificationReceiver.EXTRA_TEACHER, teacher)
            putExtra(TimetableNotificationReceiver.EXTRA_IS_TEST, true)
        }
        sendBroadcast(alertIntent)
        Toast.makeText(this, "Notification sent: $subject Starts in 5 minutes ($teacher)", Toast.LENGTH_SHORT).show()
    }

    fun sendTestBriefNotification() {
        val briefIntent = Intent(this, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_TRIGGER_BRIEF
            putExtra(TimetableNotificationReceiver.EXTRA_IS_TEST, true)
        }
        sendBroadcast(briefIntent)
        Toast.makeText(this, "Morning Daily Brief notification dispatched!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MainAppContainer(
    activity: MainActivity,
    repository: TimetableRepository,
    showNotificationPopup: Boolean,
    popupSubject: String? = null,
    popupTeacher: String? = null,
    onDismissNotificationPopup: () -> Unit,
    onSendTestNotification: () -> Unit,
    onSendTestBrief: () -> Unit
) {
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var inDevPanel by remember { mutableStateOf(false) }
    var editingPeriod by remember { mutableStateOf<TimetablePeriod?>(null) }
    var internalPopupTrigger by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showOnboardingTour by remember { mutableStateOf(false) }
    var tourStep by remember { mutableStateOf(TourStep.VIEW_FULL) }

    // Check actual initial permission status and saved repository preference
    val hasSystemPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    var notificationsEnabled by remember {
        mutableStateOf(repository.notificationsEnabled.value && hasSystemPermission)
    }

    // Permission launcher for Android 13+ (POST_NOTIFICATIONS)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationsEnabled = true
            repository.setNotificationsEnabled(true)
            TimetableNotificationScheduler.scheduleAll(activity, repository)
        } else {
            notificationsEnabled = false
            repository.setNotificationsEnabled(false)
            val canShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)
            } else false
            if (!canShowRationale) {
                showPermissionRationale = true
            }
        }
    }

    // Live countdown clock ticker (5s)
    var clockTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            clockTick++
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0), // Allow screen headers to flow behind status bar!
        bottomBar = {
            if (!inDevPanel && repository.userProfile.value.completedOnboarding) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = appColors().navBarBg,
                    tonalElevation = 6.dp
                ) {
                    AppTab.values().forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontFamily = Lexend,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = appColors().accentCobalt,
                                selectedTextColor = if (appColors().isDark) appColors().accentCobalt else LornshillNavy,
                                indicatorColor = appColors().navBarIndicator,
                                unselectedIconColor = appColors().textMuted,
                                unselectedTextColor = appColors().textMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(appColors().canvasBg)
        ) {
            // Screen switching
            if (inDevPanel) {
                DevPanelScreen(
                    repository = repository,
                    onTestNotification = onSendTestNotification,
                    onTestBriefNotification = onSendTestBrief,
                    onSimulateInAppPopup = { internalPopupTrigger = true },
                    onBack = { inDevPanel = false }
                )
            } else {
                when (currentTab) {
                    AppTab.HOME -> HomeScreen(
                        repository = repository,
                        onViewFullTimetable = { currentTab = AppTab.TIMETABLE },
                        onOpenSettings = { currentTab = AppTab.SETTINGS },
                        onEditPeriod = { editingPeriod = it }
                    )
                    AppTab.TIMETABLE -> TimetableScreen(
                        repository = repository,
                        onEditPeriod = { editingPeriod = it }
                    )
                    AppTab.LUNCH -> LunchScreen()
                    AppTab.SETTINGS -> SettingsScreen(
                        repository = repository,
                        notificationsEnabled = notificationsEnabled,
                        onToggleNotifications = { targetEnabled ->
                            if (targetEnabled) {
                                repository.setNotificationsEnabled(true)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        activity,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        notificationsEnabled = true
                                        TimetableNotificationScheduler.scheduleAll(activity, repository)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    notificationsEnabled = true
                                    TimetableNotificationScheduler.scheduleAll(activity, repository)
                                }
                            } else {
                                repository.setNotificationsEnabled(false)
                                notificationsEnabled = false
                                TimetableNotificationScheduler.cancelAll(activity, repository)
                            }
                        },
                        onOpenDevPanel = { inDevPanel = true },
                        showPermissionRationaleDialog = showPermissionRationale,
                        onDismissPermissionRationale = { showPermissionRationale = false },
                        onDeleteAllData = {
                            repository.deleteAllData(activity)
                            currentTab = AppTab.HOME
                            Toast.makeText(activity, "All data deleted", Toast.LENGTH_SHORT).show()
                        },
                        onReplayTour = {
                            currentTab = AppTab.HOME
                            tourStep = TourStep.VIEW_FULL
                            showOnboardingTour = true
                        }
                    )
                }
            }

            // Edit Period Dialog
            if (editingPeriod != null) {
                EditPeriodDialog(
                    period = editingPeriod!!,
                    studentYear = repository.userProfile.value.year,
                    allPeriods = repository.periods,
                    findDefaultTeacher = { sub -> repository.findDefaultTeacherForSubject(sub, editingPeriod?.id) },
                    onDismiss = { editingPeriod = null },
                    onSave = { updated ->
                        repository.addOrUpdatePeriod(updated)
                        TimetableNotificationScheduler.scheduleAll(activity, repository)
                        editingPeriod = null
                    },
                    onClear = {
                        repository.deletePeriod(editingPeriod!!.id)
                        TimetableNotificationScheduler.scheduleAll(activity, repository)
                        editingPeriod = null
                    }
                )
            }

            // Notification Popup Dialog
            if (showNotificationPopup || internalPopupTrigger) {
                val (currP, nextP) = repository.getCurrentAndNextPeriod(
                    repository.getEffectiveTime(),
                    repository.getEffectiveDay()
                )
                val targetP = nextP ?: currP
                NotificationDialog(
                    periodName = popupSubject ?: targetP?.subject?.takeIf { it.isNotBlank() } ?: "Biology",
                    teacherName = popupTeacher ?: targetP?.teacher?.takeIf { it.isNotBlank() } ?: "Mr Thomas",
                    onDismiss = {
                        onDismissNotificationPopup()
                        internalPopupTrigger = false
                    }
                )
            }

            // Remote APK Update Dialog (appears when update is available and cooldown allows)
            val activeUpdate = RemoteConfigManager.activeUpdateInfo.value
            if (activeUpdate != null && repository.userProfile.value.completedOnboarding && !inDevPanel) {
                AppUpdateDialog(
                    updateInfo = activeUpdate,
                    onUpdateNow = {
                        RemoteConfigManager.recordUpdatePromptShown(activity, activeUpdate.latestVersionCode)
                        RemoteConfigManager.activeUpdateInfo.value = null
                        RemoteConfigManager.openApkDownload(activity, activeUpdate.apkUrl)
                    },
                    onDismiss = {
                        RemoteConfigManager.recordUpdatePromptShown(activity, activeUpdate.latestVersionCode)
                        RemoteConfigManager.activeUpdateInfo.value = null
                    }
                )
            }

            // Remote Custom In-App Notice Dialog (appears if no update dialog is active)
            val activeNotice = RemoteConfigManager.activeNotification.value
            if (activeUpdate == null && activeNotice != null && repository.userProfile.value.completedOnboarding && !inDevPanel) {
                RemoteNotificationDialog(
                    notification = activeNotice,
                    onDismiss = {
                        RemoteConfigManager.dismissNotification(activity, activeNotice.id)
                    }
                )
            }

            // Animated Welcome / Onboarding Screen
            if (!repository.userProfile.value.completedOnboarding) {
                WelcomeOnboardingScreen(
                    initialProfile = repository.userProfile.value,
                    onFinishOnboarding = { newProfile ->
                        repository.saveUserProfile(newProfile)
                        TimetableNotificationScheduler.scheduleAll(activity, repository)
                        currentTab = AppTab.HOME
                        tourStep = TourStep.VIEW_FULL
                        showOnboardingTour = true
                    }
                )
            }

            // Interactive Guided Onboarding Tour Overlay
            if (showOnboardingTour && repository.userProfile.value.completedOnboarding) {
                OnboardingTourOverlay(
                    currentStep = tourStep,
                    studentName = repository.userProfile.value.name,
                    onNext = {
                        when (tourStep) {
                            TourStep.VIEW_FULL -> {
                                currentTab = AppTab.TIMETABLE
                                tourStep = TourStep.DAYS
                            }
                            TourStep.DAYS -> {
                                tourStep = TourStep.PERIODS
                            }
                            TourStep.PERIODS -> {
                                showOnboardingTour = false
                            }
                        }
                    },
                    onSkip = {
                        showOnboardingTour = false
                    }
                )
            }
        }
    }
}

