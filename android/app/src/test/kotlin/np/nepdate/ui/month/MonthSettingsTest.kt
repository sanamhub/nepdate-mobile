package np.nepdate.ui.month

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
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

/** MR-17: the month grid draws the AD day only while the setting is on. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h900dp-xhdpi")
class MonthSettingsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)

    @Test
    fun theAdDayIsDrawnWhenTheSettingIsOn() {
        setMonth(Settings(digits = Digits.ENGLISH, showAdDates = true))

        composeRule.onNodeWithTag("ad_25", useUnmergedTree = true).assertExists()
    }

    @Test
    fun theAdDayIsGoneWhenTheSettingIsOff() {
        setMonth(Settings(digits = Digits.ENGLISH, showAdDates = false))

        composeRule.onNodeWithTag("ad_25", useUnmergedTree = true).assertDoesNotExist()
    }

    private fun setMonth(settings: Settings) {
        val layout = monthLayout(2083, 6, settings, today)
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                MonthGrid(
                    layout = layout,
                    selectedDate = null,
                    settings = settings,
                    lang = Lang.ENGLISH,
                    bsMonthNames = months,
                    monthNames = months,
                    publicHoliday = "Public holiday",
                    onDayClick = {},
                    rowHeight = 72.dp,
                )
            }
        }
    }

    private val months =
        listOf(
            "Baisakh",
            "Jestha",
            "Asar",
            "Shrawan",
            "Bhadra",
            "Asoj",
            "Kartik",
            "Mangsir",
            "Poush",
            "Magh",
            "Falgun",
            "Chaitra",
        )
}
