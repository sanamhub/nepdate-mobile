package np.nepdate

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
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

/** An app left open overnight shows the new today once it is back in the foreground. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class RememberTodayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun todayIsReadAgainOnResume() {
        val clock = MutableClock(Instant.parse("2024-07-30T12:00:00Z"))
        val provider = TodayProvider(clock, ZoneId.of("UTC"))
        composeRule.setContent {
            Text(rememberToday(provider, DateFollows.DEVICE).toIsoString())
        }
        composeRule.onNodeWithText("2081-04-15").assertExists()

        clock.now = clock.now.plus(Duration.ofDays(1))
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        composeRule.onNodeWithText("2081-04-16").assertExists()
    }

    private class MutableClock(
        var now: Instant,
    ) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC

        override fun withZone(zone: ZoneId?): Clock = this

        override fun instant(): Instant = now
    }
}
