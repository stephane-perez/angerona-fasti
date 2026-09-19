package com.stephaneperez.angerona.fasti.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.stephaneperez.angerona.fasti.data.CalendarEvent
import com.stephaneperez.angerona.fasti.receiver.ReminderReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Schedules (and cancels) the AlarmManager alarm behind an event's reminder. One alarm
 * per event, keyed by a PendingIntent request code derived from the event's stable id
 * so re-scheduling an edited event or canceling a deleted one always targets the right
 * alarm — matching is by request code + target component, not by intent extras.
 */
object ReminderScheduler {

    /** (Re)schedules [event]'s reminder, replacing whatever was previously scheduled for it. */
    fun schedule(context: Context, event: CalendarEvent) {
        cancel(context, event.id)

        val minutesBefore = event.reminderMinutesBefore ?: return
        val startTime = event.startTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return
        val date = runCatching { LocalDate.parse(event.date) }.getOrNull() ?: return

        val triggerAt = LocalDateTime.of(date, startTime).minusMinutes(minutesBefore.toLong())
        val triggerMillis = triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (triggerMillis <= System.currentTimeMillis()) return // already in the past — nothing to schedule

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = buildPendingIntent(context, event.id, event.date)

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        runCatching {
            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            } else {
                // SCHEDULE_EXACT_ALARM was denied/revoked in system settings — fall back
                // to an inexact alarm rather than failing the reminder outright.
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        }
    }

    fun cancel(context: Context, eventId: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = buildPendingIntent(context, eventId, date = "")
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /** Re-schedules every event with a reminder — used after a reboot and on app start. */
    fun rescheduleAll(context: Context, events: List<CalendarEvent>) {
        events.forEach { schedule(context, it) }
    }

    private fun buildPendingIntent(context: Context, eventId: String, date: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_EVENT_ID, eventId)
            putExtra(NotificationHelper.EXTRA_EVENT_DATE, date)
        }
        return PendingIntent.getBroadcast(
            context,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
