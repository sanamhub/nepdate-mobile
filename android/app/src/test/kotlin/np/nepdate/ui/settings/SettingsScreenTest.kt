package np.nepdate.ui.settings

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import np.nepdate.data.DateFollows
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** A1-06 and MR-06, MR-07, MR-12 in the Settings screen. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var settings by mutableStateOf(Settings(digits = Digits.NEPALI))

    @Test
    fun choicesWriteTheSettings() {
        setUp()

        composeRule.onNodeWithText("English (012)").performClick()
        assertEquals(Digits.ENGLISH, settings.digits)

        composeRule.onNodeWithText("Nepal time").performClick()
        assertEquals(DateFollows.NEPAL, settings.dateFollows)

        composeRule.onNodeWithText("Show tithi in the month grid").performClick()
        assertEquals(true, settings.showTithi)
    }

    @Test
    fun aboutShowsVersionsCoverageAndLinks() {
        setUp()

        composeRule.onNodeWithText("About").assertExists()
        composeRule.onNodeWithText("0.0.1 (1)").assertExists()
        composeRule.onNodeWithText("0.1.0").assertExists()
        composeRule.onNodeWithText("Baisakh १, २०७७ – Mangsir २०, २०८४").assertExists()
        composeRule.onNodeWithText("Known data questions").assertExists()
        composeRule.onNodeWithText("Report a wrong date").assertExists()
        composeRule.onNodeWithText("Licences").assertExists()
        composeRule.onNodeWithText("Privacy").assertExists()
        composeRule.onNodeWithText("Source code").assertExists()
    }

    @Test
    fun copyDiagnosticInfoUsesTheBuilder() {
        setUp()

        composeRule.onNodeWithText("Copy diagnostic info").performScrollTo().performClick()

        val clipboard =
            ApplicationProvider
                .getApplicationContext<Context>()
                .getSystemService(ClipboardManager::class.java)
        assertEquals(
            "App: 0.0.1 (1)\n" +
                "Android: 14\n" +
                "Date follows: This device\n" +
                "Digits: Nepali\n" +
                "Show tithi: off",
            clipboard.primaryClip
                ?.getItemAt(0)
                ?.text
                .toString(),
        )
        composeRule.onNodeWithText("Copied").assertTextEquals("Copied")
    }

    @Test
    @Config(sdk = [32])
    fun languageRowIsAbsentBelowApi33() {
        setUp()

        composeRule.onNodeWithText("Language").assertDoesNotExist()
    }

    @Test
    @Config(sdk = [33])
    fun languageRowIsPresentOnApi33() {
        setUp()

        composeRule.onNodeWithText("Language").assertExists()
        composeRule.onNodeWithText("System").assertExists()
        composeRule.onNodeWithText("नेपाली").assertExists()
    }

    private fun setUp() {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                SettingsScreen(
                    settings = settings,
                    appVersion = "0.0.1 (1)",
                    libraryVersion = "0.1.0",
                    osVersion = "14",
                    onClose = {},
                    onDateFollows = { settings = settings.copy(dateFollows = it) },
                    onDigits = { settings = settings.copy(digits = it) },
                    onShowTithi = { settings = settings.copy(showTithi = it) },
                )
            }
        }
    }
}
