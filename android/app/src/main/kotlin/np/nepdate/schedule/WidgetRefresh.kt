package np.nepdate.schedule

import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import np.nepdate.data.Settings
import np.nepdate.widget.MonthGlanceWidget
import np.nepdate.widget.TodayGlanceWidget
import np.nepdate.widget.UpcomingGlanceWidget
import np.nepdate.widget.WIDGET_REFRESH_KEY
import java.util.concurrent.atomic.AtomicLong

/** The widget work a receiver, the settings observer or the app open triggers. */
internal interface WidgetUpdater {
    suspend fun updateAll()

    /** Clears W-03's shown month so the next update draws the current month (W-03 AC1). */
    suspend fun clearShownMonth()
}

/** Production updater. All three widgets are updated, whether or not an instance is placed. */
internal class GlanceWidgetUpdater(
    private val context: android.content.Context,
) : WidgetUpdater {
    /**
     * Writes a new [WIDGET_REFRESH_KEY] into each placed widget's state before updating it, so a
     * Glance session that is still running reloads its environment instead of redrawing the old one.
     */
    override suspend fun updateAll() {
        val token = nextRefreshToken()
        val manager = GlanceAppWidgetManager(context)
        listOf(TodayGlanceWidget(), UpcomingGlanceWidget(), MonthGlanceWidget()).forEach { widget ->
            manager.getGlanceIds(widget.javaClass).forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { preferences ->
                    preferences[WIDGET_REFRESH_KEY] = token
                }
                widget.update(context, glanceId)
            }
        }
        Log.d(TAG, "Widgets updated")
    }

    override suspend fun clearShownMonth() {
        MonthGlanceWidget().clearShownMonth(context)
    }

    private companion object {
        const val TAG = "NepdateWidgets"
        val lastToken = AtomicLong()

        /** A value no earlier refresh in this process used, and later than any earlier process's. */
        fun nextRefreshToken(): Long = lastToken.updateAndGet { maxOf(it + 1, System.currentTimeMillis()) }
    }
}

/** Refreshes widgets and re-arms the one midnight alarm (ADR-0006). */
internal class WidgetRefresh(
    private val updater: WidgetUpdater,
    private val scheduler: MidnightScheduler,
) {
    suspend fun refresh(
        settings: Settings,
        resetMonth: Boolean,
    ) {
        if (resetMonth) updater.clearShownMonth()
        updater.updateAll()
        scheduler.schedule(settings)
    }

    /**
     * Arms the midnight alarm without drawing. Called at every process start, so a widget placed
     * before the app was ever opened, or a process restarted after a force stop, still gets it.
     */
    fun rearm(settings: Settings) {
        scheduler.schedule(settings)
    }
}
