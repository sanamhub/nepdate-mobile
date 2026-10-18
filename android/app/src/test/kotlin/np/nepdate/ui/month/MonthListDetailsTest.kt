package np.nepdate.ui.month

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The list layout's visible event name follows the Digits setting, like the grid's spoken label. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp-xhdpi")
class MonthListDetailsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val cell =
        monthLayout(
            year = 2084,
            monthValue = 1,
            settings = Settings(showTithi = true),
            today = NepaliDate.of(2084, 1, 1),
        ).cells.first { it.isInMonth && it.bsDay == 1 }

    @Test
    fun eventNameUsesEnglishDigits() {
        setDetails(Digits.ENGLISH)

        composeRule.onNodeWithText("New Year 2083").assertExists()
    }

    @Test
    fun eventNameUsesNepaliDigits() {
        setDetails(Digits.NEPALI)

        composeRule.onNodeWithText("New Year २०८३").assertExists()
    }

    private fun setDetails(digits: Digits) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = density.density, fontScale = 2f),
            ) {
                NepdateTheme(darkTheme = false, dynamicColor = false) {
                    Box {
                        DayListDetails(cell, Lang.ENGLISH, digits, dayInk(cell, false))
                    }
                }
            }
        }
    }
}
