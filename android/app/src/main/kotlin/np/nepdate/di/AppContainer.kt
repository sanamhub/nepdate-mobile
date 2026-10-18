package np.nepdate.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import np.nepdate.data.SettingsRepository
import np.nepdate.data.TodayProvider
import java.time.Clock
import java.time.ZoneId

/**
 * Manual dependency injection for the app (ADR-0005). One instance lives on the [np.nepdate.NepdateApplication].
 */
class AppContainer(
    context: Context,
    clock: Clock = Clock.systemDefaultZone(),
    deviceZone: ZoneId? = null,
) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.applicationContext.preferencesDataStoreFile(SETTINGS_FILE) },
        )

    /** The persisted MR-06 and MR-07 settings. */
    val settingsRepository: SettingsRepository = SettingsRepository(dataStore)

    /** Today's BS date from the chosen zone. */
    val todayProvider: TodayProvider = TodayProvider(clock, deviceZone)

    private companion object {
        const val SETTINGS_FILE = "settings"
    }
}
