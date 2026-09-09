package com.stephaneperez.angerona.fasti.data

import kotlinx.serialization.Serializable

/**
 * A single calendar event. No recurrence, no reminders in v1 — see README.
 * [date] is ISO-8601 ("YYYY-MM-DD"); [startTime]/[endTime] are "HH:mm" 24h, optional
 * (an all-day event has both null).
 */
@Serializable
data class CalendarEvent(
    val id: String,
    val title: String,
    val date: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String = "",
)

/**
 * The whole calendar, as stored in the single encrypted file. [formatVersion] lets a
 * future version of the app recognize and migrate an older file if the shape of
 * [CalendarEvent] ever needs to change.
 */
@Serializable
data class CalendarData(
    val formatVersion: Int = 1,
    val events: List<CalendarEvent> = emptyList(),
)
