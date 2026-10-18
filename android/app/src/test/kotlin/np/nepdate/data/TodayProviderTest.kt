package np.nepdate.data

import io.github.sanamhub.nepdate.NepaliDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** A1-01 and MR-06 AC1: today in Nepal time or the device zone. */
class TodayProviderTest {
    private val clock = Clock.fixed(Instant.parse("2024-07-30T18:30:00Z"), ZoneOffset.UTC)
    private val newYork = ZoneId.of("America/New_York")

    @Test
    fun nepalTimeIsTheNextBsDay() {
        val provider = TodayProvider(clock, newYork)

        assertEquals(NepaliDate.of(2081, 4, 16), provider.today(DateFollows.NEPAL))
    }

    @Test
    fun deviceTimeIsStillTheSameAdDay() {
        val provider = TodayProvider(clock, newYork)

        assertEquals(NepaliDate.of(2081, 4, 15), provider.today(DateFollows.DEVICE))
    }

    @Test
    fun aFreshInstallUsesTheDeviceZone() {
        val provider = TodayProvider(clock, newYork)
        val freshSettings = Settings()

        assertEquals(DateFollows.DEVICE, freshSettings.dateFollows)
        assertEquals(NepaliDate.of(2081, 4, 15), provider.today(freshSettings.dateFollows))
    }
}
