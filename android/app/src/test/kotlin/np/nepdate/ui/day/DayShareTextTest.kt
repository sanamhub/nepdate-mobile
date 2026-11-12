package np.nepdate.ui.day

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

/** MR-20: the text Share hands to another app. */
class DayShareTextTest {
    private val months =
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

    @Test
    fun theShareTextHasTheDateTheWeekdayAndEveryEvent() {
        val date = NepaliDate.of(2083, 6, 9)

        val text =
            dayShareText(
                date = date,
                info = Patro.info(date),
                lang = Lang.ENGLISH,
                settings = Settings(digits = Digits.ENGLISH),
                bsMonthNames = months,
                weekdayAd = "Friday, 25 September 2026",
            )

        assertEquals(
            "Asoj 9, 2083\n" +
                "Friday, 25 September 2026\n" +
                "Indra Jatra (Kathmandu Valley Holiday)\n" +
                "Ananta Chaturdashi Brata\n" +
                "World Pharmacist Day",
            text,
        )
    }

    @Test
    fun aDayWithoutEventsSharesItsDateAlone() {
        val date = NepaliDate.of(2090, 1, 1)

        val text =
            dayShareText(
                date = date,
                info = Patro.info(date),
                lang = Lang.ENGLISH,
                settings = Settings(digits = Digits.ENGLISH),
                bsMonthNames = months,
                weekdayAd = "Some weekday",
            )

        assertEquals("Baisakh 1, 2090\nSome weekday", text)
    }
}
