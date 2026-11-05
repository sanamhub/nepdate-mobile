package np.nepdate

import org.junit.Assert.assertEquals
import org.junit.Test

/** A1-06: About's library version comes from the version catalog through Gradle. */
class BuildConfigTest {
    @Test
    fun libraryVersionIsTheCatalogPin() {
        assertEquals("1.0.0-rc.1", BuildConfig.NEPDATE_VERSION)
    }
}
