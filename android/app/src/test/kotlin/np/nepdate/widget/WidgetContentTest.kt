package np.nepdate.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.testing.unit.hasRunCallbackClickAction
import androidx.glance.appwidget.testing.unit.hasStartActivityClickAction
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasTestTag
import androidx.glance.testing.unit.hasText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h800dp-xhdpi")
class WidgetContentTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun w01EnglishDigitsAt4x1() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.ONE_LINE)
            provideComposable {
                GlanceTheme {
                    TodayWidgetContent(
                        todayModel(Digits.ENGLISH, Lang.ENGLISH),
                        WidgetOpacity.SOLID,
                    )
                }
            }

            onNode(hasText("Asoj 25 Sunday, Ghatasthapana Brata")).assertExists()
            onNode(hasText("25")).assertExists()
            onNode(hasText("२५")).assertDoesNotExist()
        }

    @Test
    fun w01NepaliDigitsWithANegativeAsciiControl() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.SMALL)
            provideComposable {
                GlanceTheme {
                    TodayWidgetContent(
                        todayModel(Digits.NEPALI, Lang.NEPALI),
                        WidgetOpacity.SOLID,
                    )
                }
            }

            onNode(hasText("२५")).assertExists()
            onNode(hasText("असोज · आइत")).assertExists()
            onAllNodes(hasText("25")).assertCountEquals(0)
        }

    @Test
    fun w01TapOpensToday() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.SMALL)
            provideComposable {
                GlanceTheme {
                    TodayWidgetContent(todayModel(Digits.ENGLISH, Lang.ENGLISH), WidgetOpacity.SOLID)
                }
            }

            onNode(hasTestTag("today_widget"))
                .assert(hasStartActivityClickAction(widgetIntent(context)))
        }

    @Test
    fun w02TapTargetsCarryTodayAndTheEntryDate() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.WIDE)
            val model = upcomingModel(Digits.ENGLISH, Lang.ENGLISH)
            provideComposable {
                GlanceTheme {
                    UpcomingWidgetContent(model, WidgetOpacity.SOLID)
                }
            }

            onNode(hasTestTag("upcoming_today"))
                .assert(hasStartActivityClickAction(widgetIntent(context)))
            onNode(hasTestTag("upcoming_entry_0"))
                .assert(
                    hasStartActivityClickAction(
                        widgetIntent(context, model.entries.first().date),
                    ),
                )
        }

    @Test
    fun w02EndsWithTheCoverageFooter() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.WIDE)
            val model =
                upcomingWidgetModel(
                    NepaliDate.of(2084, 8, 20),
                    Lang.ENGLISH,
                    Digits.ENGLISH,
                    englishCopy,
                    showNepal = false,
                )
            provideComposable {
                GlanceTheme {
                    UpcomingWidgetContent(model, WidgetOpacity.SOLID)
                }
            }

            onNode(hasText("Holiday data ends on Mangsir 20, 2084")).assertExists()
        }

    @Test
    fun w03NavigationCallbacksCarryTheAdjacentMonths() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.MONTH)
            val model = monthModel(Digits.ENGLISH, Lang.ENGLISH)
            provideComposable {
                GlanceTheme {
                    MonthWidgetContent(model, WidgetOpacity.SOLID)
                }
            }

            onNode(hasTestTag("month_previous"))
                .assert(
                    hasRunCallbackClickAction<MonthNavigationAction>(
                        androidx.glance.action.actionParametersOf(
                            YEAR_PARAM to 2083,
                            MONTH_PARAM to 5,
                        ),
                    ),
                )
            onNode(hasTestTag("month_next"))
                .assert(
                    hasRunCallbackClickAction<MonthNavigationAction>(
                        androidx.glance.action.actionParametersOf(
                            YEAR_PARAM to 2083,
                            MONTH_PARAM to 7,
                        ),
                    ),
                )
        }

    @Test
    fun w03TodayCallbackRestoresTheCurrentMonth() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.MONTH)
            val model =
                monthWidgetModel(
                    year = 2083,
                    monthValue = 7,
                    today = NepaliDate.of(2083, 6, 25),
                    lang = Lang.ENGLISH,
                    digits = Digits.ENGLISH,
                    copy = englishCopy,
                )
            provideComposable {
                GlanceTheme { MonthWidgetContent(model, WidgetOpacity.SOLID) }
            }

            onNode(hasTestTag("month_today"))
                .assert(
                    hasRunCallbackClickAction<MonthNavigationAction>(
                        androidx.glance.action.actionParametersOf(
                            YEAR_PARAM to 2083,
                            MONTH_PARAM to 6,
                        ),
                    ),
                )
        }

    @Test
    fun w03DayTapCarriesTheDeepLinkExtra() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.MONTH)
            provideComposable {
                GlanceTheme {
                    MonthWidgetContent(monthModel(Digits.ENGLISH, Lang.ENGLISH), WidgetOpacity.SOLID)
                }
            }

            onNode(hasTestTag("month_day_2083-06-25"))
                .assert(
                    hasStartActivityClickAction(
                        widgetIntent(context, NepaliDate.of(2083, 6, 25)),
                    ),
                )
        }

    @Test
    fun w03UsesNepaliDigitsAndTheNegativeAsciiControl() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(GlanceAppWidgetSize.MONTH)
            provideComposable {
                GlanceTheme {
                    MonthWidgetContent(monthModel(Digits.NEPALI, Lang.NEPALI), WidgetOpacity.SOLID)
                }
            }

            onAllNodes(hasText("२५")).assertAny(
                androidx.glance.testing.unit
                    .hasTextEqualTo("२५"),
            )
            onAllNodes(hasText("25")).assertCountEquals(0)
        }

    @Test
    fun theIntentCarriesTheBsDateExtra() {
        val date = NepaliDate.of(2083, 6, 25)
        val intent = widgetIntent(context, date)
        org.junit.Assert.assertEquals("2083-06-25", intent.getStringExtra(EXTRA_BS_DATE))
        org.junit.Assert.assertEquals(date, dateFromIntent(intent))
    }

    private fun todayModel(
        digits: Digits,
        lang: Lang,
    ): TodayWidgetModel =
        todayWidgetModel(
            date = NepaliDate.of(2083, 6, 25),
            lang = lang,
            digits = digits,
            copy = if (lang == Lang.NEPALI) nepaliCopy else englishCopy,
            showNepal = false,
        )

    private fun upcomingModel(
        digits: Digits,
        lang: Lang,
    ): UpcomingWidgetModel =
        upcomingWidgetModel(
            date = NepaliDate.of(2083, 6, 20),
            lang = lang,
            digits = digits,
            copy = if (lang == Lang.NEPALI) nepaliCopy else englishCopy,
            showNepal = false,
        )

    private fun monthModel(
        digits: Digits,
        lang: Lang,
    ): MonthWidgetModel =
        monthWidgetModel(
            year = 2083,
            monthValue = 6,
            today = NepaliDate.of(2083, 6, 25),
            lang = lang,
            digits = digits,
            copy = if (lang == Lang.NEPALI) nepaliCopy else englishCopy,
        )
}

private object GlanceAppWidgetSize {
    val SMALL = DpSize(110.dp, 110.dp)
    val ONE_LINE = DpSize(250.dp, 70.dp)
    val WIDE = DpSize(250.dp, 110.dp)
    val MONTH = DpSize(250.dp, 250.dp)
}

private val BS_MONTHS_EN =
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

private val BS_MONTHS_NE =
    listOf("बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज", "कार्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत")

private val AD_MONTHS_EN =
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

private val AD_MONTHS_SHORT_EN =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

private val AD_MONTHS_NE =
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

private val englishCopy =
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

private val nepaliCopy =
    WidgetCopy(
        "नेपाल",
        "सार्वजनिक बिदा",
        "अघिल्लो महिना",
        "अर्को महिना",
        "आज",
        BS_MONTHS_NE,
        AD_MONTHS_NE,
        AD_MONTHS_NE,
        "आज",
        "भोलि",
        { "$it दिनमा" },
        { "बिदा र कार्यक्रमको जानकारी $it मा सकिन्छ" },
    )
