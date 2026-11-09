package np.nepdate.widget

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.common.weekdayHeaders
import np.nepdate.ui.month.MonthCell
import np.nepdate.ui.month.monthLayout

/**
 * One upcoming holiday or event in a widget. The strings are already localised and already use the
 * resolved Digits setting, so a widget never formats a number itself (MR-07 AC1, A1 review).
 */
internal data class WidgetUpcomingEntry(
    val date: NepaliDate,
    val bsDate: String,
    val name: String,
    val relative: String,
    val isHoliday: Boolean,
)

/** The W-01 model: every string the Today widget draws. */
internal data class TodayWidgetModel(
    val date: NepaliDate,
    val bsDay: String,
    val monthName: String,
    val weekday: String,
    val weekdayShort: String,
    val adLine: String,
    val adShortLine: String,
    val oneLine: String,
    val holidayName: String?,
    val isHoliday: Boolean,
    val isSaturday: Boolean,
    val nepalLabel: String?,
    val nextHoliday: WidgetUpcomingEntry?,
    val bsMonthYear: String = "",
    val bsShortLine: String = "",
    val adDay: String = "",
    val adMonthYear: String = "",
    val adMonthShort: String = "",
    val tithi: String = "",
    val eventName: String? = null,
    val bsLongLine: String = "",
)

/**
 * The W-02 model: today on the left and the next days with a holiday or event on the right.
 * [todayRow] holds today's own holidays and events, or null on a day without any.
 */
internal data class UpcomingWidgetModel(
    val today: TodayWidgetModel,
    val entries: List<WidgetDayRow>,
    val dataEnds: String?,
    val todayRow: WidgetDayRow? = null,
)

/** One of the 35 or 42 day cells in W-03. */
internal data class MonthWidgetCell(
    val date: NepaliDate?,
    val bsDay: String,
    val adDay: String,
    val isInMonth: Boolean,
    val isHoliday: Boolean,
    val isSaturday: Boolean,
    val isToday: Boolean,
    val hasEvents: Boolean,
)

/** The W-03 model: a compact 5- or 6-week grid for one BS month. */
internal data class MonthWidgetModel(
    val year: Int,
    val month: Int,
    val title: String,
    val firstDay: NepaliDate,
    val lastDay: NepaliDate,
    val today: NepaliDate,
    val isCurrentMonth: Boolean,
    val weekdayHeaders: List<String>,
    val previousLabel: String,
    val nextLabel: String,
    val todayLabel: String,
    val cells: List<MonthWidgetCell>,
    val adSpan: String = "",
) {
    val rowCount: Int get() = cells.size / DAYS_PER_WEEK
}

/**
 * Builds the W-01 model. No calendar arithmetic is reimplemented here: the date, weekday and patro
 * data come from the library, and only the display strings are assembled.
 *
 * @param date today in the zone the settings choose
 * @param lang the resolved app language
 * @param digits the resolved Digits setting, never [Digits.FOLLOW_LANGUAGE]
 * @param copy the localised widget strings
 * @param showNepal true when the date follows Nepal and the device is in another zone
 */
internal fun todayWidgetModel(
    date: NepaliDate,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
    showNepal: Boolean,
): TodayWidgetModel {
    require(digits != Digits.FOLLOW_LANGUAGE) { "Resolve Digits before building a widget model" }
    val monthName = copy.bsMonthNames[date.monthValue - 1]
    val bsDay = date.dayOfMonth.toString().inDigits(digits)
    val weekday = date.dayOfWeek.displayName(lang)
    val weekdayShort = date.dayOfWeek.shortName(lang)
    val adLine = adDate(date, copy.adMonthNames, digits)
    val adShort =
        "${date.gregorianDay.toString().inDigits(digits)} " +
            copy.adMonthShortNames[date.gregorianMonth - 1]
    val info = if (date in Patro.coverage) Patro.info(date) else null
    val isHoliday = info?.isPublicHoliday == true
    val firstEvent = info?.events?.firstOrNull()?.name(lang)
    val holidayName = if (isHoliday) firstEvent ?: copy.publicHoliday else null
    val oneLine =
        if (holidayName == null) {
            "$monthName $bsDay $weekday, $adShort"
        } else {
            "$monthName $bsDay $weekday, $holidayName"
        }
    val nextHoliday = nextHoliday(after = date, lang = lang, digits = digits, copy = copy)
    return TodayWidgetModel(
        date = date,
        bsDay = bsDay,
        monthName = monthName,
        weekday = weekday,
        weekdayShort = weekdayShort,
        adLine = adLine,
        adShortLine = adShort,
        oneLine = oneLine,
        holidayName = holidayName,
        isHoliday = isHoliday,
        isSaturday = date.dayOfWeek == Weekday.SATURDAY,
        nepalLabel = if (showNepal) copy.nepal else null,
        nextHoliday = nextHoliday,
        bsMonthYear = monthTitle(monthName, date.year, digits),
        bsShortLine = "$monthName $bsDay",
        adDay = date.gregorianDay.toString().inDigits(digits),
        adMonthYear = monthTitle(copy.adMonthNames[date.gregorianMonth - 1], date.gregorianYear, digits),
        adMonthShort = copy.adMonthShortNames[date.gregorianMonth - 1],
        tithi = info?.tithi?.name(lang).orEmpty(),
        eventName = firstEvent,
        bsLongLine = bsDate(date, copy, digits),
    )
}

