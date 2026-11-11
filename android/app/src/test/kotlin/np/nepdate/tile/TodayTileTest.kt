package np.nepdate.tile

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.ui.common.weekdayName
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Quick Settings tile's two lines follow the app's language and Digits settings. */
class TodayTileTest {
    private val months =
        listOf(
            "Baisakh",
            "Jestha",
            "Ashadh",
            "Shrawan",
            "Bhadau",
            "Asoj",
            "Kartik",
            "Mangsir",
            "Poush",
            "Magh",
            "Falgun",
            "Chaitra",
        )

    @Test
    fun englishTileNamesTheWeekdayAndDate() {
        val text = todayTileText(NepaliDate.of(2083, 6, 20), Digits.ENGLISH, months, Lang.ENGLISH)

        assertEquals("Tuesday, Asoj 20, 2083", text.title)
        assertEquals("Asoj 20, 2083", text.subtitle)
    }

    @Test
    fun nepaliTileUsesDevanagariDigits() {
        val today = NepaliDate.of(2083, 6, 20)
        val text = todayTileText(today, Digits.NEPALI, months, Lang.NEPALI)

        assertEquals("${weekdayName(today, Lang.NEPALI)}, Asoj २०, २०८३", text.title)
        assertEquals("Asoj २०, २०८३", text.subtitle)
    }

    @Test
    fun englishDigitsOverrideANepaliWeekday() {
        val today = NepaliDate.of(2083, 6, 20)
        val text = todayTileText(today, Digits.ENGLISH, months, Lang.NEPALI)

        assertEquals("${weekdayName(today, Lang.NEPALI)}, Asoj 20, 2083", text.title)
    }
}
