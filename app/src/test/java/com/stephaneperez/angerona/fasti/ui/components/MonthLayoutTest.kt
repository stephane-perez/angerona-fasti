package com.stephaneperez.angerona.fasti.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.YearMonth

class MonthLayoutTest {

    @Test
    fun leadingBlanks_dependsOnFirstDayOfWeek() {
        // 1 October 2026 is a Thursday.
        val october = YearMonth.of(2026, 10)
        assertEquals(3, MonthLayout.leadingBlanks(october, DayOfWeek.MONDAY))
        assertEquals(4, MonthLayout.leadingBlanks(october, DayOfWeek.SUNDAY))
        assertEquals(0, MonthLayout.leadingBlanks(october, DayOfWeek.THURSDAY))
    }

    @Test
    fun rowCount_coversFourToSixWeeks() {
        // February 2027 starts on a Monday and has 28 days: exactly 4 rows, Monday-first.
        assertEquals(4, MonthLayout.rowCount(YearMonth.of(2027, 2), DayOfWeek.MONDAY))
        assertEquals(5, MonthLayout.rowCount(YearMonth.of(2027, 2), DayOfWeek.SUNDAY))
        // August 2026 starts on a Saturday and has 31 days: 6 rows, Monday-first.
        assertEquals(6, MonthLayout.rowCount(YearMonth.of(2026, 8), DayOfWeek.MONDAY))
    }

    @Test
    fun everyDayFitsInTheGrid_forTenYearsAndEveryWeekStart() {
        for (year in 2020..2030) for (m in 1..12) for (start in DayOfWeek.entries) {
            val month = YearMonth.of(year, m)
            val cells = MonthLayout.rowCount(month, start) * 7
            val used = MonthLayout.leadingBlanks(month, start) + month.lengthOfMonth()
            assertTrue("$month / $start", used in (cells - 6)..cells)
        }
    }
}
