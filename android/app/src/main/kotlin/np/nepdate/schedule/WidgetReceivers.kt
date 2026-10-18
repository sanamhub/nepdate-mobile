package np.nepdate.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import np.nepdate.widget.appContainer

/**
 * Fires in the inexact window after local midnight, updates every widget, clears W-03's shown month
 * and re-arms tomorrow's alarm. It uses `goAsync` rather than blocking the main thread.
 */
class MidnightAlarmReceiver : BroadcastReceiver() {
    @Suppress("TooGenericExceptionCaught") // The goAsync result must finish after any failure.
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                refreshWidgets(context, resetMonth = true)
            } catch (error: Exception) {
                Log.e(TAG, "Midnight widget refresh failed", error)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "NepdateWidgets"
    }
}

/**
 * Re-renders and re-arms after a clock, time-zone, locale or boot change. These broadcasts are
 * exempt from the implicit-broadcast limits (ADR-0006).
 */
class SystemChangeReceiver : BroadcastReceiver() {
    @Suppress("TooGenericExceptionCaught") // The goAsync result must finish after any failure.
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val resetMonth =
            when (intent.action) {
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_BOOT_COMPLETED,
                -> true
                Intent.ACTION_LOCALE_CHANGED -> false
                else -> return
            }
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                refreshWidgets(context, resetMonth)
            } catch (error: Exception) {
                Log.e(TAG, "Widget refresh after ${intent.action} failed", error)
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        const val TAG = "NepdateWidgets"
    }
}

/** Reads the current settings, refreshes the widgets and re-arms the alarm. */
internal suspend fun refreshWidgets(
    context: Context,
    resetMonth: Boolean,
) {
    val container = appContainer(context)
    val settings = container.settingsRepository.settings.first()
    container.widgetRefresh.refresh(settings, resetMonth)
}
