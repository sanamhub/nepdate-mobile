package np.nepdate.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider

/** The one inexact alarm the widgets use (ADR-0006). */
internal fun interface MidnightAlarm {
    fun arm(
        triggerAtMillis: Long,
        windowMillis: Long,
    )
}

/**
 * Arms one inexact alarm at the next local midnight in the zone the "Date follows" setting chooses.
 * It is re-armed after every fire and after a clock, time-zone or settings change.
 */
internal class MidnightScheduler(
    private val todayProvider: TodayProvider,
    private val alarm: MidnightAlarm,
) {
    fun schedule(settings: Settings) {
        val triggerAt = todayProvider.nextMidnight(settings.dateFollows).toInstant().toEpochMilli()
        alarm.arm(triggerAt, WINDOW_MILLIS)
    }

    companion object {
        /** The inexact window from A2-01 and ADR-0006. */
        const val WINDOW_MILLIS: Long = 10 * 60 * 1000L
    }
}

/** The production alarm. The request code is fixed, so re-arming replaces the previous alarm. */
internal class AndroidMidnightAlarm(
    context: Context,
) : MidnightAlarm {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val operation =
        PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            Intent(appContext, MidnightAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    override fun arm(
        triggerAtMillis: Long,
        windowMillis: Long,
    ) {
        alarmManager.setWindow(AlarmManager.RTC, triggerAtMillis, windowMillis, operation)
    }

    fun cancel() {
        alarmManager.cancel(operation)
    }

    private companion object {
        const val REQUEST_CODE = 2083
    }
}
