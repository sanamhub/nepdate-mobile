package np.nepdate.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.weekdayName

/** The 7-column month grid from design-system §4. */
@Composable
internal fun MonthGrid(
    layout: MonthLayout,
    selectedDate: NepaliDate?,
    settings: Settings,
    lang: Lang,
    bsMonthNames: List<String>,
    monthNames: List<String>,
    publicHoliday: String,
    onDayClick: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .navigationBarsPadding()
                .testTag("month_grid"),
    ) {
        layout.cells.chunked(DAYS_PER_WEEK).forEachIndexed { rowIndex, row ->
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                row.forEach { cell ->
                    val clickableDate = cell.date?.takeIf { cell.isInMonth }
                    DayCell(
                        cell = cell,
                        isSelected = cell.date != null && cell.date == selectedDate,
                        digits = settings.digits,
                        lang = lang,
                        description =
                            dayDescription(
                                cell,
                                lang,
                                settings.digits,
                                bsMonthNames,
                                monthNames,
                                publicHoliday,
                            ),
                        onCellClick = clickableDate?.let { date -> { onDayClick(date) } },
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                    )
                }
            }
            if (rowIndex < layout.rowCount - 1) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.20f),
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: MonthCell,
    isSelected: Boolean,
    digits: Digits,
    lang: Lang,
    description: String,
    onCellClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val ink = dayInk(cell, isSelected)
    val base =
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ink.fill)
            .then(if (onCellClick != null) Modifier.clickable(onClick = onCellClick) else Modifier)
            .dayCellSemantics(cell, description, isSelected, onCellClick)
    Box(
        modifier = base.padding(2.dp),
    ) {
        if (cell.date != null) {
            DayCellContent(cell, isSelected, digits, lang, ink)
        }
    }
}

private fun Modifier.dayCellSemantics(
    cell: MonthCell,
    description: String,
    isSelected: Boolean,
    onCellClick: (() -> Unit)?,
): Modifier {
    val date = cell.date ?: return this
    return clearAndSetSemantics {
        contentDescription = description
        testTag = "month_cell_${if (cell.isInMonth) "in" else "out"}_${date.toIsoString()}"
        if (onCellClick != null) {
            role = Role.Button
            selected = isSelected
            onClick(label = description) {
                onCellClick()
                true
            }
        }
    }
}

/** The month as a list above the largest font scale that fits seven columns (MR-10 AC2). */
@Composable
internal fun MonthList(
    layout: MonthLayout,
    selectedDate: NepaliDate?,
    settings: Settings,
    lang: Lang,
    bsMonthNames: List<String>,
    monthNames: List<String>,
    publicHoliday: String,
    onDayClick: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .testTag("month_list"),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(
            items = layout.cells.filter { it.isInMonth },
            key = { cell -> cell.date?.toIsoString() ?: cell.bsDay },
        ) { cell ->
            val date = cell.date ?: return@items
            DayListRow(
                cell = cell,
                date = date,
                isSelected = date == selectedDate,
                settings = settings,
                lang = lang,
                bsMonthNames = bsMonthNames,
                monthNames = monthNames,
                publicHoliday = publicHoliday,
                onDayClick = onDayClick,
            )
        }
    }
}

@Composable
private fun DayListRow(
    cell: MonthCell,
    date: NepaliDate,
    isSelected: Boolean,
    settings: Settings,
    lang: Lang,
    bsMonthNames: List<String>,
    monthNames: List<String>,
    publicHoliday: String,
    onDayClick: (NepaliDate) -> Unit,
) {
    val ink = dayInk(cell, isSelected)
    val description = dayDescription(cell, lang, settings.digits, bsMonthNames, monthNames, publicHoliday)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ink.fill)
                .clickable { onDayClick(date) }
                .clearAndSetSemantics {
                    contentDescription = description
                    testTag = "month_list_row_${date.toIsoString()}"
                    role = Role.Button
                    selected = isSelected
                    onClick(label = description) {
                        onDayClick(date)
                        true
                    }
                }.padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = bsLong(date, bsMonthNames[date.monthValue - 1], settings.digits),
            color = ink.primary,
            style = MaterialTheme.typography.titleMedium,
        )
        if (isSelected && cell.isHoliday) {
            HolidayUnderline(width = 24.dp)
        }
        Text(
            text =
                stringResource(
                    R.string.month_list_weekday_ad,
                    weekdayName(date, lang),
                    adDate(date, monthNames, settings.digits),
                ),
            color = ink.secondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        DayListDetails(cell, lang, ink)
    }
}

@Composable
private fun DayListDetails(
    cell: MonthCell,
    lang: Lang,
    ink: DayInk,
) {
    if (cell.isHoliday) {
        Text(
            text = stringResource(R.string.public_holiday),
            color = ink.secondary,
            style = MaterialTheme.typography.labelLarge,
        )
    }
    cell.tithi?.let { tithi ->
        Text(
            text = tithi.name(lang),
            color = ink.secondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    cell.events.firstOrNull()?.let { event ->
        Text(
            text = event.name(lang),
            color = ink.secondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private const val DAYS_PER_WEEK = 7
