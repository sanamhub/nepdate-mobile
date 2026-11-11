package np.nepdate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Reads and writes [Settings] in a Preferences DataStore.
 *
 * The flow emits the defaults on a fresh install. A read error from the file is treated as an empty
 * store, so a corrupt file shows the defaults instead of crashing; a write error still fails.
 */
class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val preferences: Flow<Preferences> =
        dataStore.data.catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }

    /**
     * The stored settings, or the defaults when nothing has been written.
     *
     * @return a cold flow that emits again after every write
     */
    val settings: Flow<Settings> = preferences.map { it.toSettings() }

    /** The last few festival searches, kept on this device only. */
    val recentSearches: RecentSearchStore = RecentSearchStore(dataStore, preferences)

    /** Stores the "Date follows" choice (MR-06). */
    suspend fun setDateFollows(value: DateFollows) {
        dataStore.edit { it[SettingsKeys.DATE_FOLLOWS] = value.name }
    }

    /** Stores the Digits choice (MR-07). */
    suspend fun setDigits(value: Digits) {
        dataStore.edit { it[SettingsKeys.DIGITS] = value.name }
    }

    /** Stores whether the month grid shows a tithi line (MR-01 AC8). */
    suspend fun setShowTithi(value: Boolean) {
        dataStore.edit { it[SettingsKeys.SHOW_TITHI] = value }
    }

    /** Records that the first-launch "Nepal time" suggestion was accepted or dismissed (MR-06 AC1). */
    suspend fun setDateFollowsSuggestionHandled(value: Boolean) {
        dataStore.edit { it[SettingsKeys.SUGGESTION_HANDLED] = value }
    }

    /** Stores the look: phone setting, Paper or Night. */
    suspend fun setLook(value: Look) {
        dataStore.edit { it[SettingsKeys.LOOK] = value.name }
    }

    /** Stores whether surfaces follow the wallpaper colours (Android 12+). */
    suspend fun setWallpaperColors(value: Boolean) {
        dataStore.edit { it[SettingsKeys.WALLPAPER_COLORS] = value }
    }

    /** Records that the first-run screen was completed or skipped. */
    suspend fun setFirstRunDone(value: Boolean) {
        dataStore.edit { it[SettingsKeys.FIRST_RUN_DONE] = value }
    }

    /** Stores whether the month grid shows the AD day next to each BS day. */
    suspend fun setShowAdDates(value: Boolean) {
        dataStore.edit { it[SettingsKeys.SHOW_AD_DATES] = value }
    }

    /** Stores whether the app gives haptic feedback. */
    suspend fun setHaptics(value: Boolean) {
        dataStore.edit { it[SettingsKeys.HAPTICS] = value }
    }

    /** Stores one of the two date notification switches. */
    suspend fun setDateNotice(
        notice: DateNotice,
        value: Boolean,
    ) {
        val key =
            when (notice) {
                DateNotice.STATUS_BAR -> SettingsKeys.STATUS_BAR_DATE
                DateNotice.CARD -> SettingsKeys.NOTIFICATION_DATE
            }
        dataStore.edit { it[key] = value }
    }

    /** Stores the tab a cold start opens on. */
    suspend fun setStartTab(value: StartTab) {
        dataStore.edit { it[SettingsKeys.START_TAB] = value.name }
    }
}
