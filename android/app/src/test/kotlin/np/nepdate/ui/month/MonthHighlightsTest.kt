package np.nepdate.ui.month

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.theme.NepdateTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** MR-23: the under-grid rows and the rule that hides the block when there are none. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w360dp-h900dp-xhdpi")
class MonthHighlightsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val today = NepaliDate.of(2083, 6, 20)
    private val settings = Settings(digits = Digits.ENGLISH)
    private val layout = monthLayout(2083, 6, settings, today)

    @Test
    fun everyHolidayAndEventDayHasOneRowInDateOrder() {
        val days = highlightDays(layout)

        assertTrue("Asoj has more than three rows", days.size > 3)
        assertTrue(days.all { it.isHoliday || it.hasEvents })
        assertTrue(days.mapNotNull { it.date }.zipWithNext().all { (a, b) -> a < b })
    }

    @Test
    fun aMonthWithoutHolidaysOrEventsDrawsNoBlock() {
        val quiet =
            layout.copy(
                cells = layout.cells.map { it.copy(isHoliday = false, events = emptyList()) },
            )
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                MonthHighlights(layout = quiet, lang = Lang.ENGLISH, digits = Digits.ENGLISH, onDayClick = {})
            }
        }

        composeRule.onNodeWithTag("month_highlights").assertDoesNotExist()
    }

    @Test
    fun theFirstRowOpensItsDay() {
        var opened: NepaliDate? = null
        composeRule.setContent {
            NepdateTheme(darkTheme = false, dynamicColor = false) {
                MonthHighlights(
                    layout = layout,
                    lang = Lang.ENGLISH,
                    digits = Digits.ENGLISH,
                    onDayClick = { opened = it },
                )
            }
        }

        composeRule.onNodeWithTag("month_highlights").assertExists()
        val first = highlightDays(layout).first().date!!
        composeRule.onNodeWithTag("month_highlight_${first.toIsoString()}").performClick()
        assertTrue(opened == first)
    }
}
