package np.nepdate.data

import io.github.sanamhub.nepdate.KATHMANDU
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toNepaliDate
import java.time.Clock
import java.time.ZoneId

/**
 * Turns the injected [clock] into today's BS date in the zone the settings choose (MR-06, A1-01).
 *
 * The device zone is injected as well as the clock, so a test can put the device in New York
 * without changing the JVM's default zone.
 */
class TodayProvider(
    private val clock: Clock,
    private val deviceZone: ZoneId = ZoneId.systemDefault(),
) {
    /**
     * Today's BS date in the chosen zone.
     *
     * @return the BS date at [clock]'s instant, in Nepal time or [deviceZone]
     */
    fun today(dateFollows: DateFollows): NepaliDate {
        val zone = if (dateFollows == DateFollows.NEPAL) KATHMANDU else deviceZone
        return clock
            .instant()
            .atZone(zone)
            .toLocalDate()
            .toNepaliDate()
    }
}
