package com.stephaneperez.angerona.fasti.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.stephaneperez.angerona.fasti.data.CalendarRepository
import com.stephaneperez.angerona.fasti.data.LoadResult
import com.stephaneperez.angerona.fasti.notification.ReminderScheduler

/**
 * AlarmManager clears every scheduled alarm on reboot, so every pending reminder has to
 * be rescheduled once the device comes back up. The same goes after an app update, and
 * after a clock or time zone change: alarms are stored as absolute instants, computed
 * from the event's local date and time in the zone current at scheduling time. Reading the calendar here is no
 * different from any other cold start: the Keystore key isn't gated behind user
 * authentication (see [com.stephaneperez.angerona.fasti.data.CryptoManager]), so it's
 * available as soon as the app's process can run.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        // A BroadcastReceiver's onReceive runs on the main thread and Android expects it
        // back quickly; goAsync() plus a background thread keeps the (small, local,
        // synchronous) decrypt-and-reschedule work off the main thread while still
        // holding the receiver alive long enough to finish it.
        val pendingResult = goAsync()
        Thread {
            try {
                val appContext = context.applicationContext
                val result = CalendarRepository(appContext).load()
                if (result is LoadResult.Success) {
                    ReminderScheduler.rescheduleAll(appContext, result.data.events)
                }
            } catch (_: Throwable) {
                // Nothing to report from a background broadcast; the next app start
                // reschedules everything anyway.
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
