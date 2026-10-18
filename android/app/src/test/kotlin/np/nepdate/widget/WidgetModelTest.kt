package np.nepdate.widget

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import np.nepdate.ui.common.resolve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetModelTest {
    private val english =
        WidgetCopy(
            "Nepal",
            "Public holiday",
            "Previous month",
            "Next month",
            "Today",
            BS_MONTHS_EN,
            AD_MONTHS_EN,
            AD_MONTHS_SHORT_EN,
            "Today",
            "Tomorrow",
            { "in $it days" },
            { "Holiday data ends on $it" },
        )

    private val nepali =
        WidgetCopy(
            "नेपाल",
            "सार्वजनिक बिदा",
            "अघिल्लो महिना",
            "अर्को महिना",
            "आज",
            BS_MONTHS_NE,
            AD_MONTHS_NE,
            AD_MONTHS_SHORT_NE,
            "आज",
            "भोलि",
            { "$it दिनमा" },
            { "बिदा र कार्यक्रमको जानकारी $it मा सकिन्छ" },
        )

    @Test
    fun w01NormalDayHasTheExpectedStrings() {
        val model =
            todayWidgetModel(
                NepaliDate.of(2083, 6, 20),
                Lang.ENGLISH,
                Digits.ENGLISH,
                english,
                showNepal = false,
            )

        assertEquals("20", model.bsDay)
        assertEquals("Asoj", model.monthName)
        assertEquals("Tuesday", model.weekday)
        assertEquals("6 October 2026", model.adLine)
        assertEquals("Asoj 20 Tuesday, 6 Oct", model.oneLine)
        assertFalse(model.isHoliday)
        assertNull(model.holidayName)
        assertEquals("Ghatasthapana Brata", model.nextHoliday?.name)
        assertEquals("in 5 days", model.nextHoliday?.relative)
    }

    @Test
    fun w01HolidayHasTheHolidayNameAndMark() {
        val model =
            todayWidgetModel(
                NepaliDate.of(2083, 6, 25),
                Lang.ENGLISH,
                Digits.ENGLISH,
                english,
                showNepal = false,
            )

        assertEquals("25", model.bsDay)
        assertEquals("Sunday", model.weekday)
        assertEquals("11 October 2026", model.adLine)
        assertEquals("Ghatasthapana Brata", model.holidayName)
        assertEquals("Asoj 25 Sunday, Ghatasthapana Brata", model.oneLine)
        assertTrue(model.isHoliday)
    }

    @Test
    fun w01NepalLabelOnlyWhenRequested() {
        val without =
            todayWidgetModel(NepaliDate.of(2083, 6, 20), Lang.ENGLISH, Digits.ENGLISH, english, false)
        val with =
            todayWidgetModel(NepaliDate.of(2083, 6, 20), Lang.ENGLISH, Digits.ENGLISH, english, true)
        assertNull(without.nepalLabel)
        assertEquals("Nepal", with.nepalLabel)
    }

    @Test
    fun w02ListsThreeEntriesAndUsesTheCoverageFooterAtTheEnd() {
        val model =
            upcomingWidgetModel(
                NepaliDate.of(2083, 6, 20),
                Lang.ENGLISH,
                Digits.ENGLISH,
                english,
                showNepal = false,
            )
        assertEquals(3, model.entries.size)
        assertEquals("Indira Ekadashi Brata", model.entries.first().name)
        assertNull(model.dataEnds)

        val last =
            upcomingWidgetModel(
                NepaliDate.of(2084, 8, 20),
                Lang.ENGLISH,
                Digits.ENGLISH,
                english,
                showNepal = false,
            )
        assertNotNull(last.dataEnds)
        assertTrue(last.dataEnds!!.startsWith("Holiday data ends on Mangsir 20, 2084"))
    }

    @Test
    fun w03Jestha2083HasSixWeeksAndMarksTheHolidayAndToday() {
        val model =
            monthWidgetModel(
                year = 2083,
                monthValue = 2,
                today = NepaliDate.of(2083, 6, 25),
                lang = Lang.ENGLISH,
                digits = Digits.ENGLISH,
                copy = english,
            )
        assertEquals(6, model.rowCount)

        val asoj =
            monthWidgetModel(
                year = 2083,
                monthValue = 6,
                today = NepaliDate.of(2083, 6, 25),
                lang = Lang.ENGLISH,
                digits = Digits.ENGLISH,
                copy = english,
            )
        val holiday = asoj.cells.first { it.date == NepaliDate.of(2083, 6, 25) }
        assertTrue(holiday.isHoliday)
        assertTrue(holiday.isToday)
        assertEquals("25", holiday.bsDay)
        assertEquals("11", holiday.adDay)

        val normal =
            monthWidgetModel(
                year = 2083,
                monthValue = 6,
                today = NepaliDate.of(2083, 6, 20),
                lang = Lang.ENGLISH,
                digits = Digits.ENGLISH,
                copy = english,
            )
        val normalDay = normal.cells.first { it.date == NepaliDate.of(2083, 6, 20) }
        assertTrue(normalDay.isToday)
        assertFalse(normalDay.isHoliday)
    }

    @Test
    fun w02HolidayEntriesKeepTheHolidayFlag() {
        val model =
            upcomingWidgetModel(
                NepaliDate.of(2083, 6, 25),
                Lang.ENGLISH,
                Digits.ENGLISH,
                english,
                showNepal = false,
            )
        assertEquals("Ghatasthapana Brata", model.entries.first().name)
        assertTrue(model.entries.first().isHoliday)
        assertEquals("Today", model.entries.first().relative)
    }

    @Test
    fun followLanguageUsesDevanagariInNepaliAndAsciiInEnglish() {
        val nepaliModel =
            todayWidgetModel(
                NepaliDate.of(2083, 6, 25),
                Lang.NEPALI,
                Digits.FOLLOW_LANGUAGE.resolve(Lang.NEPALI),
                nepali,
                showNepal = true,
            )
        assertEquals("२५", nepaliModel.bsDay)
        assertEquals("आइतवार", nepaliModel.weekday)
        assertEquals("११ अक्टोबर २०२६", nepaliModel.adLine)
        assertEquals("असोज २५ आइतवार, घटस्थापना व्रत", nepaliModel.oneLine)
        assertEquals("नेपाल", nepaliModel.nepalLabel)

        val englishModel =
            todayWidgetModel(
                NepaliDate.of(2083, 6, 25),
                Lang.ENGLISH,
                Digits.FOLLOW_LANGUAGE.resolve(Lang.ENGLISH),
                english,
                showNepal = false,
            )
        assertEquals("25", englishModel.bsDay)
        assertEquals("11 October 2026", englishModel.adLine)
        assertFalse(englishModel.oneLine.any { it in '०'..'९' })
    }

    @Test
    fun explicitDigitsOverrideTheLanguage() {
        val englishDigitsInNepali =
            todayWidgetModel(
                NepaliDate.of(2083, 6, 25),
                Lang.NEPALI,
                Digits.ENGLISH,
                nepali,
                showNepal = false,
            )
        assertEquals("25", englishDigitsInNepali.bsDay)
        assertTrue(englishDigitsInNepali.adLine.contains("11"))
        assertFalse(englishDigitsInNepali.adLine.any { it in '०'..'९' })

        val nepaliDigitsInEnglish =
            todayWidgetModel(
                NepaliDate.of(2083, 6, 25),
                Lang.ENGLISH,
                Digits.NEPALI,
                english,
                showNepal = false,
            )
        assertEquals("२५", nepaliDigitsInEnglish.bsDay)
        assertTrue(nepaliDigitsInEnglish.adLine.contains("११"))
        assertFalse(nepaliDigitsInEnglish.adLine.any { it in '0'..'9' })
    }

    private companion object {
        val BS_MONTHS_EN =
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
        val BS_MONTHS_NE =
            listOf(
                "बैशाख",
                "जेठ",
                "असार",
                "साउन",
                "भदौ",
                "असोज",
                "कार्तिक",
                "मंसिर",
                "पुष",
                "माघ",
                "फागुन",
                "चैत",
            )
        val AD_MONTHS_EN =
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
        val AD_MONTHS_SHORT_EN =
            listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val AD_MONTHS_NE =
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
        val AD_MONTHS_SHORT_NE = AD_MONTHS_NE
    }
}
