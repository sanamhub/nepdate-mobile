package np.nepdate.ui.day

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-04 and MR-02 AC1, AC3, AC4, AC5 and AC6. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class DayDetailTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val english = Settings(digits = Digits.ENGLISH)

    @Test
    fun mr02Ac1AndAc5ShowTheDateTithiHolidayAndEventsInOrder() {
        setDayDetail(NepaliDate.of(2083, 6, 9))

        composeRule.onNodeWithText("Asoj 9, 2083").assertExists()
        composeRule.onNodeWithText("असोज ९, २०८३").assertExists()
        composeRule.onNodeWithText("Friday, 25 September 2026").assertExists()
        composeRule.onNodeWithText("Tithi").assertExists()
        composeRule.onNodeWithText("Chaturdashi").assertExists()
        composeRule.onNodeWithText("Public holiday").assertExists()
        composeRule
            .onNodeWithTag("day_event_0")
            .assertTextEquals("Indra Jatra (Kathmandu Valley Holiday)")
        composeRule.onNodeWithTag("day_event_1").assertTextEquals("Ananta Chaturdashi Brata")
        composeRule.onNodeWithTag("day_event_2").assertTextEquals("World Pharmacist Day")
        composeRule.onNodeWithText("Add to calendar").assertExists()
        composeRule.onNodeWithText("Copy date").assertExists()
    }

    @Test
    fun mr02Ac4OutsideCoverageShowsTheMessage() {
        setDayDetail(NepaliDate.of(2090, 1, 1))

        composeRule.onNodeWithText("No holiday or tithi data for this date yet").assertExists()
        composeRule.onNodeWithText("Tithi").assertDoesNotExist()
        composeRule.onNodeWithText("Public holiday").assertDoesNotExist()
        composeRule.onNodeWithText("Events").assertDoesNotExist()
    }

    @Test
    fun mr02Ac6ADayWithoutTithiHasNoTithiRow() {
        setDayDetail(NepaliDate.of(2084, 1, 1))

        composeRule.onNodeWithText("Public holiday").assertExists()
        composeRule.onNodeWithText("New Year 2083").assertExists()
        composeRule.onNodeWithText("Tithi").assertDoesNotExist()
    }

    @Test
    fun mr02Ac3CopyDateCopiesTheBsLongDate() {
        setDayDetail(NepaliDate.of(2083, 6, 9))

        composeRule.onNodeWithText("Copy date").performClick()

        assertClipboard("Asoj 9, 2083")
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun mr02Ac3CopyDateUsesTheNepaliCopy() {
        setDayDetail(NepaliDate.of(2083, 6, 9), Settings(digits = Digits.NEPALI))

        composeRule.onNodeWithText("मिति प्रतिलिपि गर्नुहोस्").performClick()

        assertClipboard("असोज ९, २०८३")
    }

    private fun assertClipboard(expected: String) {
        val clipboard =
            ApplicationProvider
                .getApplicationContext<Context>()
                .getSystemService(ClipboardManager::class.java)
        assertEquals(
            expected,
            clipboard.primaryClip
                ?.getItemAt(0)
                ?.text
                .toString(),
        )
    }

    private fun setDayDetail(
        date: NepaliDate,
        settings: Settings = english,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                DayDetailContent(date = date, settings = settings, onClose = {})
            }
        }
    }
}
