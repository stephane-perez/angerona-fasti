package com.stephaneperez.angerona.fasti.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens shared with Angerona ("Classical" design system) for visual
 * consistency across the two apps. Accent is used only as stroke / text, never as a
 * fill.
 */
object FastiColors {
    val ground = Color(0xFFF3F2F2)
    val surface = Color(0xFFFDFCFC)
    val ink = Color(0xFF201F1D)

    val accent = Color(0xFFB68235)
    val accent600 = Color(0xFFA06F24)
    val accent700 = Color(0xFF7D5411)

    val neutral100 = Color(0xFFF8F4F4)
    val neutral200 = Color(0xFFEAE7E7)
    val neutral300 = Color(0xFFD7D3D3)
    val neutral900 = Color(0xFF282625)

    val divider = ink.copy(alpha = 0.16f)

    val inkStatusBar = ink.copy(alpha = 0.62f)
    val inkMeta = ink.copy(alpha = 0.55f)
    val inkMuted = ink.copy(alpha = 0.40f)

    val accentHover12 = accent.copy(alpha = 0.12f)
    val accentPressed22 = accent.copy(alpha = 0.22f)
    val accentTint20 = accent.copy(alpha = 0.20f)
    val accentRowPressed14 = accent.copy(alpha = 0.14f)

    val inkHover4 = ink.copy(alpha = 0.04f)
    val inkHover7 = ink.copy(alpha = 0.07f)
    val inkPressed14 = ink.copy(alpha = 0.14f)
    val inkBorder45 = ink.copy(alpha = 0.45f)

    val scrim = ink.copy(alpha = 0.45f)
}
