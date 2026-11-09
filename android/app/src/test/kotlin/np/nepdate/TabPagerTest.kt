package np.nepdate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The tabs as a pager: swipe and tap both move it, and an open-day request is consumed once. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class TabPagerTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)
    private val english = Settings(digits = Digits.ENGLISH)

    @Test
    fun swipeOutsideTheMonthGridMovesToTheNextTab() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                NepdateContent(settings = english, today = today)
            }
        }

        composeRule.onNodeWithContentDescription("Choose month and year").performTouchInput { swipeLeft() }
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("upcoming_list").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("nav_upcoming").assertIsSelected()
    }

    @Test
    fun anOpenDayRequestIsHandledOnceAndNotReplayedOnReturn() {
        var openDate by mutableStateOf<NepaliDate?>(NepaliDate.of(2083, 6, 25))
        var handled by mutableIntStateOf(0)
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                NepdateContent(
                    settings = english,
                    today = today,
                    initialDate = openDate,
                    initialDateKey = 1,
                    onOpenDateHandled = {
                        handled++
                        openDate = null
                    },
                )
            }
        }

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Asoj 25, 2083").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
        assertNull(openDate)
        assertEquals(1, handled)
    }

    @Test
    fun tappingATabSelectsIt() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                NepdateContent(settings = english, today = today)
            }
        }

        composeRule.onNodeWithTag("nav_converter").performClick()
        composeRule.onNodeWithTag("nav_converter").assertIsSelected()
        composeRule.onNodeWithTag("converter_wheel_year").assertExists()
    }
}
