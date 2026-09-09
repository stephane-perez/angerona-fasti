package com.stephaneperez.angerona.fasti.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val FastiColorScheme = lightColorScheme(
    primary = FastiColors.accent,
    onPrimary = FastiColors.surface,
    background = FastiColors.ground,
    onBackground = FastiColors.ink,
    surface = FastiColors.surface,
    onSurface = FastiColors.ink,
    outline = FastiColors.divider,
    surfaceVariant = FastiColors.neutral100,
    onSurfaceVariant = FastiColors.inkMeta,
)

@Composable
fun AngeronaFastiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FastiColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
