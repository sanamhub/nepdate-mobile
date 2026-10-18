package np.nepdate.widget

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MonthWidgetStateTest {
    private val today = NepaliDate.of(2083, 6, 25)

    @Test
    fun previousAndNextMoveTheShownMonthByOne() {
        val model =
            monthWidgetModel(
                year = 2083,
                monthValue = 6,
                today = today,
                lang = io.github.sanamhub.nepdate.Lang.ENGLISH,
                digits = np.nepdate.data.Digits.ENGLISH,
                copy = testCopy(),
            )
        assertEquals(2083 to 5, monthNavigation(model, -1))
        assertEquals(2083 to 7, monthNavigation(model, 1))
        assertEquals(2083 to 6, model.today.year to model.today.monthValue)
    }

    @Test
    fun navigatingAcrossTheYearWorks() {
        assertEquals(2082 to 12, monthStep(2083, 1, -1))
        assertEquals(2084 to 1, monthStep(2083, 12, 1))
        assertNull(monthStep(1901, 1, -1))
        assertNull(monthStep(2199, 12, 1))
    }

    @Test
    fun clearingTheShownMonthFallsBackToToday() {
        val shown = mutablePreferencesOf(SHOWN_YEAR_KEY to 2083, SHOWN_MONTH_KEY to 5)
        assertEquals(2083 to 5, shownMonth(shown, today))

        shown.remove(SHOWN_YEAR_KEY)
        shown.remove(SHOWN_MONTH_KEY)

        assertEquals(today.year to today.monthValue, shownMonth(shown, today))
        assertEquals(today.year to today.monthValue, shownMonth(emptyPreferences(), today))
    }

    private fun testCopy(): WidgetCopy =
        WidgetCopy(
            "Nepal",
            "Public holiday",
            "Previous month",
            "Next month",
            "Today",
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
            ),
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
            ),
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"),
            "Today",
            "Tomorrow",
            { "in $it days" },
            { "Holiday data ends on $it" },
        )
}
