package np.nepdate.ui.month

import io.github.sanamhub.nepdate.Lang
import np.nepdate.data.Digits
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.weekdayName

/**
 * The TalkBack label for one cell (MR-10 AC1), for example
 * `Asoj 25, 2083, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata`.
 *
 * A neighbouring month's cell has only its date, because its holiday and event marks belong to the
 * month that owns the day. Only the first event is spoken; the day sheet lists them all.
 */
internal fun dayDescription(
    cell: MonthCell,
    lang: Lang,
    digits: Digits,
    bsMonthNames: List<String>,
    monthNames: List<String>,
    publicHoliday: String,
): String {
    val date = cell.date ?: return ""
    val parts =
        mutableListOf(
            bsLong(date, bsMonthNames[date.monthValue - 1], digits),
            weekdayName(date, lang),
            adDate(date, monthNames, digits),
        )
    if (cell.isHoliday) {
        parts += publicHoliday
    }
    cell.events.firstOrNull()?.let { parts += it.name(lang).inDigits(digits) }
    return parts.joinToString(", ")
}
