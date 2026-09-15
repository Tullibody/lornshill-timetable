package com.example.simplebutton.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.simplebutton.model.TimetablePeriod
import com.example.simplebutton.model.TimetableRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object TimetableNotificationScheduler {

    private const val TAG = "TimetableScheduler"

    /**
     * Schedules all period reminder alarms, period dismissal alarms, and daily brief alarms.
     */
    fun scheduleAll(context: Context, repository: TimetableRepository) {
        val appContext = context.applicationContext

        // If user disabled notifications in settings, do not schedule any alarms
        if (!repository.notificationsEnabled.value) {
            Log.d(TAG, "Notifications are disabled in settings; skipping scheduleAll")
            return
        }

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // First, ensure notification channels exist
        TimetableNotificationReceiver.createNotificationChannels(appContext)

        val periods = repository.periods.toList()
        val now = LocalDateTime.now()

        // 1. Schedule 5-minute pre-period alerts & auto-dismissals for all valid classes
        for (period in periods) {
            // Ignore breaks, lunch, or blank unassigned slots
            if (period.isBreakOrLunch || period.subject.isBlank()) continue

            schedulePeriodAlarms(appContext, alarmManager, period, now)
        }

        // 2. Schedule Morning Daily Brief for school days (Monday - Friday)
        if (repository.dailyBriefEnabled.value) {
            val briefTime = repository.getDailyBriefStartTime()
            scheduleDailyBriefAlarms(appContext, alarmManager, briefTime, now)
        }
    }

    /**
     * Calculates the next LocalDateTime for a target day of week and time.
     * Guaranteed to return a timestamp strictly after [now] and in the future.
     */
    fun calculateNextOccurrence(
        targetDay: DayOfWeek,
        targetTime: LocalTime,
        now: LocalDateTime = LocalDateTime.now(),
        leadMinutes: Long = 0
    ): Pair<LocalDateTime, LocalDateTime> {
        val today = now.toLocalDate()
        var daysUntil = targetDay.value - today.dayOfWeek.value
        if (daysUntil < 0) {
            daysUntil += 7
        }

        var eventDateTime = LocalDateTime.of(today.plusDays(daysUntil.toLong()), targetTime)
        var alertDateTime = eventDateTime.minusMinutes(leadMinutes)

        // If the alert time is now or has already elapsed, advance to next week
        if (!alertDateTime.isAfter(now)) {
            eventDateTime = eventDateTime.plusDays(7)
            alertDateTime = eventDateTime.minusMinutes(leadMinutes)
        }

        // Double check against current epoch millis to prevent any millisecond boundary issue
        val currentMillis = System.currentTimeMillis()
        val alertMillis = alertDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (alertMillis <= currentMillis + 1000L) {
            eventDateTime = eventDateTime.plusDays(7)
            alertDateTime = eventDateTime.minusMinutes(leadMinutes)
        }

        return eventDateTime to alertDateTime
    }

    private fun schedulePeriodAlarms(
        context: Context,
        alarmManager: AlarmManager,
        period: TimetablePeriod,
        now: LocalDateTime
    ) {
        val (targetLessonDateTime, preAlertDateTime) = calculateNextOccurrence(
            targetDay = period.dayOfWeek,
            targetTime = period.startTime,
            now = now,
            leadMinutes = 5
        )
        val dismissDateTime = targetLessonDateTime.plusMinutes(5)

        val triggerEpochMillis = preAlertDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val dismissEpochMillis = dismissDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val periodRequestCode = period.dayOfWeek.value * 100 + period.periodIndex
        val dismissRequestCode = 10000 + periodRequestCode

        val alertIntent = Intent(context, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_TRIGGER_PERIOD
            putExtra(TimetableNotificationReceiver.EXTRA_SUBJECT, period.subject)
            putExtra(TimetableNotificationReceiver.EXTRA_TEACHER, period.teacher)
            putExtra(TimetableNotificationReceiver.EXTRA_PERIOD_INDEX, period.periodIndex)
            putExtra(TimetableNotificationReceiver.EXTRA_DAY_OF_WEEK, period.dayOfWeek.value)
            putExtra(TimetableNotificationReceiver.EXTRA_START_TIME, period.startTime.toString())
            putExtra(TimetableNotificationReceiver.EXTRA_TRIGGER_TIME_MILLIS, triggerEpochMillis)
        }

        val alertPendingIntent = PendingIntent.getBroadcast(
            context,
            periodRequestCode,
            alertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Auto-dismiss Intent
        val dismissIntent = Intent(context, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_DISMISS_PERIOD
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            dismissRequestCode,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, triggerEpochMillis, alertPendingIntent)
        setAlarm(alarmManager, dismissEpochMillis, dismissPendingIntent)
    }

    private fun scheduleDailyBriefAlarms(
        context: Context,
        alarmManager: AlarmManager,
        briefTime: LocalTime,
        now: LocalDateTime
    ) {
        val schoolDays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )

        for (day in schoolDays) {
            val (_, briefDateTime) = calculateNextOccurrence(
                targetDay = day,
                targetTime = briefTime,
                now = now,
                leadMinutes = 0
            )

            val briefEpochMillis = briefDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val briefRequestCode = 20000 + day.value

            val briefIntent = Intent(context, TimetableNotificationReceiver::class.java).apply {
                action = TimetableNotificationReceiver.ACTION_TRIGGER_BRIEF
                putExtra(TimetableNotificationReceiver.EXTRA_DAY_OF_WEEK, day.value)
                putExtra(TimetableNotificationReceiver.EXTRA_TRIGGER_TIME_MILLIS, briefEpochMillis)
            }

            val briefPendingIntent = PendingIntent.getBroadcast(
                context,
                briefRequestCode,
                briefIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setAlarm(alarmManager, briefEpochMillis, briefPendingIntent)
        }
    }

    fun scheduleNextWeekPeriodAlarm(
        context: Context,
        dayOfWeekVal: Int,
        periodIndex: Int,
        subject: String,
        teacher: String,
        startTimeStr: String,
        previousTriggerMillis: Long
    ) {
        val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val currentMillis = System.currentTimeMillis()
        var nextTriggerMillis = if (previousTriggerMillis > 0) {
            previousTriggerMillis + 7 * 24 * 60 * 60 * 1000L
        } else {
            currentMillis + 7 * 24 * 60 * 60 * 1000L
        }
        while (nextTriggerMillis <= currentMillis + 1000L) {
            nextTriggerMillis += 7 * 24 * 60 * 60 * 1000L
        }
        val nextDismissMillis = nextTriggerMillis + 10 * 60 * 1000L

        val periodRequestCode = dayOfWeekVal * 100 + periodIndex
        val dismissRequestCode = 10000 + periodRequestCode

        val alertIntent = Intent(context, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_TRIGGER_PERIOD
            putExtra(TimetableNotificationReceiver.EXTRA_SUBJECT, subject)
            putExtra(TimetableNotificationReceiver.EXTRA_TEACHER, teacher)
            putExtra(TimetableNotificationReceiver.EXTRA_PERIOD_INDEX, periodIndex)
            putExtra(TimetableNotificationReceiver.EXTRA_DAY_OF_WEEK, dayOfWeekVal)
            putExtra(TimetableNotificationReceiver.EXTRA_START_TIME, startTimeStr)
            putExtra(TimetableNotificationReceiver.EXTRA_TRIGGER_TIME_MILLIS, nextTriggerMillis)
        }

        val alertPendingIntent = PendingIntent.getBroadcast(
            context,
            periodRequestCode,
            alertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_DISMISS_PERIOD
        }

        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            dismissRequestCode,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, nextTriggerMillis, alertPendingIntent)
        setAlarm(alarmManager, nextDismissMillis, dismissPendingIntent)
    }

    fun scheduleNextWeekDailyBriefAlarm(
        context: Context,
        dayOfWeekVal: Int,
        previousTriggerMillis: Long
    ) {
        val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val currentMillis = System.currentTimeMillis()
        var nextTriggerMillis = if (previousTriggerMillis > 0) {
            previousTriggerMillis + 7 * 24 * 60 * 60 * 1000L
        } else {
            currentMillis + 7 * 24 * 60 * 60 * 1000L
        }
        while (nextTriggerMillis <= currentMillis + 1000L) {
            nextTriggerMillis += 7 * 24 * 60 * 60 * 1000L
        }
        val briefRequestCode = 20000 + dayOfWeekVal

        val briefIntent = Intent(context, TimetableNotificationReceiver::class.java).apply {
            action = TimetableNotificationReceiver.ACTION_TRIGGER_BRIEF
            putExtra(TimetableNotificationReceiver.EXTRA_DAY_OF_WEEK, dayOfWeekVal)
            putExtra(TimetableNotificationReceiver.EXTRA_TRIGGER_TIME_MILLIS, nextTriggerMillis)
        }

        val briefPendingIntent = PendingIntent.getBroadcast(
            context,
            briefRequestCode,
            briefIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, nextTriggerMillis, briefPendingIntent)
    }

    private fun setAlarm(alarmManager: AlarmManager, epochMillis: Long, pendingIntent: PendingIntent) {
        val currentMillis = System.currentTimeMillis()
        if (epochMillis <= currentMillis + 1000L) {
            Log.w(TAG, "Refusing to schedule alarm in past or current millisecond: $epochMillis vs $currentMillis")
            return
        }

        try {
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission restricted: ${e.message}")
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    epochMillis,
                    pendingIntent
                )
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule alarm: ${e2.message}")
            }
        }
    }

    /**
     * Cancels all scheduled alarms.
     */
    fun cancelAll(context: Context, repository: TimetableRepository) {
        val appContext = context.applicationContext
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        for (period in repository.periods) {
            val periodRequestCode = period.dayOfWeek.value * 100 + period.periodIndex
            val dismissRequestCode = 10000 + periodRequestCode

            val alertIntent = Intent(appContext, TimetableNotificationReceiver::class.java).apply {
                action = TimetableNotificationReceiver.ACTION_TRIGGER_PERIOD
            }
            val alertPendingIntent = PendingIntent.getBroadcast(
                appContext,
                periodRequestCode,
                alertIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (alertPendingIntent != null) {
                alarmManager.cancel(alertPendingIntent)
            }

            val dismissIntent = Intent(appContext, TimetableNotificationReceiver::class.java).apply {
                action = TimetableNotificationReceiver.ACTION_DISMISS_PERIOD
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                appContext,
                dismissRequestCode,
                dismissIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (dismissPendingIntent != null) {
                alarmManager.cancel(dismissPendingIntent)
            }
        }

        for (dayVal in 1..5) {
            val briefRequestCode = 20000 + dayVal
            val briefIntent = Intent(appContext, TimetableNotificationReceiver::class.java).apply {
                action = TimetableNotificationReceiver.ACTION_TRIGGER_BRIEF
            }
            val briefPendingIntent = PendingIntent.getBroadcast(
                appContext,
                briefRequestCode,
                briefIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (briefPendingIntent != null) {
                alarmManager.cancel(briefPendingIntent)
            }
        }
    }
}
