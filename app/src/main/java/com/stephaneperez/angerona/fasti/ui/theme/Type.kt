package com.stephaneperez.angerona.fasti.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.stephaneperez.angerona.fasti.R

val CormorantGaramond = FontFamily(
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal)
)

val Lora = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal)
)

// The month grid's day numbers use tabular figures so columns line up.
object FastiType {
    val dialogTitle = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 22.sp,
        color = FastiColors.ink,
    )
    val screenTitle = TextStyle(
        fontFamily = CormorantGaramond,
        fontSize = 24.sp,
        color = FastiColors.ink,
    )
    val weekdayLabel = TextStyle(
        fontFamily = Lora,
        fontSize = 11.sp,
        letterSpacing = 0.06.em,
        color = FastiColors.inkMeta,
    )
    val dayNumber = TextStyle(
        fontFamily = Lora,
        fontSize = 15.sp,
        fontFeatureSettings = "tnum",
        color = FastiColors.ink,
    )
    val dayNumberMuted = dayNumber.copy(color = FastiColors.inkMuted)
    val eventDot = TextStyle(
        fontFamily = Lora,
        fontSize = 10.sp,
        color = FastiColors.accent700,
    )
    val body = TextStyle(
        fontFamily = Lora,
        fontSize = 14.sp,
        lineHeight = 22.4.sp,
        color = FastiColors.ink,
    )
    val eventTitle = TextStyle(
        fontFamily = Lora,
        fontSize = 15.sp,
        color = FastiColors.ink,
    )
    val eventMeta = TextStyle(
        fontFamily = Lora,
        fontSize = 12.sp,
        fontFeatureSettings = "tnum",
        color = FastiColors.inkMeta,
    )
    val button = TextStyle(
        fontFamily = Lora,
        fontSize = 13.sp,
        color = FastiColors.ink,
    )
    val fieldLabel = TextStyle(
        fontFamily = Lora,
        fontSize = 11.sp,
        letterSpacing = 0.06.em,
        color = FastiColors.accent700,
    )
    val fieldValue = TextStyle(
        fontFamily = Lora,
        fontSize = 15.sp,
        color = FastiColors.ink,
    )
}
