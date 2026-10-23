package com.stephaneperez.angerona.fasti.ui.components

import java.time.DayOfWeek
import java.time.YearMonth

/** Pure grid arithmetic behind [MonthGrid], kept Compose-free so it can be unit tested. */
internal object MonthLayout {
    /** Empty cells before day 1, so day 1 lands under its weekday column. */
    fun leadingBlanks(month: YearMonth, firstDayOfWeek: DayOfWeek): Int =
        ((month.atDay(1).dayOfWeek.value - firstDayOfWeek.value) + 7) % 7

    /** Number of week rows needed to show [month] (4 to 6). */
    fun rowCount(month: YearMonth, firstDayOfWeek: DayOfWeek): Int =
        (leadingBlanks(month, firstDayOfWeek) + month.lengthOfMonth() + 6) / 7
}
