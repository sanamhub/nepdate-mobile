package np.nepdate.widget

import android.content.Context
import np.nepdate.R

/**
 * The localised strings a widget model needs. Models take this instead of a Context so their
 * builders stay pure and unit-testable.
 *
 * [relative] supplies "Today", "Tomorrow" or "in N days"; [dataEndsOn] supplies the W-02 footer.
 */
internal class WidgetCopy(
    val nepal: String,
    val publicHoliday: String,
    val previousLabel: String,
    val nextLabel: String,
    val todayLabel: String,
    val bsMonthNames: List<String>,
    val adMonthNames: List<String>,
    val adMonthShortNames: List<String>,
    private val today: String,
    private val tomorrow: String,
    private val inDays: (Int) -> String,
    private val dataEnds: (String) -> String,
) {
    fun relative(days: Int): String =
        when (days) {
            0 -> today
            1 -> tomorrow
            else -> inDays(days)
        }

    fun dataEndsOn(date: String): String = dataEnds.invoke(date)
}

/** Reads the widget strings for the current app locale. */
internal fun widgetCopy(context: Context): WidgetCopy {
    val resources = context.resources
    return WidgetCopy(
        nepal = resources.getString(R.string.widget_nepal),
        publicHoliday = resources.getString(R.string.public_holiday),
        previousLabel = resources.getString(R.string.month_previous),
        nextLabel = resources.getString(R.string.month_next),
        todayLabel = resources.getString(R.string.month_today),
        bsMonthNames = resources.getStringArray(R.array.bs_month_long).toList(),
        adMonthNames = resources.getStringArray(R.array.ad_month_long).toList(),
        adMonthShortNames = resources.getStringArray(R.array.ad_month_short).toList(),
        today = resources.getString(R.string.month_today),
        tomorrow = resources.getString(R.string.upcoming_tomorrow),
        inDays = { days -> resources.getQuantityString(R.plurals.upcoming_in_days, days, days) },
        dataEnds = { date -> resources.getString(R.string.upcoming_data_ends, date) },
    )
}
