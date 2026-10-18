package np.nepdate.screenshot

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.month.MonthScreen
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A1-03 screenshots. They are not a pass: a human checks the PNGs and then the device
 * (docs/runbooks/manual-test.md).
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class MonthScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun light() {
        capture(
            name = "a1-month-light",
            settings = Settings(digits = Digits.ENGLISH),
        )
    }

    @Test
    fun dark() {
        capture(
            name = "a1-month-dark",
            settings = Settings(digits = Digits.ENGLISH),
            darkTheme = true,
        )
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun nepali() {
        capture(
            name = "a1-month-ne",
            settings = Settings(digits = Digits.NEPALI),
        )
    }

    @Test
    fun tithiOn() {
        capture(
            name = "a1-month-tithi",
            settings = Settings(digits = Digits.ENGLISH, showTithi = true),
        )
    }

    @Test
    fun fontScale200() {
        capture(
            name = "a1-month-font-scale-200",
            settings = Settings(digits = Digits.ENGLISH),
            fontScale = 2f,
        )
    }

    @Test
    fun saturdayStart() {
        capture(
            name = "a1-month-saturday-start",
            settings = Settings(digits = Digits.ENGLISH),
            year = 2081,
            month = 1,
            today = NepaliDate.of(2081, 1, 15),
        )
    }

    private fun capture(
        name: String,
        settings: Settings,
        darkTheme: Boolean = false,
        fontScale: Float = 1f,
        year: Int = 2083,
        month: Int = 6,
        today: NepaliDate = NepaliDate.of(2083, 6, 20),
    ) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = fontScale),
            ) {
                NepdateTheme(darkTheme = darkTheme, dynamicColor = false) {
                    ScreenshotFrame {
                        MonthScreen(
                            initialYear = year,
                            initialMonth = month,
                            today = today,
                            settings = settings,
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
