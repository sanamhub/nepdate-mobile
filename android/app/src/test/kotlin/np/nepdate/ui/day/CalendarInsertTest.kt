package np.nepdate.ui.day

import android.content.Intent
import android.provider.CalendarContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

/** A1-04 and MR-02 AC2: the event-editor intent's extras. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class CalendarInsertTest {
    private val asoj9 = NepaliDate.of(2083, 6, 9)
    private val kathmandu = ZoneId.of("Asia/Kathmandu")

    @Test
    fun allDayAtLocalMidnight() {
        val intent = CalendarInsert.intent(asoj9, "Indra Jatra (Kathmandu Valley Holiday)", kathmandu)

        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertTrue(intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false))
        assertEquals(
            "Indra Jatra (Kathmandu Valley Holiday)",
            intent.getStringExtra(CalendarContract.Events.TITLE),
        )
        assertEquals(
            LocalDate
                .of(2026, 9, 25)
                .atStartOfDay(kathmandu)
                .toInstant()
                .toEpochMilli(),
            intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
        )
        assertEquals(
            LocalDate
                .of(2026, 9, 26)
                .atStartOfDay(kathmandu)
                .toInstant()
                .toEpochMilli(),
            intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0),
        )
    }

    @Test
    fun midnightUsesTheGivenDeviceZone() {
        val newYork = ZoneId.of("America/New_York")
        val intent = CalendarInsert.intent(asoj9, "Indra Jatra", newYork)

        assertEquals(
            LocalDate
                .of(2026, 9, 25)
                .atStartOfDay(newYork)
                .toInstant()
                .toEpochMilli(),
            intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
        )
    }
}
