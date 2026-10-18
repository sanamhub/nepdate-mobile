package np.nepdate.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.patro.Named
import np.nepdate.data.Digits
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.theme.LocalNepdateColors
import np.nepdate.ui.theme.holidayFillOn

/** The colours one cell uses, chosen once so the grid and the list draw the same states. */
internal data class DayInk(
    val primary: Color,
    val secondary: Color,
    val fill: Color,
    val holidayInk: Boolean,
)

@Composable
internal fun dayInk(
    cell: MonthCell,
    isSelected: Boolean,
): DayInk {
    val colors = LocalNepdateColors.current
    val background = MaterialTheme.colorScheme.background
    val selection = MaterialTheme.colorScheme.surfaceContainerHigh
    val holidayInk = cell.isInMonth && (cell.isHoliday || cell.isSaturday)
    val fill =
        when {
            isSelected -> selection
            cell.isHoliday -> colors.holidayFillOn(background)
            else -> background
        }
    val primary =
        when {
            !cell.isInMonth -> colors.dimmed
            holidayInk -> colors.holiday
            else -> MaterialTheme.colorScheme.onSurface
        }
    val secondary =
        when {
            !cell.isInMonth -> colors.dimmed
            holidayInk -> colors.holiday
            else -> colors.dimmed
        }
    return DayInk(primary, secondary, fill, holidayInk)
}

@Composable
internal fun BoxScope.DayCellContent(
    cell: MonthCell,
    isSelected: Boolean,
    digits: Digits,
    lang: Lang,
    ink: DayInk,
) {
    Text(
        text = cell.adDay.toString().inDigits(digits),
        modifier = Modifier.align(Alignment.TopEnd),
        color = ink.secondary,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
    )
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DayNumeral(cell, digits, ink.primary)
        if (isSelected && cell.isHoliday) {
            HolidayUnderline()
        }
        if (cell.hasEvents) {
            EventDot(ink.holidayInk)
        }
        cell.tithi?.let { tithi ->
            TithiLine(tithi, lang, ink.secondary)
        }
    }
}

@Composable
private fun DayNumeral(
    cell: MonthCell,
    digits: Digits,
    color: Color,
) {
    val today = LocalNepdateColors.current.today
    Box(
        modifier =
            if (cell.isToday) {
                Modifier
                    .size(34.dp)
                    .border(2.dp, today, CircleShape)
            } else {
                Modifier
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cell.bsDay.toString().inDigits(digits),
            color = color,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
        )
    }
}

@Composable
internal fun HolidayUnderline(width: Dp = 18.dp) {
    Box(
        modifier =
            Modifier
                .padding(top = 1.dp)
                .width(width)
                .height(2.dp)
                .background(LocalNepdateColors.current.holiday),
    )
}

@Composable
private fun EventDot(holidayInk: Boolean) {
    val colors = LocalNepdateColors.current
    Box(
        modifier =
            Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .background(if (holidayInk) colors.holiday else colors.event, CircleShape),
    )
}

@Composable
private fun TithiLine(
    tithi: Named,
    lang: Lang,
    color: Color,
) {
    Text(
        text = tithi.name(lang),
        color = color,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 11.sp, stepSize = 0.5.sp),
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
    )
}
