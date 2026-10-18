package np.nepdate.ui.settings

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.ZoneId

/** A1-06 and MR-06 AC1: the first-launch suggestion shows only when it should, once. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class NepalTimeSuggestionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val london = ZoneId.of("Europe/London")
    private val kathmandu = ZoneId.of("Asia/Kathmandu")

    @Test
    fun suggestionShowsOnlyForAnotherZoneAndAnUnhandledInstall() {
        assertTrue(shouldSuggestNepalTime(Settings(), london))
        assertFalse(shouldSuggestNepalTime(Settings(), kathmandu))
        assertFalse(shouldSuggestNepalTime(Settings(dateFollowsSuggestionHandled = true), london))
        assertFalse(
            shouldSuggestNepalTime(
                Settings(dateFollows = DateFollows.NEPAL),
                london,
            ),
        )
    }

    @Test
    fun notNowDismissesTheSuggestion() {
        var accepted = false
        var dismissed = false
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                NepalTimeSuggestion(onAccept = { accepted = true }, onDismiss = { dismissed = true })
            }
        }

        composeRule.onNodeWithTag("nepal_time_not_now").performClick()

        assertTrue(dismissed)
        assertFalse(accepted)
    }

    @Test
    fun acceptUsesNepalTime() {
        var accepted = false
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                NepalTimeSuggestion(onAccept = { accepted = true }, onDismiss = {})
            }
        }

        composeRule.onNodeWithTag("nepal_time_accept").performClick()

        composeRule.waitForIdle()
        assertTrue(accepted)
    }
}
