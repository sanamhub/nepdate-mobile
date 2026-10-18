package np.nepdate

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import np.nepdate.data.DateFollows
import np.nepdate.data.TodayProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.TimeZone

/** Today follows local midnight, a clock change, a time-zone change and a return to the foreground. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class RememberTodayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun todayIsReadAgainOnResume() {
        val clock = MutableClock(Instant.parse("2024-07-30T12:00:00Z"))
        val provider = TodayProvider(clock, ZoneId.of("UTC"))
        setToday(provider)

        composeRule.onNodeWithText("2081-04-15").assertExists()

        clock.now = clock.now.plus(Duration.ofDays(1))
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.onNodeWithText("2081-04-16").assertExists()
    }

    @Test
    fun todayIsReadAgainOnAClockChange() {
        val clock = MutableClock(Instant.parse("2024-07-30T12:00:00Z"))
        val provider = TodayProvider(clock, ZoneId.of("UTC"))
        setToday(provider)

        clock.now = clock.now.plus(Duration.ofDays(1))
        composeRule.activity.sendBroadcast(Intent(Intent.ACTION_TIME_CHANGED))
        composeRule.waitForIdle()

        composeRule.onNodeWithText("2081-04-16").assertExists()
    }

    @Test
    fun todayIsReadAgainOnATimeZoneChange() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            val clock = MutableClock(Instant.parse("2024-07-30T18:30:00Z"))
            val provider = TodayProvider(clock)
            setToday(provider)
            composeRule.onNodeWithText("2081-04-15").assertExists()

            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kathmandu"))
            composeRule.activity.sendBroadcast(Intent(Intent.ACTION_TIMEZONE_CHANGED))
            composeRule.waitForIdle()

            composeRule.onNodeWithText("2081-04-16").assertExists()
        } finally {
            TimeZone.setDefault(original)
        }
    }

    @Test
    fun todayRollsOverAtLocalMidnightWhileTheAppIsOpen() {
        val start = Instant.parse("2024-07-31T03:59:59.500Z")
        val startedAt = System.nanoTime()
        val clock =
            object : Clock() {
                override fun getZone(): ZoneId = ZoneOffset.UTC

                override fun withZone(zone: ZoneId?): Clock = this

                override fun instant(): Instant = start.plusMillis((System.nanoTime() - startedAt) / 1_000_000)
            }
        val provider = TodayProvider(clock, ZoneId.of("America/New_York"))
        setToday(provider)
        composeRule.onNodeWithText("2081-04-15").assertExists()

        composeRule.waitUntil(3_000) {
            composeRule.onAllNodesWithText("2081-04-16").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun setToday(provider: TodayProvider) {
        composeRule.setContent {
            Text(rememberToday(provider, DateFollows.DEVICE).toIsoString())
        }
    }

    private class MutableClock(
        var now: Instant,
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId?): Clock = this

        override fun instant(): Instant = now
    }
}
