package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stephaneperez.angerona.fasti.R
import com.stephaneperez.angerona.fasti.data.CalendarEvent
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import com.stephaneperez.angerona.fasti.ui.theme.FastiType

@Composable
fun DayEventsList(
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDelete: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (events.isEmpty()) {
        Text(
            text = stringResource(R.string.no_events_this_day),
            style = FastiType.eventMeta,
            modifier = modifier.padding(18.dp),
        )
        return
    }

    LazyColumn(modifier = modifier) {
        items(events.sortedBy { it.startTime ?: "" }, key = { it.id }) { event ->
            EventRow(event = event, onClick = { onEventClick(event) }, onDelete = { onEventDelete(event) })
        }
    }
}

@Composable
private fun EventRow(event: CalendarEvent, onClick: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = when {
        pressed -> FastiColors.accentRowPressed14
        hovered -> FastiColors.inkHover4
        else -> Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .bottomHairline(FastiColors.divider)
            .padding(vertical = 12.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = event.title, style = FastiType.eventTitle)
            val timeLabel = timeRangeLabel(event)
            if (timeLabel != null) {
                Text(text = timeLabel, style = FastiType.eventMeta, modifier = Modifier.padding(top = 2.dp))
            }
        }
        FastiIconButton(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_trash),
            contentDescription = stringResource(R.string.cd_delete_event),
            onClick = onDelete,
        )
    }
}

private fun timeRangeLabel(event: CalendarEvent): String? {
    val start = event.startTime
    val end = event.endTime
    return when {
        start != null && end != null -> "$start – $end"
        start != null -> start
        else -> null
    }
}
