package np.nepdate.ui.common

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.month.monthLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/** A1-03: the date text helpers used by the month header, cells and labels. */
class DateTextTest {
    private val longMonths =
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
    private val shortMonths =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val nepaliMonths =
        listOf(
            "जनवरी",
            "फेब्रुअरी",
            "मार्च",
            "अप्रिल",
            "मे",
            "जुन",
            "जुलाई",
            "अगस्ट",
            "सेप्टेम्बर",
            "अक्टोबर",
            "नोभेम्बर",
            "डिसेम्बर",
        )

    @Test
    fun digitsGoBothWays() {
        assertEquals("Asoj २५, २०८३", "Asoj 25, 2083".inDigits(Digits.NEPALI))
        assertEquals("Asoj 25, 2083", "Asoj २५, २०८३".inDigits(Digits.ENGLISH))
    }

    @Test
    fun bsLongUsesTheProductMonthNameAndDigits() {
        val date = NepaliDate.of(2083, 6, 25)

        assertEquals("Asoj 25, 2083", bsLong(date, "Asoj", Digits.ENGLISH))
        assertEquals("असोज २५, २०८३", bsLong(date, "असोज", Digits.NEPALI))
        assertEquals("असोज 25, 2083", bsLong(date, "असोज", Digits.ENGLISH))
    }

    @Test
    fun theDevanagariLineKeepsDevanagariDigits() {
        assertEquals("असोज ९, २०८३", bsLongNepali(NepaliDate.of(2083, 6, 9)))
    }

    @Test
    fun adDatesAndMonthTitlesUseTheChosenDigits() {
        val date = NepaliDate.of(2083, 6, 25)

        assertEquals("11 October 2026", adDate(date, longMonths, Digits.ENGLISH))
        assertEquals("११ अक्टोबर २०२६", adDate(date, nepaliMonths, Digits.NEPALI))
        assertEquals("Asoj 2083", monthTitle("Asoj", date.year, Digits.ENGLISH))
        assertEquals("असोज २०८३", monthTitle("असोज", date.year, Digits.NEPALI))
    }

    @Test
    fun asojSpansSeptemberToOctober() {
        val layout = monthLayout(2083, 6, Settings(), NepaliDate.of(2083, 6, 20))
        val span = adSpan(layout.firstDay, layout.lastDay, shortMonths, Digits.ENGLISH)

        assertEquals("Sep", span.startMonth)
        assertEquals("Oct", span.endMonth)
        assertEquals("2026", span.startYear)
        assertEquals("2026", span.endYear)
        assertFalse(span.isSameMonth)
        assertTrue(span.isSameYear)
    }

    @Test
    fun languageComesFromTheLocale() {
        assertEquals(Lang.NEPALI, languageFor(Locale.forLanguageTag("ne-NP")))
        assertEquals(Lang.ENGLISH, languageFor(Locale.forLanguageTag("en-GB")))
        assertEquals(listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"), weekdayHeaders(Lang.ENGLISH))
    }
}
