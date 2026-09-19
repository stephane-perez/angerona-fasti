package com.stephaneperez.angerona.fasti.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stephaneperez.angerona.fasti.notification.NotificationHelper

/** Fired by AlarmManager at the reminder's computed trigger time — shows the notification. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_ID) ?: return
        val eventDate = intent.getStringExtra(NotificationHelper.EXTRA_EVENT_DATE).orEmpty()

        NotificationHelper.show(context, eventId, eventDate)
    }
}
