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
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** The first-run screen in each language. A human checks the PNGs; they are not a pass. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xhdpi")
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
                    FirstRunScreen(settings = settings, onLook = {}, onDone = {}, clock = KARTIK_23_2083)
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }

    private companion object {
        /** Noon on Kartik 23, 2083 (9 November 2026) in Nepal, the day the references show. */
        val KARTIK_23_2083: Clock =
            Clock.fixed(Instant.parse("2026-11-09T06:15:00Z"), ZoneId.of("Asia/Kathmandu"))
    }
}
