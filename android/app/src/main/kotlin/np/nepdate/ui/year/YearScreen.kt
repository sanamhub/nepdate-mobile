package np.nepdate.ui.year

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.common.resolve
import np.nepdate.ui.month.MonthCell
import np.nepdate.ui.month.MonthLayout
import np.nepdate.ui.month.PICKER_FIRST_YEAR
import np.nepdate.ui.month.PICKER_LAST_YEAR
import np.nepdate.ui.month.monthLayout
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The year at a glance: twelve mini months in a 3 by 4 grid that fills the screen. Saturdays are
 * red, public holidays sit on the holiday container and today is marigold. Tapping a month opens it.
 *
 * @param initialYear the BS year to open on; clamped to the library range
 * @param onOpenMonth called with the BS year and month (1..12) of the tapped tile
 */
@Composable
internal fun YearScreen(
    initialYear: Int,
    today: NepaliDate,
    settings: Settings,
    onBack: () -> Unit,
    onOpenMonth: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val digits = settings.digits.resolve(lang)
    var year by rememberSaveable { mutableIntStateOf(initialYear.coerceIn(PICKER_FIRST_YEAR, PICKER_LAST_YEAR)) }
    // Twelve layouts, one Patro lookup per day, computed once per year. The tithi is not drawn,
    // so it is switched off to skip that work.
    val months =
        remember(year, today) {
            val plain = settings.copy(showTithi = false)
            (1..MONTHS).map { monthLayout(year, it, plain, today) }
        }
    BackHandler(onBack = onBack)
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        YearTopBar(
            year = year,
            digits = digits,
            onBack = onBack,
            onPrevious = { year-- },
            onNext = { year++ },
        )
        Legend(digits = digits, saturday = Weekday.SATURDAY.displayName(lang))
        MonthTiles(
            months = months,
            today = today,
            digits = digits,
            onOpenMonth = onOpenMonth,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun YearTopBar(
    year: Int,
    digits: Digits,
    onBack: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val backLabel = stringResource(R.string.year_back)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 72.dp)
                .padding(start = 4.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = backLabel }) {
            SymbolIcon(icon = R.drawable.ic_sym_arrow_back, tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text = year.toString().inDigits(digits),
            modifier =
                Modifier
                    .weight(1f)
                    .testTag("year_title")
                    .semantics { heading() },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            YearStep(
                label = stringResource(R.string.year_previous),
                icon = R.drawable.ic_sym_chevron_left,
                shape = groupShape(GroupPosition.START),
                enabled = year > PICKER_FIRST_YEAR,
                onClick = onPrevious,
            )
            YearStep(
                label = stringResource(R.string.year_next),
                icon = R.drawable.ic_sym_chevron_right,
                shape = groupShape(GroupPosition.END),
                enabled = year < PICKER_LAST_YEAR,
                onClick = onNext,
            )
        }
    }
}

/** One half of the connected previous/next year group: 48 by 40 dp, inside a 48 dp touch row. */
@Composable
private fun YearStep(
    label: String,
    icon: Int,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val tonal = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(
        modifier =
            Modifier
                .size(width = 48.dp, height = 48.dp)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 48.dp, height = 40.dp)
                    .clip(shape)
                    .background(if (enabled) tonal else tonal.copy(alpha = DISABLED_ALPHA)),
            contentAlignment = Alignment.Center,
        ) {
            SymbolIcon(
                icon = icon,
                size = 22.dp,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else DISABLED_ALPHA),
            )
        }
    }
}

@Composable
private fun Legend(
    digits: Digits,
    saturday: String,
) {
    val colors = LocalNepdateColors.current
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(text = stringResource(R.string.month_today)) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(colors.today))
        }
        LegendItem(text = stringResource(R.string.public_holiday)) {
            Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(colors.holidayContainer))
        }
        LegendItem(text = saturday) {
            Text(
                text = "1".inDigits(digits),
                color = colors.holiday,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun LegendItem(
    text: String,
    swatch: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        swatch()
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

/** The 3 by 4 grid of month tiles; rows and columns share the height and width equally. */
@Composable
private fun MonthTiles(
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
        Text(
            text = name,
            modifier = Modifier.padding(start = 4.dp),
            color = ink,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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

private const val MONTHS = 12
private const val COLUMNS = 3
private const val DAYS_PER_WEEK = 7
private const val DISABLED_ALPHA = 0.38f
