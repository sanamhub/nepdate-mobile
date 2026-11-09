package np.nepdate.widget

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.data.Digits
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.upcoming.upcoming

/**
 * One day in a widget list: every holiday and event on that date joined into one line, so a busy
 * day such as Laxmi Puja takes one row instead of crowding out the days after it.
 */
internal data class WidgetDayRow(
    val date: NepaliDate,
    val bsDay: String,
    val weekdayShort: String,
    val names: String,
    val relative: String,
    val isHoliday: Boolean,
)

/** The days a list widget shows, and the end of the calendar data when the list reaches it. */
internal data class WidgetDayList(
    val rows: List<WidgetDayRow>,
    val dataEndsOn: NepaliDate?,
)

/**
 * The upcoming days from [today] with at least one holiday or event, at most [maxRows] of them and
 * none more than [maxDays] days away. With [holidaysOnly] only public holidays count, and the day
 * limit widens to a year so the filter still has something to show.
 */
internal fun upcomingDays(
    today: NepaliDate,
    holidaysOnly: Boolean,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
    maxRows: Int = LIST_MAX_ROWS,
    maxDays: Int = if (holidaysOnly) HOLIDAY_WINDOW_DAYS else LIST_WINDOW_DAYS,
): WidgetDayList {
    val scanned = upcoming(today, limit = SCAN_LIMIT)
    val rows =
        scanned.entries
            .filter { it.daysFromToday <= maxDays && (!holidaysOnly || it.isHoliday) }
            .groupBy { it.date }
            .entries
            .take(maxRows)
            .map { (date, entries) ->
                WidgetDayRow(
                    date = date,
                    bsDay = date.dayOfMonth.toString().inDigits(digits),
                    weekdayShort = date.dayOfWeek.shortName(lang),
                    names = entries.joinToString(", ") { it.name.name(lang) },
                    relative = copy.relative(entries.first().daysFromToday).inDigits(digits),
                    isHoliday = entries.any { it.isHoliday },
                )
            }
    return WidgetDayList(rows, scanned.dataEndsOn)
}

/** One day of the Week widget. */
internal data class WeekDayCell(
    val date: NepaliDate,
    val bsDay: String,
    val weekdayShort: String,
    val isToday: Boolean,
    val isHoliday: Boolean,
    val isSaturday: Boolean,
    val hasEvents: Boolean,
)

/** The Week widget: the Sunday-to-Saturday week around today and the next day with an event. */
internal data class WeekWidgetModel(
    val title: String,
    val days: List<WeekDayCell>,
    val next: WidgetDayRow?,
)

/** Builds the Week widget for the week that contains [today]. */
internal fun weekWidgetModel(
    today: NepaliDate,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
): WeekWidgetModel {
    require(digits != Digits.FOLLOW_LANGUAGE) { "Resolve Digits before building a widget model" }
    val offset = Weekday.entries.indexOf(today.dayOfWeek)
    val days =
        (0 until DAYS_IN_WEEK).map { index ->
            val date = today.plusDays((index - offset).toLong())
            val info = if (date in Patro.coverage) Patro.info(date) else null
            WeekDayCell(
                date = date,
                bsDay = date.dayOfMonth.toString().inDigits(digits),
                weekdayShort = date.dayOfWeek.shortName(lang),
                isToday = date == today,
                isHoliday = info?.isPublicHoliday == true,
                isSaturday = date.dayOfWeek == Weekday.SATURDAY,
                hasEvents = info?.events?.isNotEmpty() == true,
            )
        }
    val next =
        upcomingDays(today, holidaysOnly = false, lang = lang, digits = digits, copy = copy, maxRows = 2)
            .rows
            .firstOrNull { it.date != today }
    return WeekWidgetModel(
        title = monthTitle(copy.bsMonthNames[today.monthValue - 1], today.year, digits),
        days = days,
        next = next,
    )
}

/** The most rows a scrolling list widget holds (Festivals list, item 7 of the widget review). */
internal const val LIST_MAX_ROWS = 20

/** How far ahead the full festival list looks. */
internal const val LIST_WINDOW_DAYS = 60

/** How far ahead the holidays only list looks. */
private const val HOLIDAY_WINDOW_DAYS = 366

/** Events scanned for a list; enough to cover a year of public holidays. */
private const val SCAN_LIMIT = 600

private const val DAYS_IN_WEEK = 7
