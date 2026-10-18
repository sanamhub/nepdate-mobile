package np.nepdate.schedule

import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class MidnightSchedulerTest {
    private val nepal = ZoneId.of("Asia/Kathmandu")

    @Test
    fun kathmanduNextMidnightKeepsItsQuarterHourOffset() {
        val clock = Clock.fixed(Instant.parse("2026-10-18T18:14:30Z"), ZoneOffset.UTC)
        val provider = TodayProvider(clock, ZoneId.of("UTC"))

        val next = provider.nextMidnight(DateFollows.NEPAL)

        assertEquals(Instant.parse("2026-10-18T18:15:00Z"), next.toInstant())
        assertEquals(nepal, next.zone)
        assertEquals(30_000L, Duration.between(clock.instant(), next.toInstant()).toMillis())
    }

    @Test
    fun newYorkCrossesTheSpringDstChange() {
        val clock = Clock.fixed(Instant.parse("2026-03-08T05:30:00Z"), ZoneOffset.UTC)
        val provider = TodayProvider(clock, ZoneId.of("America/New_York"))

        val next = provider.nextMidnight(DateFollows.DEVICE)

        assertEquals(Instant.parse("2026-03-09T04:00:00Z"), next.toInstant())
        assertEquals(Duration.ofMinutes(1350), Duration.between(clock.instant(), next.toInstant()))
    }

    @Test
    fun sydneyCrossesTheSpringDstChange() {
        val clock = Clock.fixed(Instant.parse("2026-10-03T14:30:00Z"), ZoneOffset.UTC)
        val provider = TodayProvider(clock, ZoneId.of("Australia/Sydney"))

        val next = provider.nextMidnight(DateFollows.DEVICE)

        assertEquals(Instant.parse("2026-10-04T13:00:00Z"), next.toInstant())
        assertEquals(Duration.ofMinutes(1350), Duration.between(clock.instant(), next.toInstant()))
    }

    @Test
    fun nepalTimeOverridesTheDeviceZone() {
        val clock = Clock.fixed(Instant.parse("2026-10-18T18:14:30Z"), ZoneOffset.UTC)
        val provider = TodayProvider(clock, ZoneId.of("America/New_York"))

        val next = provider.nextMidnight(DateFollows.NEPAL)

        assertEquals(Instant.parse("2026-10-18T18:15:00Z"), next.toInstant())
    }

    @Test
    fun schedulerArmsOneTenMinuteInexactWindow() {
        val clock = Clock.fixed(Instant.parse("2026-10-18T18:14:30Z"), ZoneOffset.UTC)
        val provider = TodayProvider(clock, ZoneId.of("UTC"))
        var trigger = 0L
        var window = 0L
        val scheduler =
            MidnightScheduler(provider) { at, length ->
                trigger = at
                window = length
            }

        scheduler.schedule(Settings(dateFollows = DateFollows.NEPAL))

        assertEquals(Instant.parse("2026-10-18T18:15:00Z").toEpochMilli(), trigger)
        assertEquals(10 * 60 * 1000L, window)
        assertTrue(window < 60 * 60 * 1000L)
    }
}
