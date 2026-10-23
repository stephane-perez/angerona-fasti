package com.stephaneperez.angerona.fasti.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The JSON shape is the on-disk format (inside the encryption): it must stay readable. */
class CalendarDataSerializationTest {
    // Same configuration as CalendarRepository.
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun roundTrip_preservesEveryField() {
        val data = CalendarData(
            events = listOf(
                CalendarEvent("1", "Dentist", "2026-05-04", "09:30", "10:00", "Bring card — ü é ✓", 30),
                CalendarEvent("2", "Holiday", "2026-08-01"),
            )
        )
        assertEquals(data, json.decodeFromString<CalendarData>(json.encodeToString(data)))
    }

    @Test
    fun decodesFilesWrittenBeforeReminders() {
        val old = """{"formatVersion":1,"events":[{"id":"x","title":"Old","date":"2025-01-02","startTime":"08:00"}]}"""
        val event = json.decodeFromString<CalendarData>(old).events.single()
        assertEquals("08:00", event.startTime)
        assertNull(event.endTime)
        assertEquals("", event.description)
        assertNull(event.reminderMinutesBefore)
    }

    @Test
    fun ignoresFieldsFromNewerVersions() {
        val newer = """{"formatVersion":2,"events":[{"id":"x","title":"N","date":"2025-01-02","color":"red"}],"tags":[]}"""
        val data = json.decodeFromString<CalendarData>(newer)
        assertEquals(2, data.formatVersion)
        assertEquals("N", data.events.single().title)
    }

    @Test
    fun emptyDocumentIsAnEmptyCalendar() {
        assertEquals(CalendarData(), json.decodeFromString<CalendarData>("{}"))
    }
}
