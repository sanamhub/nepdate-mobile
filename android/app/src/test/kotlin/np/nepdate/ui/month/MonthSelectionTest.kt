package np.nepdate.ui.month

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

/** A1-03 and MR-01 AC4: the selected day is exposed as selected. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xhdpi")
class MonthSelectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedDayCarriesTheSelectionState() {
        val layout = monthLayout(2083, 6, Settings(digits = Digits.ENGLISH), NepaliDate.of(2083, 6, 20))
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                MonthGrid(
                    layout = layout,
                    selectedDate = NepaliDate.of(2083, 6, 25),
                    settings = Settings(digits = Digits.ENGLISH),
                    lang = Lang.ENGLISH,
                    bsMonthNames = bsMonthNames,
                    monthNames = monthNames,
                    publicHoliday = "public holiday",
                    onDayClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("month_cell_in_2083-06-25").assertIsSelected()
    }

    private companion object {
        val monthNames =
            listOf(
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December",
            )
        val bsMonthNames =
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
}
