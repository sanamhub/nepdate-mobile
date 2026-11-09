package np.nepdate.ui.year

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.month.MonthCell
import np.nepdate.ui.month.MonthLayout
import np.nepdate.ui.theme.LocalNepdateColors

/** The 3 by 4 grid of month tiles; rows and columns share the height and width equally. */
@Composable
internal fun MonthTiles(
    months: List<MonthLayout>,
    today: NepaliDate,
    digits: Digits,
    onOpenMonth: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(R.array.bs_month_long).toList()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        months.chunked(COLUMNS).forEach { row ->
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { layout ->
                    MonthTile(
                        layout = layout,
                        name = monthNames[layout.month.value - 1],
                        isCurrent = layout.year == today.year && layout.month.value == today.monthValue,
                        digits = digits,
                        onClick = { onOpenMonth(layout.year, layout.month.value) },
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthTile(
    layout: MonthLayout,
    name: String,
    isCurrent: Boolean,
    digits: Digits,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (isCurrent) scheme.primaryContainer else scheme.surfaceContainerLow
    val ink = if (isCurrent) scheme.onPrimaryContainer else scheme.onSurface
    val holidays = layout.holidayDays.size
    val title = monthTitle(name, layout.year, digits)
    val description =
        if (holidays == 0) {
            title
        } else {
            stringResource(
                R.string.year_month_tile,
                title,
                pluralStringResource(R.plurals.year_month_holidays, holidays, holidays).inDigits(digits),
            )
        }
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(20.dp))
                .background(container)
                .clickable(role = Role.Button, onClick = onClick)
                // The day numbers are decoration here; TalkBack reads the month and its holiday count.
                .clearAndSetSemantics { contentDescription = description }
                .testTag("year_month_${layout.month.value}")
                .padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TileHeader(name = name, holidays = holidays, ink = ink, digits = digits)
        MiniMonth(
            layout = layout,
            ink = ink,
            digits = digits,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        )
    }
}

/** Only the month's own days; the neighbouring months' cells stay blank. */
@Composable
private fun MiniMonth(
    layout: MonthLayout,
    ink: Color,
    digits: Digits,
    modifier: Modifier = Modifier,
) {
    val usedCells = layout.leadingDays + layout.lastDay.dayOfMonth
    val weeks = layout.cells.take(usedCells).chunked(DAYS_PER_WEEK)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        weeks.forEach { week ->
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                for (column in 0 until DAYS_PER_WEEK) {
                    val cell = week.getOrNull(column)
                    MiniDay(
                        cell = cell?.takeIf { it.isInMonth },
                        ink = ink,
                        digits = digits,
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniDay(
    cell: MonthCell?,
    ink: Color,
    digits: Digits,
    modifier: Modifier = Modifier,
) {
    if (cell == null) {
        Box(modifier)
        return
    }
    val colors = LocalNepdateColors.current
    val background =
        when {
            cell.isToday -> colors.today
            cell.isHoliday -> colors.holidayContainer
            else -> Color.Transparent
        }
    val text =
        when {
            cell.isToday -> colors.onToday
            cell.isHoliday -> colors.onHolidayContainer
            cell.isSaturday -> colors.holiday
            else -> ink
        }
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(if (cell.isToday) 8.dp else 4.dp))
                .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = cell.bsDay.toString().inDigits(digits),
            color = text,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = if (cell.isToday || cell.isHoliday) FontWeight.ExtraBold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** The month name and, when it has any, its public holiday count on a small sindoor pill. */
@Composable
private fun TileHeader(
    name: String,
    holidays: Int,
    ink: Color,
    digits: Digits,
) {
    val colors = LocalNepdateColors.current
    Row(
        modifier = Modifier.padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = ink,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (holidays > 0) {
            Text(
                text = holidays.toString().inDigits(digits),
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(colors.holidayContainer)
                        .padding(horizontal = 6.dp),
                color = colors.onHolidayContainer,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private const val COLUMNS = 3
private const val DAYS_PER_WEEK = 7
