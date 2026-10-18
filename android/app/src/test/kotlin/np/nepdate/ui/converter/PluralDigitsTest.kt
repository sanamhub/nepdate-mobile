package np.nepdate.ui.converter

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import np.nepdate.ui.upcoming.UpcomingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * MR-07 AC1: counts written through plural resources ("5 दिन पछि", "1 वर्ष") use the Digits setting,
 * not the digits the platform formatter picks for the locale.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "ne-w360dp-h800dp-xhdpi")
class PluralDigitsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)

    @Test
    fun englishDigitsInNepaliKeepTheCountsAscii() {
        setConverter(Digits.ENGLISH)

        convert("2083-06-25")
        composeRule.onNodeWithText("5 दिन पछि").assertExists()

        convert("2083-01-01")
        compare("2084-01-01")
        composeRule.onNodeWithText("1 वर्ष, 0 महिना, 0 दिन").assertExists()
    }

    @Test
    fun followLanguageInNepaliWritesTheCountsInDevanagari() {
        setConverter(Digits.FOLLOW_LANGUAGE)

        convert("2083-06-25")
        composeRule.onNodeWithText("५ दिन पछि").assertExists()

        convert("2083-01-01")
        compare("2084-01-01")
        composeRule.onNodeWithText("१ वर्ष, ० महिना, ० दिन").assertExists()
    }

    @Test
    fun invalidDayMessageUsesTheDigitsSetting() {
        setConverter(Digits.ENGLISH)

        convert("2083-06-32")

        composeRule.onNodeWithTag("converter_error").assertTextEquals("असोज 2083 मा 31 दिन छ")
    }

    @Test
    fun upcomingInDaysUsesTheDigitsSetting() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                UpcomingScreen(today = today, settings = Settings(digits = Digits.ENGLISH))
            }
        }

        composeRule.onAllNodes(textMatches(Regex("[0-9]+ दिनमा"))).onFirst().assertExists()
        composeRule.onAllNodes(textMatches(Regex("[०-९]+ दिनमा"))).assertCountEquals(0)
    }

    private fun setConverter(digits: Digits) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                ConverterScreen(today = today, settings = Settings(digits = digits))
            }
        }
    }

    private fun convert(text: String) {
        composeRule.onNodeWithTag("converter_bs_input").performTextReplacement(text)
        composeRule.onNodeWithTag("converter_convert").performClick()
    }

    private fun compare(text: String) {
        composeRule.onNodeWithTag("converter_compare_input").performTextReplacement(text)
        composeRule.onNodeWithTag("converter_compare").performClick()
    }

    private fun textMatches(regex: Regex) =
        SemanticsMatcher("text matches $regex") { node ->
            node.config.getOrNull(SemanticsProperties.Text)?.any { regex.matches(it.text) } == true
        }
}
