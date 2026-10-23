package com.stephaneperez.angerona.fasti.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class EventRulesTest {

    private fun event(
        id: String = "e1",
        date: String = "2026-03-10",
        startTime: String? = "14:30",
        reminder: Int? = 15,
    ) = CalendarEvent(id = id, title = "T", date = date, startTime = startTime, reminderMinutesBefore = reminder)

    // ---- Time input formatting ----------------------------------------

    @Test
    fun formatTimeInput_insertsColonAfterTwoDigits() {
        assertEquals("", EventRules.formatTimeInput(""))
        assertEquals("1", EventRules.formatTimeInput("1"))
        assertEquals("14", EventRules.formatTimeInput("14"))
        assertEquals("14:3", EventRules.formatTimeInput("143"))
        assertEquals("14:30", EventRules.formatTimeInput("1430"))
    }

    @Test
    fun formatTimeInput_isStableOnItsOwnOutput() {
        // The field feeds its formatted value back on every keystroke.
        assertEquals("14:30", EventRules.formatTimeInput("14:30"))
        assertEquals("14:3", EventRules.formatTimeInput("14:3"))
    }

    @Test
    fun formatTimeInput_dropsNonDigitsAndCapsAtFourDigits() {
        assertEquals("14:30", EventRules.formatTimeInput("14h30"))
        assertEquals("14:30", EventRules.formatTimeInput("143059"))
        assertEquals("09:05", EventRules.formatTimeInput(" 0 9-0.5 "))
        // Only ASCII digits: other Unicode digits would not parse as a time.
        assertEquals("1", EventRules.formatTimeInput("1٣"))
    }

    @Test
    fun formatTimeInput_backspaceOverColonRemovesALastDigit() {
        // "14:3" with one char deleted → "14:" → digits "14".
        assertEquals("14", EventRules.formatTimeInput("14:"))
    }

    // ---- Time validation -------------------------------------------------

    @Test
    fun isValidTime_acceptsTheWholeDay() {
        listOf("00:00", "09:05", "12:00", "19:59", "23:59").forEach { assertTrue(it, EventRules.isValidTime(it)) }
    }

    @Test
    fun isValidTime_rejectsIncompleteOrOutOfRange() {
        listOf("", "9", "14", "14:3", "9:30", "24:00", "25:00", "12:60", "99:99", "1430", "14:300", " 14:30")
            .forEach { assertFalse(it, EventRules.isValidTime(it)) }
    }

    @Test
    fun isAcceptableTime_allowsEmpty() {
        assertTrue(EventRules.isAcceptableTime(null))
        assertTrue(EventRules.isAcceptableTime(""))
        assertTrue(EventRules.isAcceptableTime("08:15"))
        assertFalse(EventRules.isAcceptableTime("08:1"))
    }

    // ---- Reminder trigger time ------------------------------------------

    @Test
    fun reminderTrigger_countsBackFromStartTime() {
        val zone = ZoneId.of("Europe/Paris")
        val expected = ZonedDateTime.of(2026, 3, 10, 14, 15, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(expected, EventRules.reminderTriggerMillis(event(), zone))
    }

    @Test
    fun reminderTrigger_oneDayBeforeCrossesMonthAndYear() {
        val zone = ZoneId.of("UTC")
        val expected = ZonedDateTime.of(2025, 12, 31, 9, 0, 0, 0, zone).toInstant().toEpochMilli()
        val e = event(date = "2026-01-01", startTime = "09:00", reminder = 1440)
        assertEquals(expected, EventRules.reminderTriggerMillis(e, zone))
    }

    @Test
    fun reminderTrigger_handlesDstSpringForward() {
        // Paris skips 02:00–03:00 on 2026-03-29: one hour before 03:30 is 01:30 local.
        val zone = ZoneId.of("Europe/Paris")
        val e = event(date = "2026-03-29", startTime = "03:30", reminder = 60)
        val expected = ZonedDateTime.of(2026, 3, 29, 1, 30, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(expected, EventRules.reminderTriggerMillis(e, zone))
    }

    @Test
    fun reminderTrigger_oneDayBeforeKeepsLocalClockTimeAcrossDst() {
        // Paris leaves DST on 2026-10-25: the day before is a 25-hour day.
        val zone = ZoneId.of("Europe/Paris")
        val e = event(date = "2026-10-26", startTime = "09:00", reminder = 1440)
        val expected = ZonedDateTime.of(2026, 10, 25, 9, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(expected, EventRules.reminderTriggerMillis(e, zone))
    }

    @Test
    fun reminderTrigger_nullWhenNotSchedulable() {
        val zone = ZoneId.of("UTC")
        assertNull(EventRules.reminderTriggerMillis(event(reminder = null), zone))
        assertNull(EventRules.reminderTriggerMillis(event(startTime = null), zone))
        assertNull(EventRules.reminderTriggerMillis(event(startTime = "14:3"), zone))
        assertNull(EventRules.reminderTriggerMillis(event(startTime = "25:00"), zone))
        assertNull(EventRules.reminderTriggerMillis(event(date = "2026-02-30"), zone))
        assertNull(EventRules.reminderTriggerMillis(event(date = "garbage"), zone))
    }

    // ---- List helpers ------------------------------------------------------

    @Test
    fun upsert_appendsNewAndReplacesExistingInPlace() {
        val a = event(id = "a")
        val b = event(id = "b")
        val added = EventRules.upsert(listOf(a), b)
        assertEquals(listOf(a, b), added)

        val b2 = b.copy(title = "changed")
        assertEquals(listOf(a, b2), EventRules.upsert(added, b2))
    }

    @Test
    fun sortedForDay_allDayFirstThenByStartTime() {
        val late = event(id = "late", startTime = "18:00")
        val allDay = event(id = "allDay", startTime = null)
        val early = event(id = "early", startTime = "07:45")
        assertEquals(
            listOf("allDay", "early", "late"),
            EventRules.sortedForDay(listOf(late, allDay, early)).map { it.id },
        )
    }
}
