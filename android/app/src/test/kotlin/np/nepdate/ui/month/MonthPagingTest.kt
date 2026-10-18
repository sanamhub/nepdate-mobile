package np.nepdate.ui.month

import org.junit.Assert.assertEquals
import org.junit.Test

/** A1-03 and MR-01 AC6: the pager covers BS 1901-01 to 2199-12 without gaps. */
class MonthPagingTest {
    @Test
    fun firstAndLastPageAreThePickerRange() {
        assertEquals(0, monthPage(PICKER_FIRST_YEAR, 1))
        assertEquals(MONTH_PAGE_COUNT - 1, monthPage(PICKER_LAST_YEAR, 12))
        assertEquals(3588, MONTH_PAGE_COUNT)
    }

    @Test
    fun everyMonthRoundTripsThroughItsPage() {
        for (year in PICKER_FIRST_YEAR..PICKER_LAST_YEAR) {
            for (month in 1..MONTHS_PER_YEAR) {
                val page = monthPage(year, month)
                assertEquals(year, yearForPage(page))
                assertEquals(month, monthForPage(page))
            }
        }
    }
}
