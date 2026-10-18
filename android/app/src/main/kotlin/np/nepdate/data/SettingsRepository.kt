package np.nepdate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Reads and writes [Settings] in a Preferences DataStore (A1-01).
 *
 * The flow emits the defaults on a fresh install. A read error from the file is treated as an empty
 * store, so a corrupt file shows the defaults instead of crashing; a write error still fails.
 */
class SettingsRepository(
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * The stored settings, or the defaults when nothing has been written.
     *
     * @return a cold flow that emits again after every write
     */
    val settings: Flow<Settings> =
        dataStore.data
            .catch { error ->
                if (error is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw error
                }
            }.map { preferences -> preferences.toSettings() }

    /** Stores the "Date follows" choice (MR-06). */
    suspend fun setDateFollows(value: DateFollows) {
        dataStore.edit { it[DATE_FOLLOWS] = value.name }
    }

    /** Stores the Digits choice (MR-07). */
    suspend fun setDigits(value: Digits) {
        dataStore.edit { it[DIGITS] = value.name }
    }

    /** Stores whether the month grid shows a tithi line (MR-01 AC8). */
    suspend fun setShowTithi(value: Boolean) {
        dataStore.edit { it[SHOW_TITHI] = value }
    }

    /** Records that the first-launch "Nepal time" suggestion was accepted or dismissed (MR-06 AC1). */
    suspend fun setDateFollowsSuggestionHandled(value: Boolean) {
        dataStore.edit { it[SUGGESTION_HANDLED] = value }
    }

    private fun Preferences.toSettings(): Settings =
        Settings(
            dateFollows = enumValue(DATE_FOLLOWS, DateFollows.DEVICE),
            digits = enumValue(DIGITS, Digits.FOLLOW_LANGUAGE),
            showTithi = this[SHOW_TITHI] ?: false,
            dateFollowsSuggestionHandled = this[SUGGESTION_HANDLED] ?: false,
        )

    private inline fun <reified T : Enum<T>> Preferences.enumValue(
        key: Preferences.Key<String>,
        fallback: T,
    ): T {
        val stored = this[key] ?: return fallback
        return enumValues<T>().firstOrNull { it.name == stored } ?: fallback
    }

    private companion object {
        val DATE_FOLLOWS = stringPreferencesKey("date_follows")
        val DIGITS = stringPreferencesKey("digits")
        val SHOW_TITHI = booleanPreferencesKey("show_tithi")
        val SUGGESTION_HANDLED = booleanPreferencesKey("date_follows_suggestion_handled")
    }
}
