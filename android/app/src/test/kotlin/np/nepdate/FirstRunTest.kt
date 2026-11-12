package np.nepdate

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import np.nepdate.data.SettingsRepository
import np.nepdate.ui.NepdateDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** MR-15: the first-run screen shows once, and the shell opens the Patro after it. */
class FirstRunTest {
    @Test
    fun aFreshInstallShowsTheFirstRunThenRemembersIt() =
        runBlocking {
            val repository = SettingsRepository(FakeStore())

            assertFalse(repository.settings.first().firstRunDone)

            repository.setFirstRunDone(true)

            assertTrue(repository.settings.first().firstRunDone)
        }

    @Test
    fun theShellShowsTheFirstRunUntilTheFlagIsSet() {
        assertEquals(-1, ShellScreen(firstRunDone = false, overlay = Overlay.NONE).depth)
        assertEquals(0, ShellScreen(firstRunDone = true, overlay = Overlay.NONE).depth)

        val navigation =
            AppNavigation(
                overlayState = mutableStateOf(Overlay.NONE),
                destinationState = mutableStateOf(NepdateDestination.MONTH),
            )
        assertEquals(NepdateDestination.MONTH, navigation.destination)
    }
}

/** An in-memory Preferences DataStore, as the repository tests use. */
private class FakeStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
