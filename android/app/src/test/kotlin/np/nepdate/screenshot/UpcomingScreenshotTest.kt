package np.nepdate.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import np.nepdate.ui.upcoming.UpcomingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A1-05 Upcoming screenshots. A human still checks the PNGs and the device. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class UpcomingScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun light() = capture("a1-upcoming-light", darkTheme = false, digits = Digits.ENGLISH)

    @Test
    fun dark() = capture("a1-upcoming-dark", darkTheme = true, digits = Digits.ENGLISH)

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun nepali() = capture("a1-upcoming-ne", darkTheme = false, digits = Digits.NEPALI)

    private fun capture(
        name: String,
        darkTheme: Boolean,
        digits: Digits,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = darkTheme, dynamicColor = false) {
                ScreenshotFrame {
                    UpcomingScreen(
                        today = NepaliDate.of(2083, 6, 20),
                        settings = Settings(digits = digits),
                        onSettings = {},
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
