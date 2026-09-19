package com.stephaneperez.angerona.fasti.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stephaneperez.angerona.fasti.MainActivity
import com.stephaneperez.angerona.fasti.R

/**
 * Builds and shows the reminder notification. Deliberately content-free: no event
 * title, no time, nothing that identifies which event it's for beyond "you have one" —
 * anyone glancing at the lock screen while the phone isn't in the person's hands learns
 * nothing about the calendar's contents. Tapping the notification still opens the app
 * on the right day (that navigation only happens once the person is actually in the
 * app, past whatever screen lock there is).
 */
object NotificationHelper {
    const val CHANNEL_ID = "fasti_event_reminders"

    /** Intent extra keys shared with [MainActivity], [ReminderScheduler] and the receivers. */
    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_EVENT_DATE = "extra_event_date"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Shows a deliberately generic reminder notification — see the class doc comment.
     * [eventId]/[eventDate] are only used to route the tap (and to target this specific
     * notification for [android.app.NotificationManager]), never displayed.
     */
    fun show(context: Context, eventId: String, eventDate: String) {
        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_EVENT_DATE, eventDate)
            putExtra(EXTRA_EVENT_ID, eventId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clock)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body_generic))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            // Belt-and-braces: even though the content itself is already generic, this
            // also keeps the OS from ever synthesizing a "public" fallback version that
            // could differ, and keeps it off a paired Wear/Auto display by surprise.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching { NotificationManagerCompat.from(context).notify(eventId.hashCode(), notification) }
    }
}
