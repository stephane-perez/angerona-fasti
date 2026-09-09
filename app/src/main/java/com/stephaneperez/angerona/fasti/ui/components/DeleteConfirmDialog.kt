package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.stephaneperez.angerona.fasti.R
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import com.stephaneperez.angerona.fasti.ui.theme.FastiType

@Composable
fun DeleteConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(FastiColors.surface)
                .border(1.dp, FastiColors.divider, RoundedCornerShape(7.dp))
                .shadow(12.dp, RoundedCornerShape(7.dp))
                .padding(18.dp),
        ) {
            Text(text = stringResource(R.string.dialog_title_delete_event), style = FastiType.dialogTitle)
            Text(
                text = stringResource(R.string.dialog_body_delete_event),
                style = FastiType.body,
                modifier = Modifier.padding(top = 14.dp),
            )
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.End),
            ) {
                GhostButton(text = stringResource(R.string.action_cancel), onClick = onCancel)
                AccentGhostButton(text = stringResource(R.string.action_delete), onClick = onConfirm)
            }
        }
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = if (hovered) FastiColors.inkHover7 else Color.Transparent
    TextButton(onClick = onClick, interactionSource = interactionSource, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg)) {
        Text(text, style = FastiType.button)
    }
}

@Composable
private fun AccentGhostButton(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = if (hovered) FastiColors.accentHover12 else Color.Transparent
    TextButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, FastiColors.accent700, RoundedCornerShape(4.dp))
            .background(bg),
    ) {
        Text(text, style = FastiType.button.copy(color = FastiColors.accent700))
    }
}
