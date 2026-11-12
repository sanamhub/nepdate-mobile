package np.nepdate.ui.month

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** MR-22 AC2: which of the today card's chips show, and when the next holiday fills one. */
class TodayChipPlanTest {
    @Test
    fun aQuietDayShowsItsEventsAndTheNextHoliday() {
        val plan = todayChipPlan(eventCount = 2, hasNextHoliday = true)

        assertEquals(listOf(0, 1), plan.eventIndices)
        assertEquals(0, plan.moreCount)
        assertTrue(plan.showsNextHoliday)
    }

    @Test
    fun threeEventsFillTheRowAndHideTheNextHoliday() {
        val plan = todayChipPlan(eventCount = 3, hasNextHoliday = true)

        assertEquals(listOf(0, 1, 2), plan.eventIndices)
        assertEquals(0, plan.moreCount)
        assertFalse(plan.showsNextHoliday)
    }

    @Test
    fun moreThanThreeEventsShowTwoAndTheMoreChip() {
        val plan = todayChipPlan(eventCount = 5, hasNextHoliday = true)

        assertEquals(listOf(0, 1), plan.eventIndices)
        assertEquals(3, plan.moreCount)
        assertFalse(plan.showsNextHoliday)
    }

    @Test
    fun aDayWithoutEventsStillShowsTheNextHoliday() {
        val plan = todayChipPlan(eventCount = 0, hasNextHoliday = true)

        assertEquals(emptyList<Int>(), plan.eventIndices)
        assertEquals(0, plan.moreCount)
        assertTrue(plan.showsNextHoliday)
    }
}
