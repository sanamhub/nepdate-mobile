package np.nepdate.ui.month

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

/** A1-03 and MR-10 AC1: the exact spoken label for Asoj 25 2083. */
class MonthLabelsTest {
    private val bsMonthNames =
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
        )
    private val monthNames =
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

    @Test
    fun asoj25SpeaksTheRequirementString() {
        val layout = monthLayout(2083, 6, Settings(showTithi = false), NepaliDate.of(2083, 6, 20))
        val cell = layout.cells.first { it.bsDay == 25 }

        assertEquals(
            "Asoj 25, 2083, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata",
            dayDescription(cell, Lang.ENGLISH, Digits.ENGLISH, bsMonthNames, monthNames, "public holiday"),
        )
    }

    @Test
    fun aNeighbouringDaySpeaksOnlyItsDate() {
        val layout = monthLayout(2083, 6, Settings(), NepaliDate.of(2083, 6, 20))
        val neighbour = layout.cells.first { !it.isInMonth }

        assertEquals(
            "Bhadra 28, 2083, Sunday, 13 September 2026",
            dayDescription(neighbour, Lang.ENGLISH, Digits.ENGLISH, bsMonthNames, monthNames, "public holiday"),
        )
    }
}
