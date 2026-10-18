package np.nepdate.ui.month

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.toLocalDate
import np.nepdate.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** A1-02 and the month-layout parts of MR-01 and MR-02. */
class MonthLayoutTest {
    private val today = NepaliDate.of(2083, 6, 20)

    @Test
    fun asoj2083() {
        val layout = monthLayout(2083, 6, settings(), today)

        assertEquals(NepaliDate.of(2083, 6, 1), layout.firstDay)
        assertEquals(LocalDate.of(2026, 9, 17), layout.firstDay.toLocalDate())
        assertEquals(Weekday.THURSDAY, layout.firstDay.dayOfWeek)
        assertEquals(35, layout.cells.size)
        assertEquals(5, layout.rowCount)
        assertEquals(4, layout.leadingDays)
        assertEquals(listOf(3, 9, 18, 25, 31), layout.holidayDays)
        assertEquals(NepaliDate.of(2083, 6, 31), layout.lastDay)
        assertEquals(LocalDate.of(2026, 10, 17), layout.lastDay.toLocalDate())
        assertTrue(layout.cells.first { it.bsDay == 3 }.isSaturday)
        assertTrue(layout.cells.first { it.bsDay == 20 }.isToday)
        assertTrue(layout.cells.first { it.bsDay == 1 }.hasEvents)
    }

    @Test
    fun asar2083() {
        val layout = monthLayout(2083, 3, settings(), today)

        assertEquals(NepaliDate.of(2083, 3, 1), layout.firstDay)
        assertEquals(LocalDate.of(2026, 6, 15), layout.firstDay.toLocalDate())
        assertEquals(Weekday.MONDAY, layout.firstDay.dayOfWeek)
        assertEquals(32, layout.firstDay.lengthOfMonth())
        assertEquals(35, layout.cells.size)
        assertEquals(1, layout.leadingDays)
    }

    @Test
    fun jestha2083() {
        val layout = monthLayout(2083, 2, settings(), today)

        assertEquals(LocalDate.of(2026, 5, 15), layout.firstDay.toLocalDate())
        assertEquals(Weekday.FRIDAY, layout.firstDay.dayOfWeek)
        assertEquals(42, layout.cells.size)
        assertEquals(5, layout.leadingDays)
        assertEquals(6, layout.rowCount)
    }

    @Test
    fun baisakh2084DayOneHasAHolidayAndNoTithi() {
        val layout = monthLayout(2084, 1, settings(), today)
        val dayOne = layout.cells.first { it.isInMonth }

        assertEquals(1, dayOne.bsDay)
        assertTrue(dayOne.isHoliday)
        assertTrue(dayOne.hasEvents)
        assertNull(dayOne.tithi)
    }

    @Test
    fun tithiFollowsTheSetting() {
        val off = monthLayout(2083, 6, settings(showTithi = false), today)
        val on = monthLayout(2083, 6, settings(showTithi = true), today)

        assertNull(off.cells.first { it.isInMonth }.tithi)
        assertTrue(on.cells.first { it.isInMonth }.tithi != null)
    }

    @Test
    fun everyMonthInTheDataRangeMatchesTheLibrary() {
        var date = Patro.coverage.start
        var months = 0
        while (date <= Patro.coverage.endInclusive) {
            val layout = monthLayout(date.year, date.monthValue, settings(showTithi = true), today)
            val first = date.firstDayOfMonth()
            val last = date.lastDayOfMonth()
            val leading = first.dayOfWeek.ordinal
            val expectedCells = if (leading + last.dayOfMonth <= SHORT_GRID) SHORT_GRID else LONG_GRID

            assertEquals("$date leading days", leading, layout.leadingDays)
            assertEquals("$date cell count", expectedCells, layout.cells.size)
            assertEquals("$date first day", first, layout.firstDay)
            assertEquals("$date last day", last, layout.lastDay)
            assertEquals("$date days in month", last.dayOfMonth, layout.cells.count { it.isInMonth })
            assertEquals("$date first in-month cell", first, layout.cells.first { it.isInMonth }.date)
            assertEquals("$date last in-month cell", last, layout.cells.last { it.isInMonth }.date)
            assertEquals("$date leading cells", leading, layout.cells.take(leading).count { !it.isInMonth })

            layout.cells.forEachIndexed { index, cell ->
                val cellDate = cell.date
                if (cellDate == null) {
                    assertFalse("$date index $index", cell.isInMonth)
                    return@forEachIndexed
                }
                assertEquals("$date index $index BS day", cellDate.dayOfMonth, cell.bsDay)
                assertEquals("$date index $index AD day", cellDate.gregorianDay, cell.adDay)
                assertEquals("$date index $index Saturday", cellDate.dayOfWeek == Weekday.SATURDAY, cell.isSaturday)
                if (cell.isInMonth) {
                    val info = Patro.info(cellDate)
                    assertEquals("$date index $index holiday", info.isPublicHoliday, cell.isHoliday)
                    assertEquals("$date index $index events", info.events, cell.events)
                } else {
                    assertFalse("$date index $index neighbour holiday", cell.isHoliday)
                    assertFalse("$date index $index neighbour event", cell.hasEvents)
                }
            }
            months++
            date = first.plusMonths(1)
        }

        assertEquals(92, months)
    }

    @Test
    fun gridsAtTheEndsOfTheLibraryRangeHaveBlankEdgeCells() {
        val first = monthLayout(NepaliDate.MIN.year, NepaliDate.MIN.monthValue, settings(), today)
        val last = monthLayout(NepaliDate.MAX.year, NepaliDate.MAX.monthValue, settings(), today)

        assertEquals(NepaliDate.MIN.firstDayOfMonth().lengthOfMonth(), first.cells.count { it.isInMonth })
        assertEquals(NepaliDate.MAX.firstDayOfMonth().lengthOfMonth(), last.cells.count { it.isInMonth })
        assertTrue(last.cells.any { it.date == null && !it.isInMonth })
    }

    private fun settings(showTithi: Boolean = false): Settings = Settings(showTithi = showTithi)
}
