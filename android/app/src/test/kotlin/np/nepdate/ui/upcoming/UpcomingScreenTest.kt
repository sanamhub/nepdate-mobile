package np.nepdate.ui.upcoming

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-05 and MR-03 AC1 to AC3 in the Upcoming screen. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class UpcomingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val settings = Settings(digits = Digits.ENGLISH)

    @Test
    fun todayStartsWithTheFirstTwoEventsAndGroupedByMonth() {
        setUp(NepaliDate.of(2083, 6, 20))

        composeRule.onNodeWithText("Asoj 2083").assertExists()
        composeRule.onNodeWithText("Indira Ekadashi Brata").assertExists()
        composeRule.onNodeWithText("Ekadashi Shraddha").assertExists()
        composeRule.onAllNodesWithText("Asoj 20, 2083 · 6 October 2026").onFirst().assertExists()
        composeRule.onNodeWithTag("upcoming_entry_0").assertExists()
    }

    @Test
    fun tappingAnEntryOpensItsDayDetail() {
        setUp(NepaliDate.of(2083, 6, 20))

        composeRule.onNodeWithTag("upcoming_entry_0").performClick()

        composeRule.onNodeWithTag("day_detail").assertExists()
        composeRule.onNodeWithText("Asoj 20, 2083").assertExists()
        composeRule.onNodeWithTag("day_event_0").assertTextEquals("Indira Ekadashi Brata")
    }

    @Test
    fun nearTheEndTheFooterNamesTheCoverageEnd() {
        setUp(NepaliDate.of(2084, 8, 10))

        composeRule.onNodeWithText("Holiday data ends on Mangsir 20, 2084").assertExists()
    }

    private fun setUp(today: NepaliDate) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                UpcomingScreen(today = today, settings = settings)
            }
        }
    }
}
