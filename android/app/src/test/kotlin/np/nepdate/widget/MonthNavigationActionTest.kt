package np.nepdate.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Widget review item 1: Previous, Next and Today did nothing on the device. The state transition is
 * tested against real Glance state, and every callback is built the way Glance builds it.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MonthNavigationActionTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val today = NepaliDate.of(2083, 7, 20)

    @Test
    fun aTapStoresTheTargetMonthForThatWidgetOnlyAndRedrawsIt() =
        runBlocking {
            val tapped = glanceId(41)
            val other = glanceId(42)
            val redrawn = mutableListOf<GlanceId>()

            applyMonthNavigation(context, tapped, 2083, 8) { _, id -> redrawn += id }

            assertEquals(2083 to 8, shownMonth(state(tapped), today))
            assertEquals(today.year to today.monthValue, shownMonth(state(other), today))
            assertEquals(listOf(tapped), redrawn)
        }

    @Test
    fun nextThenTodayReturnsToTheCurrentMonth() =
        runBlocking {
            val id = glanceId(43)
            val target = monthStep(today.year, today.monthValue, 1)!!
            applyMonthNavigation(context, id, target.first, target.second) { _, _ -> }
            assertEquals(2083 to 8, shownMonth(state(id), today))

            applyMonthNavigation(context, id, today.year, today.monthValue) { _, _ -> }
            assertEquals(2083 to 7, shownMonth(state(id), today))
        }

    @Test
    fun anOutOfRangeMonthChangesNothingAndDoesNotRedraw() =
        runBlocking {
            val id = glanceId(44)
            var redraws = 0
            applyMonthNavigation(context, id, 2083, 13) { _, _ -> redraws++ }
            assertEquals(0, redraws)
            assertEquals(today.year to today.monthValue, shownMonth(state(id), today))
        }

    /**
     * Glance runs a callback with `Class.forName(name).getDeclaredConstructor().newInstance()` and only
     * logs a failure. R8 full mode removed that constructor, which is why the buttons were dead in the
     * shrunk build; each callback now carries `@Keep`. This checks the reflective path itself.
     */
    @Test
    fun everyCallbackCanBeBuiltTheWayGlanceBuildsIt() {
        listOf(
            MonthNavigationAction::class.java,
            TodayFaceAction::class.java,
            FestivalStepAction::class.java,
            HolidayFilterAction::class.java,
        ).forEach { type ->
            val callback = Class.forName(type.name).getDeclaredConstructor().newInstance()
            assertTrue("${type.simpleName} is an ActionCallback", callback is ActionCallback)
        }
    }

    private fun glanceId(appWidgetId: Int): GlanceId =
        GlanceAppWidgetManager(context)
            .getGlanceIdBy(Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))!!

    private suspend fun state(id: GlanceId): Preferences =
        getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
}
