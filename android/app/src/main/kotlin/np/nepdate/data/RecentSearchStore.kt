package np.nepdate.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The last [MAX_RECENT_SEARCHES] festival searches, newest first. They stay in the app's own
 * preferences file on this device; nothing leaves it.
 */
class RecentSearchStore internal constructor(
    private val dataStore: DataStore<Preferences>,
    preferences: Flow<Preferences>,
) {
    /** The stored searches, newest first; empty on a fresh install. */
    val queries: Flow<List<String>> = preferences.map { decode(it[SettingsKeys.RECENT_SEARCHES]) }

    /** Puts [query] at the front, drops an older copy of it and keeps the newest few. */
    suspend fun remember(query: String) {
        dataStore.edit { stored ->
            val updated = withRecent(decode(stored[SettingsKeys.RECENT_SEARCHES]), query)
            stored[SettingsKeys.RECENT_SEARCHES] = encode(updated)
        }
    }

    /** Forgets every stored search. */
    suspend fun clear() {
        dataStore.edit { it.remove(SettingsKeys.RECENT_SEARCHES) }
    }
}

/** How many searches are kept. */
internal const val MAX_RECENT_SEARCHES = 5

/**
 * [recent] with [query] moved to the front, compared without case, and cut to [MAX_RECENT_SEARCHES].
 * A blank query leaves the list as it is.
 */
internal fun withRecent(
    recent: List<String>,
    query: String,
): List<String> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return recent
    return (listOf(trimmed) + recent.filterNot { it.equals(trimmed, ignoreCase = true) }).take(MAX_RECENT_SEARCHES)
}

// A newline cannot be typed into the single-line search field, so it separates the entries safely.
private fun encode(queries: List<String>): String = queries.joinToString(SEPARATOR)

private fun decode(stored: String?): List<String> = stored?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()

private const val SEPARATOR = "\n"
