package np.nepdate

import org.junit.Assert.assertEquals
import org.junit.Test

/** About's library version comes from the version catalog through Gradle. */
class BuildConfigTest {
    @Test
    fun libraryVersionIsTheCatalogPin() {
        assertEquals("1.0.0-rc.1", BuildConfig.NEPDATE_VERSION)
    }
}
