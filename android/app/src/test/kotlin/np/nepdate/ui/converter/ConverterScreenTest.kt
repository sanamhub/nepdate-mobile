package np.nepdate.ui.converter

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-05 and MR-04 AC1 to AC4 in the Converter screen. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class ConverterScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)

    @Test
    fun bsInputShowsTheAdDateAndWeekday() {
        setUp()

        convert("2083-06-20")

        composeRule.onNodeWithTag("converter_result").assertTextEquals("Asoj 20, 2083")
        composeRule.onNodeWithText("Tuesday, 6 October 2026").assertExists()
        composeRule.onNodeWithText("Today").assertExists()
    }

    @Test
    fun invalidDayShowsTheMessageAndKeepsThePreviousResult() {
        setUp()
        convert("2083-06-20")

        convert("2083-06-32")

        composeRule.onNodeWithTag("converter_error").assertTextEquals("Asoj has 31 days in 2083")
        composeRule.onNodeWithTag("converter_result").assertTextEquals("Asoj 20, 2083")
    }

    @Test
    fun secondDateShowsTheDifference() {
        setUp()
        convert("2081-01-01")

        composeRule.onNodeWithTag("converter_compare_input").performTextReplacement("2082-01-01")
        composeRule.onNodeWithTag("converter_compare").performClick()

        composeRule.onNodeWithText("1 year, 0 months, 0 days").assertExists()
    }

    private fun convert(text: String) {
        composeRule.onNodeWithTag("converter_bs_input").performTextReplacement(text)
        composeRule.onNodeWithTag("converter_convert").performClick()
    }

    private fun setUp() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                ConverterScreen(today = today, settings = Settings(digits = Digits.ENGLISH))
            }
        }
    }
}
