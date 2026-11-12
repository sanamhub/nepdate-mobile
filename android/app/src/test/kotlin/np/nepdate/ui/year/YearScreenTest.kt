package np.nepdate.ui.year

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** MR-14: the year's twelve months, today's mark, the tap target and both range ends. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h900dp-xhdpi")
class YearScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)
    private val settings = Settings(digits = Digits.ENGLISH)

    @Test
    fun theYearShowsTwelveMonthsAndTappingOneOpensIt() {
        var opened: Pair<Int, Int>? = null
        setYear(2083, onOpenMonth = { year, month -> opened = year to month })

        for (month in 1..12) {
            composeRule.onNodeWithTag("year_month_$month", useUnmergedTree = true).assertExists()
        }
        composeRule.onNodeWithTag("year_month_6", useUnmergedTree = true).performClick()

        assertEquals(2083 to 6, opened)
    }

    @Test
    fun theRangeStartsAtTheFirstYear() {
        setYear(1800)

        composeRule.onNodeWithTag("year_title", useUnmergedTree = true).assertTextEquals("1901")
        composeRule.onNodeWithTag("year_month_1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("year_month_12", useUnmergedTree = true).assertExists()
    }

    @Test
    fun theRangeEndsAtTheLastYear() {
        setYear(2200)

        composeRule.onNodeWithTag("year_title", useUnmergedTree = true).assertTextEquals("2199")
        composeRule.onNodeWithTag("year_month_1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithTag("year_month_12", useUnmergedTree = true).assertExists()
    }

    private fun setYear(
        year: Int,
        onOpenMonth: (Int, Int) -> Unit = { _, _ -> },
    ) {
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                YearScreen(
                    initialYear = year,
                    today = today,
                    settings = settings,
                    onBack = {},
                    onOpenMonth = onOpenMonth,
                )
            }
        }
    }
}
