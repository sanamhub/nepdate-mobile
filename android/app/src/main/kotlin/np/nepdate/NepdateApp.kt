package np.nepdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import np.nepdate.data.Settings
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
    val today = remember(settings.dateFollows) { container.todayProvider.today(settings.dateFollows) }

    MonthScreen(
        initialYear = today.year,
        initialMonth = today.monthValue,
        today = today,
        settings = settings,
        modifier = modifier,
    )
}
