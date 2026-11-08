package np.nepdate.widget

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.testing.unit.hasStartActivityClickAction
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasTestTag
import androidx.glance.testing.unit.hasText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import np.nepdate.data.Digits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The widgets added by the widget review (item 10) and the open-the-app rule (item 8). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h800dp-xhdpi")
class NewWidgetsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val today = NepaliDate.of(2083, 6, 25)

    @Test
    fun theWeekRunsSundayToSaturdayAroundToday() {
        val model = weekWidgetModel(today, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy)

        assertEquals(7, model.days.size)
        val first = model.days.first().date
        val last = model.days.last().date
        assertEquals(Weekday.entries.first(), first.dayOfWeek)
        assertEquals(Weekday.SATURDAY, last.dayOfWeek)
        assertEquals(listOf(today), model.days.filter { it.isToday }.map { it.date })
        assertTrue(model.days.single { it.isToday }.isHoliday)
        assertEquals("Asoj 2083", model.title)
        assertTrue(model.next!!.date > today)
    }

    @Test
    fun aWeekDayOpensItsOwnDate() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(320.dp, 110.dp))
            val model = weekWidgetModel(today, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy)
            provideComposable { GlanceTheme { WeekWidgetContent(model, WidgetOpacity.SOLID) } }

            onNode(hasTestTag("week_day_2083-06-26"))
                .assert(hasStartActivityClickAction(widgetIntent(context, NepaliDate.of(2083, 6, 26))))
            onNode(hasTestTag("week_widget")).assert(hasStartActivityClickAction(widgetIntent(context)))
            onNode(hasText("Asoj 2083")).assertExists()
        }

    @Test
    fun aOneCellDateChipDrawsTheNumeralAlone() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(60.dp, 60.dp))
            val model = todayWidgetModel(today, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, false)
            provideComposable { GlanceTheme { DateChipContent(model, WidgetOpacity.SOLID) } }

            onNode(hasText("25")).assertExists()
            onNode(hasText("Asoj")).assertDoesNotExist()
        }

    @Test
    fun theDateChipShowsTheMonthAndOpensTheApp() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(110.dp, 110.dp))
            val model = todayWidgetModel(today, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, false)
            provideComposable { GlanceTheme { DateChipContent(model, WidgetOpacity.SOLID) } }

            onNode(hasText("Asoj")).assertExists()
            onNode(hasText("25")).assertExists()
            onNode(hasTestTag("chip_widget")).assert(hasStartActivityClickAction(widgetIntent(context)))
        }

    @Test
    fun theWholeTodayAndUpcomingSurfaceOpensTheApp() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(330.dp, 160.dp))
            val model = upcomingWidgetModel(today, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, false)
            provideComposable { GlanceTheme { UpcomingWidgetContent(model, WidgetOpacity.SOLID) } }

            onNode(hasTestTag("upcoming_widget")).assert(hasStartActivityClickAction(widgetIntent(context)))
            onNode(hasTestTag("upcoming_entry_0"))
                .assert(hasStartActivityClickAction(widgetIntent(context, model.entries.first().date)))
            onNode(hasText("Coming up")).assertExists()
        }

    @Test
    fun theWideTodayFaceShowsTheWeekdayAndTheTithi() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setAppWidgetSize(DpSize(330.dp, 160.dp))
            val model = todayWidgetModel(today, Lang.ENGLISH, Digits.ENGLISH, englishWidgetCopy, false)
            provideComposable { GlanceTheme { TodayWidgetContent(model, WidgetOpacity.SOLID) } }

            assertTrue(model.tithi.isNotEmpty())
            onNode(hasText(model.tithi)).assertExists()
            onNode(hasText("Sunday")).assertExists()
        }
}
