package com.example.simplebutton.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.simplebutton.MainActivity
import com.example.simplebutton.model.DailyBriefManager
import com.example.simplebutton.model.TimetableRepository
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Tracks whether the user is actively viewing the application in the foreground.
 * Used to suppress redundant system notification popups while the app is already open.
 */
object AppLifecycleTracker {
    @Volatile
    var isAppInForeground: Boolean = false
}

class TimetableNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_PERIOD_ID = "lornshill_timetable_channel"
        const val CHANNEL_BRIEF_ID = "lornshill_brief_channel"

        const val NOTIFICATION_ID_PERIOD = 1001
        const val NOTIFICATION_ID_BRIEF = 1002

        const val ACTION_TRIGGER_PERIOD = "com.example.simplebutton.ACTION_TRIGGER_PERIOD"
        const val ACTION_DISMISS_PERIOD = "com.example.simplebutton.ACTION_DISMISS_PERIOD"
        const val ACTION_TRIGGER_BRIEF = "com.example.simplebutton.ACTION_TRIGGER_BRIEF"

        const val EXTRA_SUBJECT = "extra_subject"
        const val EXTRA_TEACHER = "extra_teacher"
        const val EXTRA_PERIOD_INDEX = "extra_period_index"
        const val EXTRA_DAY_OF_WEEK = "extra_day_of_week"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_TRIGGER_TIME_MILLIS = "extra_trigger_time_millis"
        const val EXTRA_IS_TEST = "extra_is_test"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

                // 1. Period alerts channel
                val periodChannel = NotificationChannel(
                    CHANNEL_PERIOD_ID,
                    "Period Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders 5 minutes before school classes begin"
                    enableVibration(true)
                }

                // 2. Morning daily brief channel
                val briefChannel = NotificationChannel(
                    CHANNEL_BRIEF_ID,
                    "Morning Daily Brief",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Morning summary of today's schedule, kits, and classes"
                    enableVibration(true)
                }

                notificationManager.createNotificationChannel(periodChannel)
                notificationManager.createNotificationChannel(briefChannel)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannels(context)

        when (intent.action) {
            ACTION_TRIGGER_PERIOD -> {
                handlePeriodNotification(context, intent)
            }
            ACTION_DISMISS_PERIOD -> {
                handleDismissPeriodNotification(context)
            }
            ACTION_TRIGGER_BRIEF -> {
                handleBriefNotification(context, intent)
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val repository = TimetableRepository(context.applicationContext)
                TimetableNotificationScheduler.scheduleAll(context.applicationContext, repository)
            }
        }
    }

    private fun handlePeriodNotification(context: Context, intent: Intent) {
        val subject = intent.getStringExtra(EXTRA_SUBJECT)?.trim() ?: "Class"
        val rawTeacher = intent.getStringExtra(EXTRA_TEACHER)?.trim() ?: ""
        val periodIndex = intent.getIntExtra(EXTRA_PERIOD_INDEX, -1)
        val dayOfWeekVal = intent.getIntExtra(EXTRA_DAY_OF_WEEK, -1)
        val startTimeStr = intent.getStringExtra(EXTRA_START_TIME)
        val triggerMillis = intent.getLongExtra(EXTRA_TRIGGER_TIME_MILLIS, 0L)
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)

        // 1. Immediately reschedule for next week so this period's recurring alarm never dies
        if (!isTest && dayOfWeekVal > 0 && periodIndex > 0) {
            TimetableNotificationScheduler.scheduleNextWeekPeriodAlarm(
                context = context,
                dayOfWeekVal = dayOfWeekVal,
                periodIndex = periodIndex,
                subject = subject,
                teacher = rawTeacher,
                startTimeStr = startTimeStr ?: "",
                previousTriggerMillis = triggerMillis
            )
        }

        val repository = TimetableRepository(context.applicationContext)
        if (!repository.notificationsEnabled.value) {
            Log.d("TimetableReceiver", "Notifications disabled in settings; dropping period alert")
            return
        }

        // 2. Suppress system notification when the user is already inside the app (unless it is an explicit test trigger)
        if (!isTest && AppLifecycleTracker.isAppInForeground) {
            Log.d("TimetableReceiver", "App is currently in foreground; suppressing system notification")
            return
        }

        // 3. Drop stale notifications
        if (!isTest) {
            val now = LocalDateTime.now()
            // Check day
            if (dayOfWeekVal > 0 && now.dayOfWeek.value != dayOfWeekVal) {
                Log.w("TimetableReceiver", "Dropping stale notification: Day mismatch (expected $dayOfWeekVal, got ${now.dayOfWeek.value})")
                return
            }

            // Check start time: if lesson has already started, do NOT show "Starts in 5 minutes"
            if (!startTimeStr.isNullOrBlank()) {
                try {
                    val lessonStartTime = LocalTime.parse(startTimeStr)
                    if (now.toLocalTime().isAfter(lessonStartTime)) {
                        Log.w("TimetableReceiver", "Dropping stale notification: Class has already started at $lessonStartTime (current time: ${now.toLocalTime()})")
                        return
                    }
                } catch (e: Exception) {
                    Log.w("TimetableReceiver", "Failed to parse startTimeStr: $startTimeStr", e)
                }
            }

            // Check trigger delay: if alarm was delayed by Android by more than 3 minutes, drop it
            if (triggerMillis > 0) {
                val delayMillis = System.currentTimeMillis() - triggerMillis
                if (delayMillis > 3 * 60 * 1000L) {
                    Log.w("TimetableReceiver", "Dropping stale notification: Delayed by ${delayMillis / 1000}s")
                    return
                }
            }
        }

        // Format subtext: e.g. "(Mr Thomas)"
        val teacherSubtext = if (rawTeacher.isNotBlank()) {
            "(${rawTeacher.removeSurrounding("(", ")").trim()})"
        } else {
            "Starts in 5 minutes"
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SHOW_POPUP", true)
            putExtra(EXTRA_SUBJECT, subject)
            putExtra(EXTRA_TEACHER, rawTeacher)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_PERIOD,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification title: "Biology Starts in 5 minutes"
        // Underneath: "(Mr Thomas)"
        // Timeout: 10 minutes from now (which is 5 minutes into the period)
        val builder = NotificationCompat.Builder(context, CHANNEL_PERIOD_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$subject Starts in 5 minutes")
            .setContentText(teacherSubtext)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(10 * 60 * 1000L) // Auto-clears 5 minutes into next period

        if (canPostNotification(context)) {
            val notificationManager = NotificationManagerCompat.from(context)
            try {
                notificationManager.notify(NOTIFICATION_ID_PERIOD, builder.build())
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }

    private fun handleDismissPeriodNotification(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(NOTIFICATION_ID_PERIOD)
    }

    private fun handleBriefNotification(context: Context, intent: Intent) {
        val dayOfWeekVal = intent.getIntExtra(EXTRA_DAY_OF_WEEK, -1)
        val triggerMillis = intent.getLongExtra(EXTRA_TRIGGER_TIME_MILLIS, 0L)
        val isTest = intent.getBooleanExtra(EXTRA_IS_TEST, false)

        // 1. Immediately reschedule for next week
        if (!isTest && dayOfWeekVal > 0) {
            TimetableNotificationScheduler.scheduleNextWeekDailyBriefAlarm(
                context = context,
                dayOfWeekVal = dayOfWeekVal,
                previousTriggerMillis = triggerMillis
            )
        }

        val repository = TimetableRepository(context.applicationContext)
        if (!repository.notificationsEnabled.value || !repository.dailyBriefEnabled.value) return

        // 2. Suppress system notification if app is already open in foreground
        if (!isTest && AppLifecycleTracker.isAppInForeground) {
            Log.d("TimetableReceiver", "App in foreground; suppressing daily brief notification")
            return
        }

        // 3. Drop stale brief if outside active morning brief window
        if (!isTest) {
            val now = LocalDateTime.now()
            if (dayOfWeekVal > 0 && now.dayOfWeek.value != dayOfWeekVal) return
            val briefEndTime = repository.getDailyBriefEndTime()
            if (now.toLocalTime().isAfter(briefEndTime)) {
                Log.w("TimetableReceiver", "Dropping stale Daily Brief: Current time ${now.toLocalTime()} is after brief end $briefEndTime")
                return
            }
        }

        val today = LocalDate.now().dayOfWeek
        val todayPeriods = repository.getPeriodsForDay(today)
        val brief = DailyBriefManager.generateBrief(todayPeriods)

        if (!brief.hasLessons) return

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_BRIEF,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bulletSummary = brief.bulletPoints.take(3).joinToString("\n")

        val builder = NotificationCompat.Builder(context, CHANNEL_BRIEF_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Morning School Brief")
            .setContentText(brief.headline)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${brief.headline}\n\n$bulletSummary"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(90 * 60 * 1000L) // 90 min timeout

        if (canPostNotification(context)) {
            val notificationManager = NotificationManagerCompat.from(context)
            try {
                notificationManager.notify(NOTIFICATION_ID_BRIEF, builder.build())
            } catch (_: SecurityException) {
                // Permission not granted
            }
        }
    }

    private fun canPostNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
