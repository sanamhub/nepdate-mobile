package np.nepdate.ui.settings

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.lang.reflect.Modifier

/** A1-06 and MR-07 AC2 and MR-12 AC2 link and share constants. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class AboutLinksTest {
    @Test
    fun aboutUrlsAreTheOnesTheRequirementNames() {
        assertEquals(
            "https://github.com/sanamhub/nepdate-mobile/blob/main/docs/product/known-data-questions.md",
            KNOWN_DATA_URL,
        )
        assertEquals(
            "https://github.com/sanamhub/nepdate-mobile/issues/new?template=wrong-date.yml",
            REPORT_DATE_URL,
        )
        assertEquals("", HOLIDAYS_ICS_URL)
    }

    @Test
    fun everyUrlInAboutLinksIsInAPublicRepository() {
        val urls = aboutLinkUrls()

        assertEquals(
            setOf(KNOWN_DATA_URL, REPORT_DATE_URL, LICENCES_URL, PRIVACY_URL, SOURCE_URL),
            urls.toSet(),
        )
        for (url in urls) {
            assertNull("$url should be allowed", aboutUrlProblem(url))
        }
    }

    @Test
    fun theUnpublishedHubUrlIsRejected() {
        val oldHubUrl =
            "https://github.com/sanamhub/nepdate/blob/main/data/README.md" +
                "#known-data-questions-to-raise-upstream"

        assertNotNull(aboutUrlProblem(oldHubUrl))
    }

    @Test
    fun linksOutsideTheAllowListAreRejected() {
        assertNotNull(aboutUrlProblem("http://github.com/sanamhub/nepdate-mobile"))
        assertNotNull(aboutUrlProblem("https://example.test/sanamhub/nepdate-mobile"))
        assertNotNull(aboutUrlProblem("https://github.com/sanamhub/nepdate"))
        assertNotNull(aboutUrlProblem("https://github.com/another/repository"))
        assertNotNull(aboutUrlProblem("https://github.com/sanamhub/nepdate-mobile/../nepdate"))
    }

    @Test
    fun subscriptionShareCarriesTheUrlAsText() {
        val intent = holidayShareIntent("https://example.test/holidays.ics")

        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("text/plain", intent.type)
        assertEquals("https://example.test/holidays.ics", intent.getStringExtra(Intent.EXTRA_TEXT))
    }

    private fun aboutLinkUrls(): List<String> =
        Class
            .forName("np.nepdate.ui.settings.AboutLinksKt")
            .declaredFields
            .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .map { field ->
                field.isAccessible = true
                field.get(null) as String
            }.filter { it.startsWith("http") }
}
