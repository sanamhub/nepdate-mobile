package np.nepdate.schedule

import android.util.Log
import androidx.glance.appwidget.updateAll
import np.nepdate.data.Settings
import np.nepdate.widget.MonthGlanceWidget
import np.nepdate.widget.TodayGlanceWidget
import np.nepdate.widget.UpcomingGlanceWidget

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
    override suspend fun updateAll() {
        TodayGlanceWidget().updateAll(context)
        UpcomingGlanceWidget().updateAll(context)
        MonthGlanceWidget().updateAll(context)
        Log.d(TAG, "Widgets updated")
    }

    override suspend fun clearShownMonth() {
        MonthGlanceWidget().clearShownMonth(context)
    }

    private companion object {
        const val TAG = "NepdateWidgets"
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
}
