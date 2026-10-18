package np.nepdate.ui.day

import android.content.Intent
import android.provider.CalendarContract
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toLocalDate
import java.time.ZoneId

/** Builds the system event-editor intent for MR-02 AC2. */
object CalendarInsert {
    /**
     * An all-day insert for [date] at local midnight in [zone].
     *
     * @param date the BS day to add
     * @param title the holiday or event name
     * @param zone the device zone at the moment of the tap
     * @return an `ACTION_INSERT` intent for the calendar provider
     */
    fun intent(
        date: NepaliDate,
        title: String,
        zone: ZoneId,
    ): Intent {
        val start =
            date
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        val end =
            date
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
        }
    }
}
