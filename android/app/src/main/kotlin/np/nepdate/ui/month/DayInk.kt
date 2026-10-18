package np.nepdate.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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

/**
 * The inside of a grid cell. The numeral, the mark slot and the tithi slot have fixed heights, so the
 * numerals of a row share one baseline whether a day has an event dot, a today ring or a two-line
 * tithi.
 */
@Composable
internal fun BoxScope.DayCellContent(
    cell: MonthCell,
    isSelected: Boolean,
    digits: Digits,
    lang: Lang,
    showTithi: Boolean,
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
        DayNumeral(cell, digits, ink.primary, showUnderline = isSelected && cell.isHoliday)
        Box(modifier = Modifier.height(MARK_SLOT), contentAlignment = Alignment.TopCenter) {
            if (cell.hasEvents) {
                EventDot(ink.holidayInk)
            }
        }
        if (showTithi) {
            Box(modifier = Modifier.height(tithiSlot()), contentAlignment = Alignment.TopCenter) {
                cell.tithi?.let { tithi ->
                    TithiLine(tithi, lang, ink.secondary)
                }
            }
        }
    }
}

/** Two tithi lines at their largest size, so a wrapped name and a short one take the same room. */
@Composable
private fun tithiSlot(): Dp {
    val fontSize = MaterialTheme.typography.labelSmall.fontSize
    return with(LocalDensity.current) { (fontSize * TITHI_LINE_HEIGHT.value * 2).toDp() }
}

/**
 * The BS numeral in a box the size of the today ring, drawn or not, so today's numeral sits where
 * every other numeral does. The selected holiday's underline (rule H1, when the selection fill
 * replaces the holiday fill) sits inside the box, under the digits.
 */
@Composable
private fun DayNumeral(
    cell: MonthCell,
    digits: Digits,
    color: Color,
    showUnderline: Boolean,
) {
    val today = LocalNepdateColors.current.today
    Box(
        modifier =
            Modifier
                .size(numeralSlot())
                .then(if (cell.isToday) Modifier.border(2.dp, today, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cell.bsDay.toString().inDigits(digits),
            color = color,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
        )
        if (showUnderline) {
            HolidayUnderline(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp))
        }
    }
}

/** The numeric slot is at least the today ring and always as tall as the numeral's line box. */
@Composable
private fun numeralSlot(): Dp {
    val lineHeight =
        with(LocalDensity.current) {
            MaterialTheme.typography.titleLarge.lineHeight
                .toDp()
        }
    return maxOf(NUMERAL_BOX, lineHeight)
}

@Composable
internal fun HolidayUnderline(
    modifier: Modifier = Modifier,
    width: Dp = 18.dp,
) {
    Box(
        modifier =
            modifier
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

/**
 * The tithi under the numeral (MR-01 AC8). It never ellipsizes, because the requirement forbids
 * truncation. A name first shrinks on one line from `labelSmall` down to [TITHI_MIN_SIZE]; only a
 * name that still does not fit ("Chaturdashi", "Ekadashi (K)" on a 360 dp screen) wraps to two
 * lines, at the largest size that fits, with a Latin word broken at a hyphen. Devanagari is broken
 * only between words, so conjuncts stay whole.
 */
@Composable
internal fun TithiLine(
    tithi: Named,
    lang: Lang,
    color: Color,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val text = tithi.name(lang)
    val style =
        MaterialTheme.typography.labelSmall.copy(
            hyphens = Hyphens.Auto,
            lineHeight = TITHI_LINE_HEIGHT,
            textAlign = TextAlign.Center,
        )
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier) {
        val maxWidth = constraints.maxWidth
        val fit = remember(text, style, maxWidth, measurer) { tithiFit(measurer, text, style, maxWidth) }
        Text(
            text = text,
            color = color,
            style = style.copy(fontSize = fit.size),
            maxLines = fit.lines,
            softWrap = fit.lines > 1,
            onTextLayout = onTextLayout,
        )
    }
}

/** The size and line count a tithi name is drawn with. */
private data class TithiFit(
    val size: TextUnit,
    val lines: Int,
)

private fun tithiFit(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    maxWidth: Int,
): TithiFit {
    val sizes = generateSequence(style.fontSize.value) { it - TITHI_SIZE_STEP }.takeWhile { it >= TITHI_MIN_SIZE.value }
    for (lines in 1..2) {
        for (size in sizes) {
            val result =
                measurer.measure(
                    text = text,
                    style = style.copy(fontSize = size.sp),
                    maxLines = lines,
                    softWrap = lines > 1,
                    constraints = Constraints(maxWidth = maxWidth),
                )
            if (!result.hasVisualOverflow) return TithiFit(size.sp, lines)
        }
    }
    return TithiFit(TITHI_MIN_SIZE, 2)
}

/** The smallest tithi size: below this Devanagari vowel signs stop being legible on a phone. */
internal val TITHI_MIN_SIZE = 9.sp

private const val TITHI_SIZE_STEP = 0.5f

private val TITHI_LINE_HEIGHT = 1.15.em

private val NUMERAL_BOX = 32.dp

private val MARK_SLOT = 6.dp