/**
 * Builds the W-02 model: up to [UPCOMING_MAX_ROWS] days after today, each with all its events on
 * one line. [UpcomingWidgetModel.dataEnds] is set only when the calendar data ends before the list
 * fills (W-02 AC2).
 */
internal fun upcomingWidgetModel(
    date: NepaliDate,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
    showNepal: Boolean,
): UpcomingWidgetModel {
    val list =
        upcomingDays(
            today = date,
            holidaysOnly = false,
            lang = lang,
            digits = digits,
            copy = copy,
            maxRows = UPCOMING_MAX_ROWS + 1,
            maxDays = Int.MAX_VALUE,
        )
    val entries = list.rows.filter { it.date != date }.take(UPCOMING_MAX_ROWS)
    return UpcomingWidgetModel(
        today = todayWidgetModel(date, lang, digits, copy, showNepal),
        entries = entries,
        todayRow = list.rows.firstOrNull { it.date == date },
        dataEnds =
            list.dataEndsOn?.takeIf { entries.size < UPCOMING_MAX_ROWS }?.let { end ->
                copy.dataEndsOn(bsDate(end, copy, digits)).inDigits(digits)
            },
    )
}

/** The most days the Today and upcoming widget lists. */
internal const val UPCOMING_MAX_ROWS = 4

/**
 * Builds the W-03 model for [year]-[monthValue]. The model keeps the 5- or 6-row grid from the app,
 * but carries only compact strings; no tithi line is drawn in the widget.
 */
internal fun monthWidgetModel(
    year: Int,
    monthValue: Int,
    today: NepaliDate,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
): MonthWidgetModel {
    require(digits != Digits.FOLLOW_LANGUAGE) { "Resolve Digits before building a widget model" }
    val layout = monthLayout(year, monthValue, Settings(showTithi = false), today)
    return MonthWidgetModel(
        year = year,
        month = monthValue,
        title = monthTitle(copy.bsMonthNames[monthValue - 1], year, digits),
        firstDay = layout.firstDay,
        lastDay = layout.lastDay,
        today = today,
        isCurrentMonth = year == today.year && monthValue == today.monthValue,
        weekdayHeaders = weekdayHeaders(lang),
        previousLabel = copy.previousLabel,
        nextLabel = copy.nextLabel,
        todayLabel = copy.todayLabel,
        cells = layout.cells.map { it.toWidgetCell(digits) },
        adSpan = adSpan(layout.firstDay, layout.lastDay, copy, digits),
    )
}

/**
 * The AD months a BS month spans, such as "Sep – Oct 2026", or "Dec 2026 – Jan 2027" across a year.
 * The form is the same in English and Nepali, so it is assembled here rather than read from a string.
 */
internal fun adSpan(
    first: NepaliDate,
    last: NepaliDate,
    copy: WidgetCopy,
    digits: Digits,
): String {
    val firstMonth = copy.adMonthShortNames[first.gregorianMonth - 1]
    val lastMonth = copy.adMonthShortNames[last.gregorianMonth - 1]
    val firstYear = first.gregorianYear.toString().inDigits(digits)
    val lastYear = last.gregorianYear.toString().inDigits(digits)
    return when {
        first.gregorianYear != last.gregorianYear -> "$firstMonth $firstYear – $lastMonth $lastYear"
        first.gregorianMonth != last.gregorianMonth -> "$firstMonth – $lastMonth $lastYear"
        else -> "$firstMonth $lastYear"
    }
}

/** The next public holiday after [after], or null when the coverage ends first. */
internal fun nextHoliday(
    after: NepaliDate,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
): WidgetUpcomingEntry? {
    var date = maxOf(after.plusDays(1), Patro.coverage.start)
    while (date <= Patro.coverage.endInclusive) {
        val info = Patro.info(date)
        if (info.isPublicHoliday) {
            val name = info.events.firstOrNull()?.name(lang) ?: copy.publicHoliday
            return WidgetUpcomingEntry(
                date = date,
                bsDate = bsDate(date, copy, digits),
                name = name,
                relative = copy.relative(after.daysUntil(date).toInt()).inDigits(digits),
                isHoliday = true,
            )
        }
        date = date.plusDays(1)
    }
    return null
}

internal fun np.nepdate.ui.upcoming.UpcomingEntry.toWidgetEntry(
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
): WidgetUpcomingEntry =
    WidgetUpcomingEntry(
        date = date,
        bsDate = bsDate(date, copy, digits),
        name = name.name(lang),
        relative = copy.relative(daysFromToday).inDigits(digits),
        isHoliday = isHoliday,
    )

private fun MonthCell.toWidgetCell(digits: Digits): MonthWidgetCell =
    MonthWidgetCell(
        date = date,
        bsDay = if (date == null) "" else bsDay.toString().inDigits(digits),
        adDay = if (date == null) "" else adDay.toString().inDigits(digits),
        isInMonth = isInMonth,
        isHoliday = isHoliday,
        isSaturday = isSaturday,
        isToday = isToday,
        hasEvents = hasEvents,
    )

internal fun bsDate(
    date: NepaliDate,
    copy: WidgetCopy,
    digits: Digits,
): String =
    "${copy.bsMonthNames[date.monthValue - 1]} ${date.dayOfMonth.toString().inDigits(digits)}, " +
        date.year.toString().inDigits(digits)

private const val DAYS_PER_WEEK = 7
