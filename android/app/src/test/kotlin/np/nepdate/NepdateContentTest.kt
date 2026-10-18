package np.nepdate

import androidx.compose.ui.test.junit4.v2.createComposeRule
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

/** A1-05: the three design-system tabs open their screens. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class NepdateContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun bottomBarSwitchesBetweenMonthUpcomingAndConverter() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                NepdateContent(
                    settings = Settings(digits = Digits.ENGLISH),
                    today = NepaliDate.of(2083, 6, 20),
                )
            }
        }

        composeRule.onNodeWithText("Asoj 2083").assertExists()
        composeRule.onNodeWithTag("nav_upcoming").performClick()
        composeRule.onNodeWithTag("upcoming_list").assertExists()
        composeRule.onNodeWithTag("nav_converter").performClick()
        composeRule.onNodeWithTag("converter_bs_input").assertExists()
        composeRule.onNodeWithTag("nav_month").performClick()
        composeRule.onNodeWithText("Asoj 2083").assertExists()
    }
}
