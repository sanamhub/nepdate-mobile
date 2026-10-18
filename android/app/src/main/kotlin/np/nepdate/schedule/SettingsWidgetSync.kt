package np.nepdate.schedule

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import np.nepdate.data.Settings

/**
 * Calls [onSettings] after every settings write. The observer is started once per app process, so
 * changing Language, Digits, Date follows or Show tithi updates open widgets immediately (MR-07
 * AC3). The first emission is the stored value, not a change, so it is skipped.
 */
internal class SettingsWidgetSync(
    private val settings: Flow<Settings>,
    private val onSettings: suspend (Settings) -> Unit,
) {
    fun start(scope: CoroutineScope) {
        scope.launch {
            settings.drop(1).distinctUntilChanged().collect { onSettings(it) }
        }
    }
}
