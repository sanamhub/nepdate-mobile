package np.nepdate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import np.nepdate.data.Look
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** MR-16: a look change repaints the app in place, without a relaunch. */
@RunWith(AndroidJUnit4::class)
class ThemeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun changingTheLookRepaintsAtOnce() {
        var look by mutableStateOf(Look.PAPER)
        composeRule.setContent {
            NepdateTheme(darkTheme = look.isDark(), dynamicColor = false) {
                Text(
                    text = MaterialTheme.colorScheme.background.toString(),
                    modifier = Modifier.testTag("background"),
                )
            }
        }

        composeRule.onNodeWithTag("background").assertTextEquals(LightColors.background.toString())

        look = Look.DARK
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("background").assertTextEquals(DarkColors.background.toString())
    }
}
