package np.nepdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.delay
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
 * Today's BS date. It is read again when the app comes back to the foreground, at local midnight
 * while it stays in the foreground, and when the system reports a clock or time-zone change.
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
    LaunchedEffect(todayProvider, dateFollows) {
        while (true) {
            delay(todayProvider.millisUntilNextMidnight(dateFollows))
            today = todayProvider.today(dateFollows)
        }
    }
    val context = LocalContext.current
    LifecycleStartEffect(context, todayProvider, dateFollows) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    context: Context?,
                    intent: Intent?,
                ) {
                    today = todayProvider.today(dateFollows)
                }
            }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onStopOrDispose { context.unregisterReceiver(receiver) }
    }
    return today
}
