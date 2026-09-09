package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import com.stephaneperez.angerona.fasti.ui.theme.FastiType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    datesWithEvents: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val firstDayOfWeek = firstDayOfWeekFor(locale)
    val firstOfMonth = month.atDay(1)
    // Days from the previous month needed to fill the first row.
    val leadingBlanks = ((firstOfMonth.dayOfWeek.value - firstDayOfWeek.value) + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val totalCells = leadingBlanks + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier.fillMaxWidth()) {
        // Weekday header row
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
            for (i in 0 until 7) {
                val dow = DayOfWeek.of(((firstDayOfWeek.value - 1 + i) % 7) + 1)
                Text(
                    text = dow.getDisplayName(TextStyle.NARROW, locale).uppercase(locale),
                    style = FastiType.weekdayLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - leadingBlanks + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = month.atDay(dayOfMonth)
                        DayCell(
                            date = date,
                            isToday = date == today,
                            isSelected = date == selectedDate,
                            hasEvents = date in datesWithEvents,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

/** java.time doesn't expose a super simple locale-aware "first day of week" outside WeekFields. */
private fun firstDayOfWeekFor(locale: Locale): DayOfWeek =
    java.time.temporal.WeekFields.of(locale).firstDayOfWeek

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()

    val background = when {
        isSelected -> FastiColors.accentTint20
        pressed -> FastiColors.inkPressed14
        hovered -> FastiColors.inkHover4
        else -> Color.Transparent
    }
    val textStyle = if (isSelected || isToday) FastiType.dayNumber.copy(color = FastiColors.accent700) else FastiType.dayNumber

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .then(
                if (isToday && !isSelected) Modifier.border(1.dp, FastiColors.accent, RoundedCornerShape(6.dp))
                else Modifier
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = date.dayOfMonth.toString(), style = textStyle)
            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .background(FastiColors.accent, CircleShape)
                )
            }
        }
    }
}
