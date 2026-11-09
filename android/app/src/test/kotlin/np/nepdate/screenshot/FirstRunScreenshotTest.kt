package np.nepdate.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.firstrun.FirstRunScreen
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The first-run screen in each language. A human checks the PNGs; they are not a pass. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class FirstRunScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun english() {
        capture(name = "a1-first-run-light", settings = Settings(digits = Digits.ENGLISH), darkTheme = false)
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun nepaliDark() {
        capture(name = "a1-first-run-ne-dark", settings = Settings(digits = Digits.NEPALI), darkTheme = true)
    }

    private fun capture(
        name: String,
        settings: Settings,
        darkTheme: Boolean,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = darkTheme, dynamicColor = false) {
                ScreenshotFrame {
                    FirstRunScreen(settings = settings, onLook = {}, onDone = {})
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
