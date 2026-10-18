package np.nepdate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** A1-01 and MR-06 AC1: the settings store and its fresh-install defaults. */
class SettingsRepositoryTest {
    @Test
    fun freshInstallFollowsTheDevice() =
        runBlocking {
            val settings = SettingsRepository(FakePreferencesDataStore()).settings.first()

            assertEquals(DateFollows.DEVICE, settings.dateFollows)
            assertEquals(Digits.FOLLOW_LANGUAGE, settings.digits)
            assertFalse(settings.showTithi)
            assertFalse(settings.dateFollowsSuggestionHandled)
        }

    @Test
    fun writesEverySetting() =
        runBlocking {
            val repository = SettingsRepository(FakePreferencesDataStore())

            repository.setDateFollows(DateFollows.NEPAL)
            repository.setDigits(Digits.ENGLISH)
            repository.setShowTithi(true)
            repository.setDateFollowsSuggestionHandled(true)

            val settings = repository.settings.first()
            assertEquals(DateFollows.NEPAL, settings.dateFollows)
            assertEquals(Digits.ENGLISH, settings.digits)
            assertTrue(settings.showTithi)
            assertTrue(settings.dateFollowsSuggestionHandled)
        }

    @Test
    fun unknownStoredValuesFallBackToTheDefaults() =
        runBlocking {
            val store =
                FakePreferencesDataStore(
                    preferencesOf(
                        stringPreferencesKey("date_follows") to "MARS",
                        stringPreferencesKey("digits") to "ROMAN",
                    ),
                )

            val settings = SettingsRepository(store).settings.first()
            assertEquals(DateFollows.DEVICE, settings.dateFollows)
            assertEquals(Digits.FOLLOW_LANGUAGE, settings.digits)
        }
}

/**
 * A Preferences DataStore in memory. The repository tests use it because DataStore's file writes are
 * not reliable under Robolectric on Windows; the real store is created by `AppContainer`.
 */
private class FakePreferencesDataStore(
    initial: Preferences = emptyPreferences(),
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}
