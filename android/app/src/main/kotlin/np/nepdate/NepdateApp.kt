package np.nepdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider
import np.nepdate.di.AppContainer
import np.nepdate.ui.month.MonthScreen

/**
 * The app shell. A1 starts on the current BS month; the settings screen and the other tabs arrive in
 * A1-05 and A1-06.
 */
@Composable
fun NepdateApp(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = Settings())
    val today = rememberToday(container.todayProvider, settings.dateFollows)

    MonthScreen(
        initialYear = today.year,
        initialMonth = today.monthValue,
        today = today,
        settings = settings,
        modifier = modifier,
    )
}

/**
 * Today's BS date, read again every time the app comes back to the foreground, so an app left open
 * overnight does not keep yesterday's today ring (MR-06 AC2).
 */
@Composable
internal fun rememberToday(
    todayProvider: TodayProvider,
    dateFollows: DateFollows,
): NepaliDate {
    var today by remember(dateFollows) { mutableStateOf(todayProvider.today(dateFollows)) }
    LifecycleResumeEffect(todayProvider, dateFollows) {
        today = todayProvider.today(dateFollows)
        onPauseOrDispose {}
    }
    return today
}
