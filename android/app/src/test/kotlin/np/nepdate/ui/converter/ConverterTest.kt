package np.nepdate.ui.converter

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/** A1-05 and MR-04 AC1 to AC4. */
class ConverterTest {
    private val today = NepaliDate.of(2083, 6, 20)

    @Test
    fun nepaliBsInputConvertsToAd() {
        val state = ConverterState().fromBs("२०८३ असोज २०", today)

        assertEquals(NepaliDate.of(2083, 6, 20), state.result?.bs)
        assertEquals(LocalDate.of(2026, 10, 6), state.result?.ad)
        assertEquals(Weekday.TUESDAY, state.result?.bs?.dayOfWeek)
        assertNull(state.error)
    }

    @Test
    fun numericBsInputConvertsToAd() {
        val state = ConverterState().fromBs("2083-06-20", today)

        assertEquals(LocalDate.of(2026, 10, 6), state.result?.ad)
        assertEquals(0, state.result?.daysFromToday)
    }

    @Test
    fun adInputConvertsToBs() {
        val state = ConverterState().fromAd(LocalDate.of(2026, 10, 6), today)

        assertEquals(NepaliDate.of(2083, 6, 20), state.result?.bs)
        assertEquals(0, state.result?.daysFromToday)
    }

    @Test
    fun invalidDayKeepsThePreviousResult() {
        val first = ConverterState().fromBs("2083-06-20", today)

        val second = first.fromBs("2083-06-32", today)

        assertEquals(first.result, second.result)
        assertEquals(ConverterError.InvalidDay(year = 2083, month = 6, monthLength = 31), second.error)
        assertEquals(LocalDate.of(2026, 10, 6), second.result?.ad)
    }

    @Test
    fun invalidMonthIsReported() {
        val state = ConverterState().fromBs("2083-13-01", today)

        assertEquals(ConverterError.InvalidMonth, state.error)
        assertNull(state.result)
    }

    @Test
    fun secondDateGivesYearsMonthsAndDays() {
        val first = ConverterState().fromBs("2081-01-01", today)
        val compared = first.compareWithBs("2082-01-01")

        assertEquals(1, compared.comparison?.diff?.years)
        assertEquals(0, compared.comparison?.diff?.months)
        assertEquals(0, compared.comparison?.diff?.days)
        assertNull(compared.comparisonError)
    }

    @Test
    fun comparingWithoutAFirstDateIsAnError() {
        val state = ConverterState().compareWithBs("2082-01-01")

        assertEquals(ConverterError.NoFirstDate, state.comparisonError)
    }
}
