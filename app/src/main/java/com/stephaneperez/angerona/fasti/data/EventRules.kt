package com.stephaneperez.angerona.fasti.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Pure, Android-free rules about events — kept apart from the ViewModel, the UI and
 * AlarmManager so they can be covered by plain JVM unit tests.
 */
object EventRules {
    private val TIME_REGEX = Regex("""([01]\d|2[0-3]):[0-5]\d""")
    private const val MINUTES_PER_DAY = 24 * 60

    /**
     * Formats raw time-field input as the person types: keeps digits only, caps them at
     * four ("HHmm"), and inserts the ":" itself once there are more than two (the numeric
     * keyboard has no colon key). "1430" → "14:30", "9" → "9".
     */
    fun formatTimeInput(raw: String): String {
        val digits = raw.filter { it in '0'..'9' }.take(4)
        return if (digits.length > 2) digits.substring(0, 2) + ":" + digits.substring(2) else digits
    }

    /** True for a complete, real 24h "HH:mm" time ("00:00" to "23:59"). */
    fun isValidTime(value: String): Boolean = TIME_REGEX.matches(value)

    /** A time field is acceptable when it's left empty or holds a valid "HH:mm" time. */
    fun isAcceptableTime(value: String?): Boolean = value.isNullOrEmpty() || isValidTime(value)

    /**
     * When [event]'s reminder should fire, in epoch millis for [zone], or null if it has
     * no reminder or no usable date/start time.
     */
    fun reminderTriggerMillis(event: CalendarEvent, zone: ZoneId): Long? {
        val minutesBefore = event.reminderMinutesBefore ?: return null
        val startTime = event.startTime?.takeIf { isValidTime(it) }?.let { LocalTime.parse(it) } ?: return null
        val date = runCatching { LocalDate.parse(event.date) }.getOrNull() ?: return null
        // Whole days count back on the local calendar ("1 day before" a 09:00 event is
        // 09:00 the day before, even across a DST change); the rest counts back on the
        // real timeline, so "1 hour before" stays one real hour across a DST jump.
        return LocalDateTime.of(date, startTime)
            .minusDays((minutesBefore / MINUTES_PER_DAY).toLong())
            .atZone(zone)
            .minusMinutes((minutesBefore % MINUTES_PER_DAY).toLong())
            .toInstant()
            .toEpochMilli()
    }

    /** Replaces the event with [event]'s id in [events], or appends it if it's new. */
    fun upsert(events: List<CalendarEvent>, event: CalendarEvent): List<CalendarEvent> =
        if (events.any { it.id == event.id }) {
            events.map { if (it.id == event.id) event else it }
        } else {
            events + event
        }

    /** A day's events in display order: all-day events first, then by start time. */
    fun sortedForDay(events: List<CalendarEvent>): List<CalendarEvent> =
        events.sortedWith(compareBy({ it.startTime != null }, { it.startTime }))
}
