package np.nepdate.ui.upcoming

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Named
import io.github.sanamhub.nepdate.patro.Patro

/** One event in the Upcoming list, with its distance from today (MR-03 AC1). */
internal data class UpcomingEntry(
    val date: NepaliDate,
    val name: Named,
    val isHoliday: Boolean,
    val daysFromToday: Int,
)

/** The entries of one BS month, in date and file order. */
internal data class UpcomingGroup(
    val year: Int,
    val month: Int,
    val entries: List<UpcomingEntry>,
)

/** The next entries and, when fewer than the limit remain, the last day of coverage (MR-03 AC3). */
internal data class UpcomingList(
    val groups: List<UpcomingGroup>,
    val dataEndsOn: NepaliDate?,
) {
    val entries: List<UpcomingEntry> get() = groups.flatMap { it.entries }
}

/**
 * The next [limit] public holidays and events from [today], in date order and then file order.
 *
 * The library owns the dates and the event names. When the coverage ends before [limit] entries, the
 * result carries the last day of coverage so the list can say where the data ends.
 */
internal fun upcoming(
    today: NepaliDate,
    limit: Int = 20,
): UpcomingList {
    require(limit > 0) { "limit must be positive" }
    val start = maxOf(today, Patro.coverage.start)
    val entries = mutableListOf<UpcomingEntry>()
    var date = start
    while (date <= Patro.coverage.endInclusive && entries.size < limit) {
        val info = Patro.info(date)
        val days = today.daysUntil(date).toInt()
        info.events.forEach { event ->
            if (entries.size < limit) {
                entries +=
                    UpcomingEntry(
                        date = date,
                        name = event,
                        isHoliday = info.isPublicHoliday,
                        daysFromToday = days,
                    )
            }
        }
        date = date.plusDays(1)
    }
    val groups =
        entries
            .groupBy { it.date.year to it.date.monthValue }
            .map { (key, groupEntries) -> UpcomingGroup(key.first, key.second, groupEntries) }
    val dataEndsOn = if (entries.size < limit) Patro.coverage.endInclusive else null
    return UpcomingList(groups, dataEndsOn)
}
