package np.nepdate.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.preferencesOf
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasText
import androidx.glance.text.Text
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.runBlocking
import np.nepdate.MainActivity
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider
import np.nepdate.schedule.MidnightScheduler
import np.nepdate.schedule.WidgetRefresh
import np.nepdate.schedule.WidgetUpdater
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** Tests added by the A2 review. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h800dp-xhdpi")
class WidgetReviewTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun environment(digits: Digits): WidgetEnvironment =
        WidgetEnvironment(
            settings = Settings(),
            today = NepaliDate.of(2083, 6, 25),
            lang = Lang.ENGLISH,
            digits = digits,
            copy = englishWidgetCopy,
            showNepal = false,
        )

    @Test
    fun aRefreshTokenReloadsTheEnvironmentInARunningSession() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setState(preferencesOf(WIDGET_REFRESH_KEY to 1L))
            provideComposable {
                val shown = rememberWidgetEnvironment(environment(Digits.ENGLISH)) { environment(Digits.NEPALI) }
                Text("digits ${shown.digits}")
            }

            onNode(hasText("digits NEPALI")).assertExists()
            onNode(hasText("digits ENGLISH")).assertDoesNotExist()
        }

    @Test
    fun withoutATokenTheSessionSnapshotIsDrawn() =
        runGlanceAppWidgetUnitTest {
            setContext(context)
            setState(preferencesOf())
            provideComposable {
                val shown = rememberWidgetEnvironment(environment(Digits.ENGLISH)) { environment(Digits.NEPALI) }
                Text("digits ${shown.digits}")
            }

            onNode(hasText("digits ENGLISH")).assertExists()
        }

    @Test
    fun rearmArmsTheAlarmWithoutDrawing() {
        val calls = mutableListOf<String>()
        val updater =
            object : WidgetUpdater {
                override suspend fun updateAll() {
                    calls += "update"
                }

                override suspend fun clearShownMonth() {
                    calls += "clear"
                }
            }
        val provider =
            TodayProvider(Clock.fixed(Instant.parse("2026-10-18T18:14:30Z"), ZoneOffset.UTC), ZoneOffset.UTC)
        var armed = 0L
        val refresh =
            WidgetRefresh(
                updater,
                MidnightScheduler(provider) { at, _ ->
                    armed = at
                    calls += "alarm"
                },
            )

        runBlocking { refresh.rearm(Settings()) }

        assertEquals(listOf("alarm"), calls)
        assertEquals(Instant.parse("2026-10-19T00:00:00Z").toEpochMilli(), armed)
    }

    @Test
    fun aDeepLinkAcceptsOnlyTheStrictIsoDate() {
        fun extra(value: String) = Intent(context, MainActivity::class.java).putExtra(EXTRA_BS_DATE, value)

        assertEquals(NepaliDate.of(2083, 6, 25), dateFromIntent(extra("2083-06-25")))
        assertNull(dateFromIntent(extra("25 Asoj 2083")))
        assertNull(dateFromIntent(extra("3000-01-01")))
        assertNull(dateFromIntent(extra("not a date")))
        assertNull(dateFromIntent(Intent()))
    }

    @Test
    fun theConfigurationActivityRefusesAnIdItDoesNotOwn() {
        val intent =
            Intent(context, WidgetConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, FOREIGN_WIDGET_ID)

        ActivityScenario.launch<WidgetConfigActivity>(intent).use { scenario ->
            assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    private companion object {
        const val FOREIGN_WIDGET_ID = 4242
    }
}
