package np.nepdate

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Proves that the composite-build library, both units, is on the app classpath.
 */
class NepaliDateSmokeTest {
    @Test
    fun coreAndPatroAreAvailable() {
        val date = NepaliDate.of(2083, 1, 1)

        assertEquals("2083-01-01", date.toIsoString())
        assertTrue(date in Patro.coverage)
    }
}
