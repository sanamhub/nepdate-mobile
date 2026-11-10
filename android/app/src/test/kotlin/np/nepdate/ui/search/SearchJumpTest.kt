package np.nepdate.ui.search

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toLocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Search date jump: a typed year-month-day is a real day in BS and a real day in AD, so the screen
 * offers a reading for each calendar that can read it.
 */
class SearchJumpTest {
    @Test
    fun aDateInBothCalendarsOffersBothReadings() {
        val dates = parseJumpDates("2081-05-10")

        assertEquals("2081-05-10", dates.bs?.toIsoString())
        assertEquals(LocalDate.of(2081, 5, 10), dates.ad?.toLocalDate())
        assertTrue(dates.isDate)
    }

    @Test
    fun theBsReadingComesFirstSoTheSearchActionOpensIt() {
        val dates = parseJumpDates("2081-05-10")

        assertEquals(dates.bs, dates.primary)
    }

    @Test
    fun aDatePastTheAdCoverageOffersOnlyTheBsReading() {
        val dates = parseJumpDates("2150-01-01")

        assertEquals("2150-01-01", dates.bs?.toIsoString())
        assertNull(dates.ad)
        assertEquals(dates.bs, dates.primary)
    }

    @Test
    fun aDateBeforeTheBsCoverageOffersOnlyTheAdReading() {
        // BS years start at 1901, so a year that an AD date can carry but BS cannot has one reading.
        val dates = parseJumpDates("1850-01-01")

        assertNull(dates.bs)
        assertEquals(LocalDate.of(1850, 1, 1), dates.ad?.toLocalDate())
        assertEquals(dates.ad, dates.primary)
    }

    @Test
    fun devanagariDigitsReadTheSameAsAsciiDigits() {
        assertEquals(parseJumpDates("2081-05-10"), parseJumpDates("२०८१-०५-१०"))
    }

    @Test
    fun eightBareDigitsReadAsYearMonthDay() {
        assertEquals(parseJumpDates("2081-05-10"), parseJumpDates("20810510"))
    }

    @Test
    fun aSlashOrADotSeparatesThePartsToo() {
        assertEquals(parseJumpDates("2081-05-10"), parseJumpDates("2081/05/10"))
        assertEquals(parseJumpDates("2081-05-10"), parseJumpDates("2081.05.10"))
    }

    @Test
    fun textThatIsNotADateOffersNoReading() {
        val dates = parseJumpDates("Dashain")

        assertFalse(dates.isDate)
        assertNull(dates.primary)
    }

    @Test
    fun aDayTheBsMonthDoesNotHaveIsNotADateButCanStillBeAnAdDate() {
        // 2081 Chaitra has 30 days, so 2081-12-32 is nothing in BS while AD has no 32nd either.
        val dates = parseJumpDates("2081-12-32")

        assertFalse(dates.isDate)
    }

    @Test
    fun theJumpTargetIsTheDayTheTypedNumbersName() {
        val dates = parseJumpDates("2083-07-15")

        assertNotNull(dates.bs)
        assertEquals(NepaliDate.of(2083, 7, 15), dates.bs)
    }
}
