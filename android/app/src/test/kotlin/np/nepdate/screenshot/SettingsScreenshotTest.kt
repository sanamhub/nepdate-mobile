package np.nepdate.screenshot

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.settings.SettingsScreen
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A1-06 Settings and About screenshots, including the language row on API 35. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h1400dp-xhdpi")
class SettingsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun light() = capture("a1-settings-light", darkTheme = false)

    @Test
    fun dark() = capture("a1-settings-dark", darkTheme = true)

    @Test
    @Config(qualifiers = "ne-w360dp-h1400dp-xhdpi")
    fun nepali() = capture("a1-settings-ne", darkTheme = false)

    private fun capture(
        name: String,
        darkTheme: Boolean,
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = darkTheme, dynamicColor = false) {
                ScreenshotFrame {
                    SettingsScreen(
                        settings = Settings(digits = Digits.ENGLISH),
                        appVersion = "0.0.1 (1)",
                        libraryVersion = "0.1.0",
                        osVersion = "14",
                        onClose = {},
                        onDateFollows = {},
                        onDigits = {},
                        onShowTithi = {},
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage("$name.png")
    }
}
