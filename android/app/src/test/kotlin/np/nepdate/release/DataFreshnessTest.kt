package np.nepdate.release

import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.toLocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** ADR-0003 §4: 183 days of public holidays are a failure; tithi and events only warn. */
class DataFreshnessTest {
    @Test
    fun `183 days passes and 182 fails`() {
        val pass = ReleaseDataStatus.evaluate(LocalDate.of(2027, 6, 3), lastHoliday(), lastTithi(), lastEvent())
        val fail = ReleaseDataStatus.evaluate(LocalDate.of(2027, 6, 4), lastHoliday(), lastTithi(), lastEvent())

        assertTrue(pass.failures.isEmpty())
        assertEquals(183, pass.holidayDays)
        assertEquals(1, fail.failures.size)
        assertTrue(fail.failures.single().contains("holiday data has 182 days left"))
    }

    @Test
    fun `tithi below the threshold warns without failing`() {
        val status = ReleaseDataStatus.evaluate(LocalDate.of(2026, 10, 18), lastHoliday(), lastTithi(), lastEvent())

        assertTrue(status.failures.isEmpty())
        assertTrue(status.warnings.any { it.contains("tithi data has") })
    }

    @Test
    fun `the real tables end on the documented dates`() {
        assertEquals(LocalDate.of(2027, 12, 3), lastHoliday())
        assertEquals(LocalDate.of(2027, 4, 13), lastTithi())
        assertEquals(LocalDate.of(2027, 12, 5), lastEvent())
    }

    /** The release preflight sets `RELEASE_DATE`; normal CI runs only the fixed-date tests. */
    @Test
    fun `release gate uses the UTC date`() {
        val releaseDate = System.getenv("RELEASE_DATE")
        assumeTrue("RELEASE_DATE is set only by the release preflight", releaseDate != null)

        val date = LocalDate.parse(releaseDate)
        val status =
            ReleaseDataStatus.evaluate(
                date,
                lastHoliday(),
                lastTithi(),
                lastEvent(),
            )
        val report = status.report(date)
        System.getProperty("releaseStatusFile")?.let { File(it).writeText(report) }
        println(report)
        assertTrue(status.failures.joinToString("\n"), status.failures.isEmpty())
    }

    private fun lastHoliday(): LocalDate = Patro.publicHolidays.last().toLocalDate()

    private fun lastTithi(): LocalDate = Patro.coverage.last { date -> Patro.info(date).tithi != null }.toLocalDate()

    private fun lastEvent(): LocalDate =
        Patro.coverage.last { date -> Patro.info(date).events.isNotEmpty() }.toLocalDate()
}

internal data class ReleaseDataStatus(
    val releaseDate: LocalDate,
    val holidayDays: Int,
    val tithiDays: Int,
    val eventDays: Int,
    val warnings: List<String>,
    val failures: List<String>,
) {
    fun report(date: LocalDate): String =
        buildString {
            appendLine("release-date: $date (UTC)")
            appendLine("holidays: $holidayDays days left")
            appendLine("tithi: $tithiDays days left")
            appendLine("events: $eventDays days left")
            warnings.forEach { appendLine(it) }
            failures.forEach { appendLine(it) }
        }

    companion object {
        private const val THRESHOLD_DAYS = 183

        fun evaluate(
            releaseDate: LocalDate,
            lastHoliday: LocalDate,
            lastTithi: LocalDate,
            lastEvent: LocalDate,
        ): ReleaseDataStatus {
            val holidayDays = ChronoUnit.DAYS.between(releaseDate, lastHoliday).toInt()
            val tithiDays = ChronoUnit.DAYS.between(releaseDate, lastTithi).toInt()
            val eventDays = ChronoUnit.DAYS.between(releaseDate, lastEvent).toInt()
            val warnings = mutableListOf<String>()
            val failures = mutableListOf<String>()
            if (holidayDays < THRESHOLD_DAYS) {
                failures += "holiday data has $holidayDays days left (threshold $THRESHOLD_DAYS)"
            }
            if (tithiDays < THRESHOLD_DAYS) {
                warnings += "warning: tithi data has $tithiDays days left (threshold $THRESHOLD_DAYS)"
            }
            if (eventDays < THRESHOLD_DAYS) {
                warnings += "warning: events data has $eventDays days left (threshold $THRESHOLD_DAYS)"
            }
            return ReleaseDataStatus(releaseDate, holidayDays, tithiDays, eventDays, warnings, failures)
        }
    }
}
