package np.nepdate.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.testing.unit.hasRunCallbackClickAction
import androidx.glance.appwidget.testing.unit.hasStartActivityClickAction
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasContentDescription
import androidx.glance.testing.unit.hasTestTag
import androidx.glance.testing.unit.hasText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.Digits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The interactive controls of the redesigned widgets and the two festival widgets. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h800dp-xhdpi")
class WidgetControlsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val today = NepaliDate.of(2083, 6, 20)
    private val controls by lazy { widgetControlCopy(context) }

    @Test
    fun theTodayFlipButtonRunsItsCallbackAndTheAdFaceShowsTheAdDate() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(110.dp, 110.dp))
            val model =
                todayWidgetModel(NepaliDate.of(2083, 6, 25), Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, false)
            provideComposable {
                GlanceTheme { TodayWidgetContent(model, WidgetOpacity.SOLID, showAd = true) }
            }

            onNode(hasTestTag("today_flip")).assert(hasRunCallbackClickAction<TodayFaceAction>())
            onNode(hasTestTag("today_flip")).assert(hasContentDescription("Show the Bikram Sambat date"))
            onNode(hasText("October 2026")).assertExists()
            onNode(hasText("Asoj 25")).assertExists()
        }

    @Test
    fun nextFestivalStartsAtTheNearestAndWrapsBothWays() {
        val first = nextFestivalModel(today, 0, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, controls)
        assertEquals(0, first.index)
        assertEquals(festivalWindow(today).size - 1, first.previousIndex)
        assertEquals(1, first.nextIndex)

        val last =
            nextFestivalModel(
                today,
                festivalWindow(today).size - 1,
                Lang.ENGLISH,
                Digits.ENGLISH,
                englishWidgetCopy,
                controls,
            )
        assertEquals(0, last.nextIndex)

        val stale = nextFestivalModel(today, 99, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, controls)
        assertEquals(0, stale.index)
    }

    @Test
    fun nextFestivalCountsTheDaysLeftAndPicksTheHolidayKind() {
        val entries = festivalWindow(today)
        val holidayIndex = entries.indexOfFirst { it.isHoliday }
        val model = nextFestivalModel(today, holidayIndex, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, controls)

        assertTrue("a public holiday is within reach of the arrows", holidayIndex >= 0)
        assertTrue(model.isHoliday)
        assertEquals("Next public holiday", model.kind)
        assertEquals(entries[holidayIndex].daysFromToday.toString(), model.days)
    }

    @Test
    fun theFestivalArrowsCarryTheIndexToShow() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(160.dp, 160.dp))
            val model = nextFestivalModel(today, 0, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, controls)
            provideComposable {
                GlanceTheme { NextFestivalWidgetContent(model, WidgetOpacity.SOLID) }
            }

            onNode(hasTestTag("next_festival_next"))
                .assert(hasRunCallbackClickAction<FestivalStepAction>(actionParametersOf(FESTIVAL_INDEX_PARAM to 1)))
            onNode(hasTestTag("next_festival_previous"))
                .assert(
                    hasRunCallbackClickAction<FestivalStepAction>(
                        actionParametersOf(FESTIVAL_INDEX_PARAM to festivalWindow(today).size - 1),
                    ),
                )
            onNode(hasTestTag("next_festival_widget"))
                .assert(hasStartActivityClickAction(widgetIntent(context, model.entry?.date)))
        }

    @Test
    fun theHolidaysOnlyFilterKeepsOnlyPublicHolidays() {
        val all =
            festivalsListModel(
                today,
                false,
                FESTIVALS_MAX_ROWS,
                Digits.ENGLISH,
                Lang.ENGLISH,
                englishWidgetCopy,
                controls,
            )
        val holidays =
            festivalsListModel(
                today,
                true,
                FESTIVALS_MAX_ROWS,
                Digits.ENGLISH,
                Lang.ENGLISH,
                englishWidgetCopy,
                controls,
            )

        assertEquals(FESTIVALS_MAX_ROWS, all.rows.size)
        assertFalse(all.holidaysOnly)
        assertEquals(FESTIVALS_MAX_ROWS, holidays.rows.size)
        assertTrue(holidays.rows.all { it.isHoliday })
    }

    @Test
    fun aFestivalRowOpensItsDayAndTheChipTogglesTheFilter() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(320.dp, 250.dp))
            val model =
                festivalsListModel(
                    today,
                    false,
                    FESTIVALS_MAX_ROWS,
                    Digits.ENGLISH,
                    Lang.ENGLISH,
                    englishWidgetCopy,
                    controls,
                )
            provideComposable {
                GlanceTheme { FestivalsWidgetContent(model, WidgetOpacity.SOLID) }
            }

            onNode(hasTestTag("festivals_entry_0"))
                .assert(hasStartActivityClickAction(widgetIntent(context, model.rows.first().date)))
            onNode(hasTestTag("festivals_filter")).assert(hasRunCallbackClickAction<HolidayFilterAction>())
        }
}
