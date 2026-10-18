package np.nepdate.ui.month

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale

/** MR-07 AC1: the default digits follow the resolved app language; explicit choices override it. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class FollowLanguageDigitsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 25)

    @Test
    fun freshInstallInEnglishShowsEnglishDigits() {
        setMonthScreen(Settings())
        assertTrue(cellDescription().startsWith("Asoj 25, 2083"))
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h640dp-xhdpi")
    fun freshInstallInNepaliShowsNepaliDigits() {
        setMonthScreen(Settings())
        assertTrue(cellDescription().startsWith("असोज २५, २०८३"))
    }

    @Test
    fun changingTheLanguageFlipsTheDigitsWithoutTouchingTheSetting() {
        var nepali by mutableStateOf(false)
        composeRule.setContent {
            val source = LocalConfiguration.current
            val config =
                Configuration(source).apply {
                    setLocale(if (nepali) Locale.forLanguageTag("ne-NP") else Locale.ENGLISH)
                }
            CompositionLocalProvider(LocalConfiguration provides config) {
                NepdateTheme(darkTheme = false, dynamicColor = false) {
                    Column {
                        Button(
                            onClick = { nepali = !nepali },
                            modifier = Modifier.testTag("switch_language"),
                        ) {
                            Text("switch")
                        }
                        MonthScreen(
                            initialYear = 2083,
                            initialMonth = 6,
                            today = today,
                            settings = Settings(),
                        )
                    }
                }
            }
        }
        assertTrue(cellDescription(), cellDescription().startsWith("Asoj 25, 2083"))

        composeRule.onNodeWithTag("switch_language").performClick()
        composeRule.waitForIdle()

        assertTrue(cellDescription(), cellDescription().startsWith("Asoj २५, २०८३"))
    }

    @Test
    fun explicitNepaliOverridesTheEnglishLanguage() {
        setMonthScreen(Settings(digits = Digits.NEPALI))
        assertTrue(cellDescription().startsWith("Asoj २५, २०८३"))
    }

    @Test
    @Config(qualifiers = "ne-w360dp-h640dp-xhdpi")
    fun explicitEnglishOverridesTheNepaliLanguage() {
        setMonthScreen(Settings(digits = Digits.ENGLISH))
        assertTrue(cellDescription().startsWith("असोज 25, 2083"))
        assertFalse(cellDescription().contains("२५"))
    }

    private fun setMonthScreen(settings: Settings) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                MonthScreen(
                    initialYear = 2083,
                    initialMonth = 6,
                    today = today,
                    settings = settings,
                )
            }
        }
    }

    private fun cellDescription(): String =
        composeRule
            .onNodeWithTag("month_cell_in_2083-06-25")
            .fetchSemanticsNode()
            .config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription]
            .joinToString(", ")
}
