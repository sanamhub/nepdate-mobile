package np.nepdate.ui.day

import android.content.Intent
import android.provider.CalendarContract
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

/**
 * A1-04 and MR-02 AC2 on a device: the exact extras the day sheet sends to the calendar editor.
 * ADR-0008 runs instrumented tests on the nightly managed devices, not on every pull request.
 */
@RunWith(AndroidJUnit4::class)
class CalendarInsertInstrumentedTest {
    @Test
    fun allDayInsertCarriesTheLocalMidnightAndTitle() {
        val zone = ZoneId.of("Asia/Kathmandu")
        val intent =
            CalendarInsert.intent(
                NepaliDate.of(2083, 6, 9),
                "Indra Jatra (Kathmandu Valley Holiday)",
                zone,
            )

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
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli(),
            intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
        )
    }
}
