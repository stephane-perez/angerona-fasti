package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

fun Modifier.bottomHairline(color: Color): Modifier = drawBehind {
    val strokeWidth = 1f
    drawLine(
        color = color,
        start = Offset(0f, size.height - strokeWidth / 2f),
        end = Offset(size.width, size.height - strokeWidth / 2f),
        strokeWidth = strokeWidth,
    )
}

fun Modifier.topHairline(color: Color): Modifier = drawBehind {
    val strokeWidth = 1f
    drawLine(
        color = color,
        start = Offset(0f, strokeWidth / 2f),
        end = Offset(size.width, strokeWidth / 2f),
        strokeWidth = strokeWidth,
    )
}
