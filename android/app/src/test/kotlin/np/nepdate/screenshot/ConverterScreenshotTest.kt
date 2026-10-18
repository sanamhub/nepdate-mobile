package np.nepdate.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.converter.ConverterScreen
import np.nepdate.ui.converter.ConverterState
import np.nepdate.ui.converter.compareWithBs
import np.nepdate.ui.converter.fromBs
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A1-05 Converter screenshots. A human still checks the PNGs and the device. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class ConverterScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun light() = capture("a1-converter-light", darkTheme = false, digits = Digits.ENGLISH)

    @Test
    fun dark() = capture("a1-converter-dark", darkTheme = true, digits = Digits.ENGLISH)

    @Test
    @Config(qualifiers = "ne-w360dp-h800dp-xhdpi")
    fun nepali() = capture("a1-converter-ne", darkTheme = false, digits = Digits.NEPALI)

    private fun capture(
        name: String,
        darkTheme: Boolean,
        digits: Digits,
    ) {
        val today = NepaliDate.of(2083, 6, 20)
        val state =
            ConverterState()
                .fromBs("2083-06-20", today)
                .compareWithBs("2082-01-01")
        composeRule.setContent {
            NepdateTheme(darkTheme = darkTheme, dynamicColor = false) {
                ScreenshotFrame {
                    ConverterScreen(
                        today = today,
                        settings = Settings(digits = digits),
                        initialBs = "2083-06-20",
                        initialCompare = "2082-01-01",
                        initialState = state,
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
