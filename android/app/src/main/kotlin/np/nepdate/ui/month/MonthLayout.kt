package np.nepdate.ui.month

import androidx.compose.runtime.Immutable
import io.github.sanamhub.nepdate.Month
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import io.github.sanamhub.nepdate.patro.Named
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.data.Settings

/**
 * One square of the month grid. [date] is null only when the grid reaches past the library's range
 * (BS 1901-01-01..2199-12-30); the cell is then blank.
 *
 * A cell outside the shown month is dimmed and not tappable. Its holiday, event and tithi fields are
 * empty, because the marks belong to the month that owns the day.
 */
@Immutable
data class MonthCell(
    val date: NepaliDate?,
    val bsDay: Int,
    val adDay: Int,
    val isSaturday: Boolean,
    val isHoliday: Boolean,
    val isToday: Boolean,
    val isInMonth: Boolean,
    val tithi: Named?,
    val events: List<Named>,
) {
    /** Whether the event dot shows. */
    val hasEvents: Boolean get() = events.isNotEmpty()
}

/**
 * A 5- or 6-row month grid, Sunday first (MR-01 AC7). The rows are 35 or 42 cells so the grid never
 * changes width; the leading and trailing cells belong to the neighbouring months.
 */
@Immutable
data class MonthLayout(
    val year: Int,
    val month: Month,
    val firstDay: NepaliDate,
    val lastDay: NepaliDate,
    val leadingDays: Int,
    val cells: List<MonthCell>,
) {
    /** The number of week rows: 5 or 6. */
    val rowCount: Int get() = cells.size / DAYS_PER_WEEK

    /** The BS day numbers that are public holidays in this month, in order. */
    val holidayDays: List<Int>
        get() = cells.filter { it.isInMonth && it.isHoliday }.map { it.bsDay }

    private companion object {
        const val DAYS_PER_WEEK = 7
    }
}

/**
 * Lays out one BS month from the library's calendar and patro data (A1-02). No calendar arithmetic
 * happens here: the month length, the weekday and the neighbouring dates all come from [NepaliDate].
 *
 * @param year the BS year, 1901..2199
 * @param monthValue the BS month, 1..12
 * @param settings the tithi switch decides whether a cell carries its tithi
 * @param today the day that gets the today ring
 * @return the 35 or 42 cells and the month's first and last day
 */
fun monthLayout(
    year: Int,
    monthValue: Int,
    settings: Settings,
    today: NepaliDate,
): MonthLayout {
    val month = Month.of(monthValue)
    val first = NepaliDate.of(year, month, 1)
    val last = first.lastDayOfMonth()
    val leading = first.dayOfWeek.ordinal
    val cellCount = if (leading + last.dayOfMonth <= SHORT_GRID) SHORT_GRID else LONG_GRID
    val cells =
        List(cellCount) { index ->
            val offset = index - leading
            when {
                offset < 0 -> cellBefore(first, -offset, settings, today)
                offset < last.dayOfMonth -> cellOf(first.plusDays(offset.toLong()), true, settings, today)
                else -> cellAfter(last, offset - last.dayOfMonth + 1, settings, today)
            }
        }
    return MonthLayout(
        year = year,
        month = month,
        firstDay = first,
        lastDay = last,
        leadingDays = leading,
        cells = cells,
    )
}

private fun cellBefore(
    first: NepaliDate,
    daysBefore: Int,
    settings: Settings,
    today: NepaliDate,
): MonthCell {
    val epochDay = first.toEpochDay() - daysBefore
    val date = if (epochDay < NepaliDate.MIN.toEpochDay()) null else first.minusDays(daysBefore.toLong())
    return cellOf(date, inMonth = false, settings, today)
}

private fun cellAfter(
    last: NepaliDate,
    daysAfter: Int,
    settings: Settings,
    today: NepaliDate,
): MonthCell {
    val epochDay = last.toEpochDay() + daysAfter
    val date = if (epochDay > NepaliDate.MAX.toEpochDay()) null else last.plusDays(daysAfter.toLong())
    return cellOf(date, inMonth = false, settings, today)
}

private fun cellOf(
    date: NepaliDate?,
    inMonth: Boolean,
    settings: Settings,
    today: NepaliDate,
): MonthCell {
    if (date == null) {
        return MonthCell(
            date = null,
            bsDay = 0,
            adDay = 0,
            isSaturday = false,
            isHoliday = false,
            isToday = false,
            isInMonth = false,
            tithi = null,
            events = emptyList(),
        )
    }
    val info = if (inMonth) Patro.info(date) else null
    return MonthCell(
        date = date,
        bsDay = date.dayOfMonth,
        adDay = date.gregorianDay,
        isSaturday = date.dayOfWeek == Weekday.SATURDAY,
        isHoliday = info?.isPublicHoliday == true,
        isToday = date == today,
        isInMonth = inMonth,
        tithi = if (settings.showTithi) info?.tithi else null,
        events = info?.events.orEmpty(),
    )
}

internal const val SHORT_GRID = 35
internal const val LONG_GRID = 42
