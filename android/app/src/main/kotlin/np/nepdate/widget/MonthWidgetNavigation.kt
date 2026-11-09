package np.nepdate.widget

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import io.github.sanamhub.nepdate.NepaliDate

/** The Glance state keys for W-03's shown month. */
internal val SHOWN_YEAR_KEY = intPreferencesKey("shown_year")
internal val SHOWN_MONTH_KEY = intPreferencesKey("shown_month")

internal val YEAR_PARAM = ActionParameters.Key<Int>("year")
internal val MONTH_PARAM = ActionParameters.Key<Int>("month")

/** The shown month, falling back to [today] when no navigation state is stored. */
internal fun shownMonth(
    preferences: Preferences,
    today: NepaliDate,
): Pair<Int, Int> {
    val year = preferences[SHOWN_YEAR_KEY]
    val month = preferences[SHOWN_MONTH_KEY]
    return if (year != null && month != null && isValidShownMonth(year, month)) {
        year to month
    } else {
        today.year to today.monthValue
    }
}

/** Whether [year]-[month] is inside the library's BS range and the 12 BS months. */
internal fun isValidShownMonth(
    year: Int,
    month: Int,
): Boolean = year in NepaliDate.MIN.year..NepaliDate.MAX.year && month in 1..MONTHS_PER_YEAR

/** The month one step before or after [year]-[month], or null at the library's range edge. */
internal fun monthStep(
    year: Int,
    month: Int,
    delta: Int,
): Pair<Int, Int>? {
    val nextMonth = month + delta
    return when {
        nextMonth == 0 && year == NepaliDate.MIN.year -> null
        nextMonth == MONTHS_PER_YEAR + 1 && year == NepaliDate.MAX.year -> null
        nextMonth == 0 -> (year - 1) to MONTHS_PER_YEAR
        nextMonth == MONTHS_PER_YEAR + 1 -> (year + 1) to 1
        else -> year to nextMonth
    }
}

/** The target of the previous/next callbacks (W-03 AC1). */
internal fun monthNavigation(
    model: MonthWidgetModel,
    delta: Int,
): Pair<Int, Int>? = monthStep(model.year, model.month, delta)

/**
 * Applies a previous/next/Today tap to the Glance state and then asks the widget to redraw. The
 * update lambda is injectable so the state transition can be tested without a session.
 */
internal suspend fun applyMonthNavigation(
    context: Context,
    glanceId: GlanceId,
    year: Int,
    month: Int,
    update: suspend (Context, GlanceId) -> Unit,
) {
    if (!isValidShownMonth(year, month)) return
    androidx.glance.appwidget.state.updateAppWidgetState(context, glanceId) { preferences ->
        preferences[SHOWN_YEAR_KEY] = year
        preferences[SHOWN_MONTH_KEY] = month
    }
    update(context, glanceId)
}

/**
 * The previous/next/today callback. Parameters carry the target month computed at render time.
 * [Keep] stops R8 full mode from removing the constructor Glance calls by reflection.
 */
@Keep
internal class MonthNavigationAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val year = parameters[YEAR_PARAM] ?: return
        val month = parameters[MONTH_PARAM] ?: return
        applyMonthNavigation(context, glanceId, year, month) { ctx, id ->
            MonthGlanceWidget().update(ctx, id)
        }
    }
}

private const val MONTHS_PER_YEAR = 12
