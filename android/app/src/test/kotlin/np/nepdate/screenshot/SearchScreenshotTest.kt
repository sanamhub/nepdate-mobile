package np.nepdate.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.search.SearchScreen
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The search screen on a typed date that both calendars can read, so it shows a card for each
 * reading. A human still checks the PNGs and the device.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class SearchScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun twoCards() = capture("a1-search-two-cards", "2081-05-10", Digits.ENGLISH)

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun twoCardsNepali() = capture("a1-search-two-cards-ne", "२०८१-०५-१०", Digits.NEPALI)

    private fun capture(
        name: String,
        query: String,
        digits: Digits,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                SearchScreen(
                    today = NepaliDate.of(2083, 6, 20),
                    settings = Settings(digits = digits),
                    onBack = {},
                    onOpenDate = {},
                )
            }
        }
        composeRule.onNodeWithTag("search_input").performTextInput(query)
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
