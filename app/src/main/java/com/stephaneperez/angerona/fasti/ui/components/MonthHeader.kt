package com.stephaneperez.angerona.fasti.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stephaneperez.angerona.fasti.R
import com.stephaneperez.angerona.fasti.ui.theme.FastiColors
import com.stephaneperez.angerona.fasti.ui.theme.FastiType
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.getDefault()
    val monthName = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.titlecase(locale) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(FastiColors.surface)
            .bottomHairline(FastiColors.divider)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FastiIconButton(
            painter = painterResource(R.drawable.ic_chevron_left),
            contentDescription = stringResource(R.string.cd_previous_month),
            onClick = onPrevious,
        )
        Text(
            text = "$monthName ${month.year}",
            style = FastiType.screenTitle,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
        )
        TodayButton(onClick = onToday)
        FastiIconButton(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = stringResource(R.string.cd_next_month),
            onClick = onNext,
        )
    }
}

@Composable
private fun TodayButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val bg = when {
        pressed -> FastiColors.inkPressed14
        hovered -> FastiColors.inkHover7
        else -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(text = stringResource(R.string.action_today), style = FastiType.button.copy(color = FastiColors.accent))
    }
}
