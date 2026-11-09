package np.nepdate.widget

import android.content.Context
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.upcoming.upcoming

/**
 * The localised strings for the widget controls and the two festival widgets. Kept apart from
 * [WidgetCopy] so the existing models and their tests keep their shape.
 */
internal class WidgetControlCopy(
    val showAd: String,
    val showBs: String,
    val nextHolidayKind: String,
    val nextFestivalKind: String,
    val previousFestival: String,
    val followingFestival: String,
    val listTitle: String,
    val holidaysOnly: String,
    val holidaysOnlyOn: String,
    val holidaysOnlyOff: String,
    val noneLeft: String,
    private val daysUnit: (Int) -> String,
    private val dateLine: (String, String) -> String,
) {
    fun unit(days: Int): String = daysUnit(days)

    fun weekdayAndDate(
        weekday: String,
        date: String,
    ): String = dateLine(weekday, date)
}

/** Reads the control strings for the current app locale. */
internal fun widgetControlCopy(context: Context): WidgetControlCopy {
    val resources = context.resources
    return WidgetControlCopy(
        showAd = resources.getString(R.string.widget2_show_ad),
        showBs = resources.getString(R.string.widget2_show_bs),
        nextHolidayKind = resources.getString(R.string.widget2_next_holiday_kind),
        nextFestivalKind = resources.getString(R.string.widget2_next_festival_kind),
        previousFestival = resources.getString(R.string.widget2_previous_festival),
        followingFestival = resources.getString(R.string.widget2_following_festival),
        listTitle = resources.getString(R.string.widget2_festivals_title),
        holidaysOnly = resources.getString(R.string.widget2_holidays_only),
        holidaysOnlyOn = resources.getString(R.string.widget2_holidays_only_on),
        holidaysOnlyOff = resources.getString(R.string.widget2_holidays_only_off),
        noneLeft = resources.getString(R.string.widget2_none_left),
        daysUnit = { days -> resources.getQuantityString(R.plurals.widget2_days_unit, days) },
        dateLine = { weekday, date -> resources.getString(R.string.widget2_date_line, weekday, date) },
    )
}

/** What the Next festival widget draws for the entry at [index] in the browse window. */
internal data class NextFestivalModel(
    val entry: WidgetUpcomingEntry?,
    val kind: String,
    val days: String,
    val unit: String,
    val dateLine: String,
    val index: Int,
    val previousIndex: Int?,
    val nextIndex: Int?,
    val emptyText: String,
) {
    val isHoliday: Boolean get() = entry?.isHoliday == true
}

/** What the Festivals list widget draws. */
internal data class FestivalsListModel(
    val rows: List<WidgetDayRow>,
    val holidaysOnly: Boolean,
    val emptyText: String,
)

/**
 * Builds the Next festival model. The arrows step through the next [FESTIVAL_WINDOW] holidays and
 * festivals and wrap at either end, as in the design. An [index] outside the window falls back to
 * the nearest entry, so a stale index after the data changes never fails.
 */
internal fun nextFestivalModel(
    today: NepaliDate,
    index: Int,
    lang: Lang,
    digits: Digits,
    copy: WidgetCopy,
    controls: WidgetControlCopy,
): NextFestivalModel {
    require(digits != Digits.FOLLOW_LANGUAGE) { "Resolve Digits before building a widget model" }
    val entries = festivalWindow(today)
    if (entries.isEmpty()) {
        return NextFestivalModel(
            entry = null,
            kind = controls.nextFestivalKind,
            days = "",
            unit = "",
            dateLine = "",
            index = 0,
            previousIndex = null,
            nextIndex = null,
            emptyText = controls.noneLeft,
        )
    }
    val shown = if (index in entries.indices) index else 0
    val source = entries[shown]
    val days = source.daysFromToday
    val canBrowse = entries.size > 1
    return NextFestivalModel(
        entry = source.toWidgetEntry(lang, digits, copy),
        kind = if (source.isHoliday) controls.nextHolidayKind else controls.nextFestivalKind,
        days = if (days == 0) copy.relative(0) else days.toString().inDigits(digits),
        unit = if (days == 0) "" else controls.unit(days),
        dateLine =
            controls.weekdayAndDate(
                weekday = source.date.dayOfWeek.displayName(lang),
                date = shortBsDate(source.date, copy, digits),
            ),
        index = shown,
        previousIndex = if (canBrowse) (shown - 1 + entries.size) % entries.size else null,
        nextIndex = if (canBrowse) (shown + 1) % entries.size else null,
        emptyText = controls.noneLeft,
    )
}

/**
 * Builds the Festivals list model: every upcoming day with a holiday or an event by default, or only
 * public holidays when [holidaysOnly] is on, capped by [upcomingDays].
 */
internal fun festivalsListModel(
    today: NepaliDate,
    holidaysOnly: Boolean,
    digits: Digits,
    lang: Lang,
    copy: WidgetCopy,
    controls: WidgetControlCopy,
): FestivalsListModel {
    require(digits != Digits.FOLLOW_LANGUAGE) { "Resolve Digits before building a widget model" }
    return FestivalsListModel(
        rows = upcomingDays(today, holidaysOnly, lang, digits, copy).rows,
        holidaysOnly = holidaysOnly,
        emptyText = controls.noneLeft,
    )
}

/**
 * The entries the Next festival arrows browse: the nearest festivals, plus the next few public
 * holidays even when many smaller observances fall before them, in date order and capped at
 * [FESTIVAL_WINDOW].
 */
internal fun festivalWindow(today: NepaliDate): List<np.nepdate.ui.upcoming.UpcomingEntry> {
    val scanned = upcoming(today, limit = HOLIDAY_SCAN_LIMIT).entries
    val first = scanned.take(FESTIVAL_WINDOW)
    val laterHolidays = scanned.filter { it.isHoliday }.take(WINDOW_HOLIDAYS).filter { it !in first }
    val chosen = (first.take(FESTIVAL_WINDOW - laterHolidays.size) + laterHolidays).toSet()
    return scanned.filter { it in chosen }
}

private fun shortBsDate(
    date: NepaliDate,
    copy: WidgetCopy,
    digits: Digits,
): String = "${copy.bsMonthNames[date.monthValue - 1]} ${date.dayOfMonth.toString().inDigits(digits)}"

/** How many upcoming entries the Next festival arrows browse before wrapping. */
internal const val FESTIVAL_WINDOW = 12

/** How many public holidays the browse window always reaches. */
private const val WINDOW_HOLIDAYS = 3

/** How many events are scanned to find the next few public holidays for the filtered list. */
private const val HOLIDAY_SCAN_LIMIT = 400
