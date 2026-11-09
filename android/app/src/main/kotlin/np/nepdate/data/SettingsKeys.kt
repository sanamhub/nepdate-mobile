package np.nepdate.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/** The Preferences keys of [Settings] and the recent searches. The names are stored on disk. */
internal object SettingsKeys {
    val DATE_FOLLOWS = stringPreferencesKey("date_follows")
    val DIGITS = stringPreferencesKey("digits")
    val SHOW_TITHI = booleanPreferencesKey("show_tithi")
    val SUGGESTION_HANDLED = booleanPreferencesKey("date_follows_suggestion_handled")
    val LOOK = stringPreferencesKey("look")
    val WALLPAPER_COLORS = booleanPreferencesKey("wallpaper_colors")
    val FIRST_RUN_DONE = booleanPreferencesKey("first_run_done")
    val SHOW_AD_DATES = booleanPreferencesKey("show_ad_dates")
    val HAPTICS = booleanPreferencesKey("haptics")
    val START_TAB = stringPreferencesKey("start_tab")
    val STATUS_BAR_DATE = booleanPreferencesKey("status_bar_date")
    val NOTIFICATION_DATE = booleanPreferencesKey("notification_date")
    val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
}

/** Reads [Settings] from stored preferences; a missing or unknown value falls back to its default. */
internal fun Preferences.toSettings(): Settings {
    val defaults = Settings()
    return Settings(
        dateFollows = enumValue(SettingsKeys.DATE_FOLLOWS, defaults.dateFollows),
        digits = enumValue(SettingsKeys.DIGITS, defaults.digits),
        showTithi = this[SettingsKeys.SHOW_TITHI] ?: defaults.showTithi,
        dateFollowsSuggestionHandled = this[SettingsKeys.SUGGESTION_HANDLED] ?: defaults.dateFollowsSuggestionHandled,
        look = enumValue(SettingsKeys.LOOK, defaults.look),
        wallpaperColors = this[SettingsKeys.WALLPAPER_COLORS] ?: defaults.wallpaperColors,
        firstRunDone = this[SettingsKeys.FIRST_RUN_DONE] ?: defaults.firstRunDone,
        showAdDates = this[SettingsKeys.SHOW_AD_DATES] ?: defaults.showAdDates,
        haptics = this[SettingsKeys.HAPTICS] ?: defaults.haptics,
        startTab = enumValue(SettingsKeys.START_TAB, defaults.startTab),
        statusBarDate = this[SettingsKeys.STATUS_BAR_DATE] ?: defaults.statusBarDate,
        notificationDate = this[SettingsKeys.NOTIFICATION_DATE] ?: defaults.notificationDate,
    )
}

private inline fun <reified T : Enum<T>> Preferences.enumValue(
    key: Preferences.Key<String>,
    fallback: T,
): T {
    val stored = this[key] ?: return fallback
    return enumValues<T>().firstOrNull { it.name == stored } ?: fallback
}
