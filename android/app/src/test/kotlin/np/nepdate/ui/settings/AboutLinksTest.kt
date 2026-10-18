package np.nepdate.ui.settings

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-06 and MR-07 AC2 and MR-12 AC2 link and share constants. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AboutLinksTest {
    @Test
    fun aboutUrlsAreTheOnesTheRequirementNames() {
        assertEquals(
            "https://github.com/sanamhub/nepdate/blob/main/data/README.md#known-data-questions-to-raise-upstream",
            KNOWN_DATA_URL,
        )
        assertEquals(
            "https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml",
            REPORT_DATE_URL,
        )
        assertEquals("", HOLIDAYS_ICS_URL)
    }

    @Test
    fun subscriptionShareCarriesTheUrlAsText() {
        val intent = holidayShareIntent("https://example.test/holidays.ics")

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("https://example.test/holidays.ics", intent.getStringExtra(Intent.EXTRA_TEXT))
    }
}
