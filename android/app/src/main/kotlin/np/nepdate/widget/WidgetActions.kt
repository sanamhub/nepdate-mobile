package np.nepdate.widget

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState

// Every ActionCallback here carries @Keep. Glance builds a callback with
// `getDeclaredConstructor().newInstance()` and only logs a failure; its consumer rule keeps the class
// but, under R8 full mode, not the no-argument constructor, so a shrunk build silently ignored taps.

// Per-widget state for the interactive controls. Each key lives in the placed widget's own Glance
// preferences, next to the opacity and the shown month, so two placed copies never share a choice.

/** True when the Today widget shows the AD face. */
internal val TODAY_SHOW_AD_KEY = booleanPreferencesKey("today_show_ad")

/** The Next festival widget's position in its browse window; cleared at the midnight refresh. */
internal val FESTIVAL_INDEX_KEY = intPreferencesKey("festival_index")

/** True when the Festivals list shows public holidays only. */
internal val HOLIDAYS_ONLY_KEY = booleanPreferencesKey("festivals_holidays_only")

internal val FESTIVAL_INDEX_PARAM = ActionParameters.Key<Int>("festival_index")

/**
 * Writes one change to [glanceId]'s state and then redraws only that widget. The redraw is
 * injectable so the state change can be tested without a Glance session.
 */
internal suspend fun changeWidgetState(
    context: Context,
    glanceId: GlanceId,
    change: (MutablePreferences) -> Unit,
    redraw: suspend (Context, GlanceId) -> Unit,
) {
    updateAppWidgetState(context, glanceId) { preferences -> change(preferences) }
    redraw(context, glanceId)
}

/** Flips the Today widget between its BS and AD faces. */
@Keep
internal class TodayFaceAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        changeWidgetState(
            context,
            glanceId,
            change = { it[TODAY_SHOW_AD_KEY] = !(it[TODAY_SHOW_AD_KEY] ?: false) },
            redraw = { ctx, id -> TodayGlanceWidget().update(ctx, id) },
        )
    }
}

/** Moves the Next festival widget to the index computed at render time. */
@Keep
internal class FestivalStepAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val index = parameters[FESTIVAL_INDEX_PARAM] ?: return
        if (index !in 0 until FESTIVAL_WINDOW) return
        changeWidgetState(
            context,
            glanceId,
            change = { it[FESTIVAL_INDEX_KEY] = index },
            redraw = { ctx, id -> NextFestivalGlanceWidget().update(ctx, id) },
        )
    }
}

/** Toggles the Festivals list's public holidays only filter. */
@Keep
internal class HolidayFilterAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        changeWidgetState(
            context,
            glanceId,
            change = { it[HOLIDAYS_ONLY_KEY] = !(it[HOLIDAYS_ONLY_KEY] ?: false) },
            redraw = { ctx, id -> FestivalsGlanceWidget().update(ctx, id) },
        )
    }
}
