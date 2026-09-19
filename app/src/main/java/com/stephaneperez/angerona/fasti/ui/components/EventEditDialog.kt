package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.stephaneperez.angerona.fasti.R
import com.stephaneperez.angerona.fasti.data.EditingEvent
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import com.stephaneperez.angerona.fasti.ui.theme.FastiType
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventEditDialog(
    editing: EditingEvent,
    isNew: Boolean,
    onTitleChanged: (String) -> Unit,
    onTimesChanged: (String?, String?) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onReminderChanged: (Int?) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val dateLabel = editing.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.getDefault()))
        .replaceFirstChar { it.titlecase(Locale.getDefault()) }

    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(FastiColors.surface)
                .border(1.dp, FastiColors.divider, RoundedCornerShape(7.dp))
                .shadow(12.dp, RoundedCornerShape(7.dp))
                .padding(18.dp),
        ) {
            Text(
                text = stringResource(if (isNew) R.string.dialog_title_new_event else R.string.dialog_title_edit_event),
                style = FastiType.dialogTitle,
            )
            Text(text = dateLabel, style = FastiType.eventMeta, modifier = Modifier.padding(top = 4.dp))

            FastiTextField(
                value = editing.title,
                onValueChange = onTitleChanged,
                placeholder = stringResource(R.string.field_event_title),
                modifier = Modifier.padding(top = 14.dp),
            )

            Row(modifier = Modifier.padding(top = 9.dp)) {
                TimeField(
                    value = editing.startTime.orEmpty(),
                    placeholder = stringResource(R.string.field_start_time),
                    onValueChange = { onTimesChanged(it.ifBlank { null }, editing.endTime) },
                    modifier = Modifier.weight(1f),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.widthIn(min = 9.dp))
                TimeField(
                    value = editing.endTime.orEmpty(),
                    placeholder = stringResource(R.string.field_end_time),
                    onValueChange = { onTimesChanged(editing.startTime, it.ifBlank { null }) },
                    modifier = Modifier.weight(1f),
                )
            }

            FastiTextField(
                value = editing.description,
                onValueChange = onDescriptionChanged,
                placeholder = stringResource(R.string.field_description),
                singleLine = false,
                modifier = Modifier.padding(top = 9.dp),
            )

            val reminderEnabled = !editing.startTime.isNullOrBlank()
            Text(
                text = stringResource(R.string.field_reminder),
                style = FastiType.eventMeta,
                modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
            )
            ReminderField(
                selectedMinutes = editing.reminderMinutesBefore,
                enabled = reminderEnabled,
                onSelected = onReminderChanged,
                modifier = Modifier.fillMaxWidth(),
            )
            if (!reminderEnabled) {
                Text(
                    text = stringResource(R.string.hint_reminder_needs_time),
                    style = FastiType.eventMeta,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Row(
                modifier = Modifier.padding(top = 18.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (onDelete != null) {
                    DialogGhostButton(text = stringResource(R.string.action_delete), onClick = onDelete, danger = true)
                } else {
                    androidx.compose.foundation.layout.Spacer(Modifier)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    DialogGhostButton(text = stringResource(R.string.action_cancel), onClick = onCancel)
                    DialogAccentButton(
                        text = stringResource(R.string.action_save),
                        onClick = onConfirm,
                        enabled = editing.title.isNotBlank(),
                    )
                }
            }
        }
    }
}

@Composable
private fun FastiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        focused -> FastiColors.accent
        hovered -> FastiColors.inkBorder45
        else -> FastiColors.divider
    }
    val borderWidth = if (focused) 2.dp else 1.dp

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = FastiType.fieldValue.copy(color = FastiColors.inkMeta)) },
        singleLine = singleLine,
        interactionSource = interactionSource,
        textStyle = FastiType.fieldValue,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FastiColors.surface,
            unfocusedContainerColor = FastiColors.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = FastiColors.accent,
        ),
    )
}

/**
 * Plain "HH:mm" text entry — no wheel picker, kept deliberately simple for v1. The
 * numeric keyboard ([KeyboardType.Number]) has no ":" key on Android, so the colon is
 * inserted automatically as the person types digits (e.g. typing "1430" renders as
 * "14:30") rather than asking them to type a character the keyboard can't produce.
 */
@Composable
private fun TimeField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        focused -> FastiColors.accent
        hovered -> FastiColors.inkBorder45
        else -> FastiColors.divider
    }
    val borderWidth = if (focused) 2.dp else 1.dp

    TextField(
        value = value,
        onValueChange = { new ->
            // Strip everything but digits, cap at 4 ("HHmm"), then re-insert the colon
            // ourselves once there are more than 2 digits. Still a light-touch guard,
            // not full validation — an invalid value just won't parse as a time and is
            // treated the same as "no time set".
            val digits = new.filter { it.isDigit() }.take(4)
            val filtered = if (digits.length > 2) {
                digits.substring(0, 2) + ":" + digits.substring(2)
            } else {
                digits
            }
            onValueChange(filtered)
        },
        placeholder = { Text(placeholder, style = FastiType.fieldValue.copy(color = FastiColors.inkMeta)) },
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = FastiType.fieldValue,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(4.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FastiColors.surface,
            unfocusedContainerColor = FastiColors.surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = FastiColors.accent,
        ),
    )
}

/**
 * The lead-time choice for an event's reminder notification: None, or a fixed offset
 * before the event's start time. Disabled (and forced back to "None" by the caller)
 * when the event has no start time, since a reminder counts back from a clock time.
 */
@Composable
private fun ReminderField(
    selectedMinutes: Int?,
    enabled: Boolean,
    onSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val borderColor = if (enabled && hovered) FastiColors.inkBorder45 else FastiColors.divider

    val options: List<Pair<Int?, String>> = listOf(
        null to stringResource(R.string.reminder_option_none),
        5 to stringResource(R.string.reminder_option_5min),
        15 to stringResource(R.string.reminder_option_15min),
        30 to stringResource(R.string.reminder_option_30min),
        60 to stringResource(R.string.reminder_option_1hour),
        120 to stringResource(R.string.reminder_option_2hours),
        1440 to stringResource(R.string.reminder_option_1day),
    )
    val selectedLabel = options.firstOrNull { it.first == selectedMinutes }?.second ?: options.first().second

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                ) { expanded = true }
                .padding(horizontal = 12.dp, vertical = 14.dp),
        ) {
            Text(
                text = selectedLabel,
                style = FastiType.fieldValue.copy(color = if (enabled) FastiColors.ink else FastiColors.inkMuted),
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(R.drawable.ic_clock),
                contentDescription = null,
                tint = FastiColors.inkMeta,
                modifier = Modifier.size(16.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (minutes, label) ->
                DropdownMenuItem(
                    text = { Text(label, style = FastiType.fieldValue) },
                    onClick = {
                        onSelected(minutes)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DialogGhostButton(text: String, onClick: () -> Unit, danger: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = if (hovered) FastiColors.inkHover7 else Color.Transparent
    TextButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg),
    ) {
        Text(text, style = FastiType.button.copy(color = if (danger) FastiColors.accent700 else FastiColors.ink))
    }
}

@Composable
private fun DialogAccentButton(text: String, onClick: () -> Unit, enabled: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = if (hovered && enabled) FastiColors.accentHover12 else Color.Transparent
    TextButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, if (enabled) FastiColors.accent else FastiColors.divider, RoundedCornerShape(4.dp))
            .background(bg),
    ) {
        Text(text, style = FastiType.button.copy(color = if (enabled) FastiColors.accent else FastiColors.inkMuted))
    }
}
