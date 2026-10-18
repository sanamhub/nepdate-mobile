package np.nepdate.data

import io.github.sanamhub.nepdate.KATHMANDU
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toNepaliDate
import java.time.Clock
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Turns the injected [clock] into today's BS date in the zone the settings choose (MR-06, A1-01).
 *
 * The device zone can be injected for a test. When it is null, the zone is read on every call, so a
 * time-zone change is seen without rebuilding the provider.
 */
class TodayProvider(
    private val clock: Clock,
    private val deviceZone: ZoneId? = null,
) {
    /**
     * Today's BS date in the chosen zone.
     *
     * @return the BS date at [clock]'s instant, in Nepal time or the device zone
     */
    fun today(dateFollows: DateFollows): NepaliDate = now(dateFollows).toLocalDate().toNepaliDate()

    /** The current date and time in the zone the settings choose. */
    fun now(dateFollows: DateFollows): ZonedDateTime = clock.instant().atZone(zone(dateFollows))

    /**
     * Milliseconds until local midnight in the chosen zone. The foreground ticker uses it to refresh
     * the today ring while the app stays open (MR-06 AC2).
     */
    fun nextMidnight(dateFollows: DateFollows): ZonedDateTime {
        val now = now(dateFollows)
        return now.toLocalDate().plusDays(1).atStartOfDay(now.zone)
    }

    /** Milliseconds until local midnight in the chosen zone, for the foreground today ticker. */
    fun millisUntilNextMidnight(dateFollows: DateFollows): Long =
        Duration.between(now(dateFollows), nextMidnight(dateFollows)).toMillis().coerceAtLeast(1)

    private fun zone(dateFollows: DateFollows): ZoneId =
        if (dateFollows == DateFollows.NEPAL) KATHMANDU else deviceZone ?: ZoneId.systemDefault()
}
