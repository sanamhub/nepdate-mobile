package np.nepdate.schedule

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class WidgetRefreshTest {
    @Test
    fun refreshClearsTheMonthThenUpdatesThenRearms() {
        runBlocking {
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
                TodayProvider(
                    Clock.fixed(Instant.parse("2026-10-18T18:14:30Z"), ZoneOffset.UTC),
                    ZoneOffset.UTC,
                )
            var armed = 0L
            val scheduler =
                MidnightScheduler(provider) { at, _ ->
                    armed = at
                    calls += "alarm"
                }

            WidgetRefresh(updater, scheduler).refresh(
                Settings(dateFollows = DateFollows.NEPAL),
                resetMonth = true,
            )

            assertEquals(listOf("clear", "update", "alarm"), calls)
            assertEquals(Instant.parse("2026-10-18T18:15:00Z").toEpochMilli(), armed)
        }
    }

    @Test
    fun aSettingsChangeSkipsTheMonthResetButStillUpdates() {
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
        val provider = TodayProvider(Clock.systemUTC(), ZoneOffset.UTC)
        val refresh = WidgetRefresh(updater, MidnightScheduler(provider) { _, _ -> calls += "alarm" })
        val state = MutableStateFlow(Settings())
        val scope = CoroutineScope(Dispatchers.Unconfined)
        SettingsWidgetSync(state) { refresh.refresh(it, resetMonth = false) }.start(scope)

        state.value = Settings(showTithi = true)
        runBlocking { yield() }

        assertEquals(listOf("update", "alarm"), calls)
    }

    @Test
    fun aSettingsChangeReachesTheObserverWithinOneSecond() {
        runBlocking {
            val seen = CompletableDeferred<Settings>()
            val state = MutableStateFlow(Settings())
            val scope = CoroutineScope(Dispatchers.Unconfined)
            try {
                SettingsWidgetSync(state) { settings ->
                    seen.complete(settings)
                }.start(scope)

                state.value = Settings(showTithi = true)

                val changed = withTimeout(1_000) { seen.await() }
                assertTrue(changed.showTithi)
            } finally {
                scope.cancel()
            }
        }
    }
}
