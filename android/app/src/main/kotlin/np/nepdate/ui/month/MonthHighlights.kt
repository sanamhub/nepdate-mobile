package np.nepdate.ui.month

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SunnyShape
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The month's holidays and events under the grid, one row per day, so a month shows what happens in
 * it without opening every day. A row opens the day sheet. Nothing shows for a month without any.
 */
@Composable
internal fun MonthHighlights(
    layout: MonthLayout,
    lang: Lang,
    digits: Digits,
    onDayClick: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = highlightDays(layout)
    if (days.isEmpty()) return
    val adMonths = stringArrayResource(R.array.ad_month_long).toList()
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("month_highlights"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = stringResource(R.string.month_highlights_title),
            modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
        )
        days.forEach { cell ->
            HighlightRow(cell, requireNotNull(cell.date), lang, digits, adMonths, onDayClick)
        }
    }
}

/** The month's own days with a public holiday or an event, in date order. */
internal fun highlightDays(layout: MonthLayout): List<MonthCell> =
    layout.cells.filter { it.isInMonth && it.date != null && (it.isHoliday || it.hasEvents) }

@Composable
private fun HighlightRow(
    cell: MonthCell,
    date: NepaliDate,
    lang: Lang,
    digits: Digits,
    adMonths: List<String>,
    onDayClick: (NepaliDate) -> Unit,
) {
    val colors = LocalNepdateColors.current
    val scheme = MaterialTheme.colorScheme
    val (shape, container, content) =
        when {
            cell.isToday -> Triple(SunnyShape, colors.today, colors.onToday)
            cell.isHoliday -> Triple(DayTileShape, colors.holidayContainer, colors.onHolidayContainer)
            else -> Triple(DayTileShape, scheme.surfaceContainerHighest, scheme.onSurface)
        }
    val names = cell.events.map { it.name(lang).inDigits(digits) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(role = Role.Button) { onDayClick(date) }
                .padding(horizontal = 4.dp, vertical = 6.dp)
                .testTag("month_highlight_${date.toIsoString()}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShapeBadge(shape = shape, color = container, size = 40.dp) {
            Text(
                text = cell.bsDay.toString().inDigits(digits),
                color = content,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp, end = 8.dp).weight(1f)) {
            Text(
                text = highlightNames(names, cell.isHoliday, colors.holiday),
                color = scheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text =
                    stringResource(
                        R.string.month_list_weekday_ad,
                        weekdayName(date, lang),
                        adDate(date, adMonths, digits),
                    ),
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
        }
    }
}

/** The day's names, the public holiday first and in the holiday colour. */
private fun highlightNames(
    names: List<String>,
    isHoliday: Boolean,
    holiday: Color,
): AnnotatedString =
    buildAnnotatedString {
        names.forEachIndexed { index, name ->
            if (index > 0) append(", ")
            if (isHoliday && index == 0) {
                withStyle(SpanStyle(color = holiday, fontWeight = FontWeight.Medium)) { append(name) }
            } else {
                append(name)
            }
        }
    }

private val DayTileShape = RoundedCornerShape(12.dp)
