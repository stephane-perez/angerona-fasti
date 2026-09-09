package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors

@Composable
fun FastiIconButton(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()

    val background = when {
        pressed -> FastiColors.accentPressed22
        hovered || focused -> FastiColors.accentHover12
        else -> Color.Transparent
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .then(
                if (focused) Modifier.border(2.dp, FastiColors.accent, RoundedCornerShape(4.dp))
                else Modifier
            )
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = FastiColors.ink,
            modifier = Modifier.size(20.dp)
        )
    }
}
