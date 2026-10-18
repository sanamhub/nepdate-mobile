package np.nepdate.ui.upcoming

import io.github.sanamhub.nepdate.NepaliDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** A1-05 and MR-03 AC1 and AC3. */
class UpcomingTest {
    @Test
    fun firstTwentyEntriesStartWithTodaysEvents() {
        val list = upcoming(NepaliDate.of(2083, 6, 20))

        assertEquals(20, list.entries.size)
        assertEquals(listOf("Indira Ekadashi Brata", "Ekadashi Shraddha"), list.entries.take(2).map { it.name.english })
        assertEquals(0, list.entries[0].daysFromToday)
        assertEquals(0, list.entries[1].daysFromToday)
        assertEquals(NepaliDate.of(2083, 6, 20), list.entries[0].date)
        assertFalse(list.entries[0].isHoliday)
        assertNull(list.dataEndsOn)
    }

    @Test
    fun entriesAreGroupedByBsMonthInDateOrder() {
        val list = upcoming(NepaliDate.of(2083, 6, 20))

        assertEquals(listOf(2083 to 6), list.groups.map { it.year to it.month })
        assertEquals(list.entries, list.groups.flatMap { it.entries })
        assertTrue(list.entries.zipWithNext().all { (a, b) -> a.date <= b.date })
    }

    @Test
    fun publicHolidayEntriesCarryTheMark() {
        val entry = upcoming(NepaliDate.of(2083, 6, 20)).entries.first { it.date == NepaliDate.of(2083, 6, 25) }

        assertTrue(entry.isHoliday)
        assertEquals("Ghatasthapana Brata", entry.name.english)
        assertEquals(5, entry.daysFromToday)
    }

    @Test
    fun nearTheEndTheListStopsAtCoverageWithThreeEntries() {
        val list = upcoming(NepaliDate.of(2084, 8, 10))

        assertEquals(3, list.entries.size)
        assertEquals(NepaliDate.of(2084, 8, 20), list.dataEndsOn)
        assertEquals(
            listOf("Pradosh Brata", "Udhauli Festival", "Pradosh Brata"),
            list.entries.map { it.name.english },
        )
        assertEquals(listOf(false, true, false), list.entries.map { it.isHoliday })
    }
}
