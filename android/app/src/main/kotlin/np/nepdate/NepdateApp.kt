package np.nepdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sanamhub.nepdate.KATHMANDU
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.data.SettingsRepository
import np.nepdate.data.TodayProvider
import np.nepdate.di.AppContainer
import np.nepdate.ui.NepdateDestination
import np.nepdate.ui.ShortNavigationBar
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.converter.ConverterScreen
import np.nepdate.ui.firstrun.FirstRunScreen
import np.nepdate.ui.month.MonthScreen
import np.nepdate.ui.settings.NepalTimeSuggestion
import np.nepdate.ui.settings.shouldSuggestNepalTime
import np.nepdate.ui.upcoming.UpcomingScreen
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The app shell: the three tabs and the shared today value. The settings screen and the first-launch
 * suggestion arrive in A1-06.
 */
@Composable
fun NepdateApp(
    container: AppContainer,
    modifier: Modifier = Modifier,
    initialDate: NepaliDate? = null,
    initialDateKey: Int = 0,
    onFirstGridDrawn: () -> Unit = {},
) {
    val repository = container.settingsRepository
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = Settings())
    val today = rememberToday(container.todayProvider, settings.dateFollows)
    val nepalTime = rememberNepalTime(container.todayProvider, settings)
    val scope = rememberCoroutineScope()
    val firstRunDone by remember(repository) { repository.settings.map { it.firstRunDone } }
        .collectAsStateWithLifecycle(initialValue = true)
    val navigation = rememberAppNavigation()

    LaunchedEffect(initialDateKey) {
        if (initialDate != null) navigation.showDate(initialDate)
    }
    BackHandler(enabled = navigation.overlay != Overlay.NONE) { navigation.back() }

    if (!firstRunDone) {
        FirstRunScreen(
            settings = settings,
            onLook = { scope.launch { repository.setLook(it) } },
            onDone = { scope.launch { repository.setFirstRunDone(true) } },
            modifier = modifier,
        )
        return
    }
    if (navigation.overlay != Overlay.NONE) {
        OverlayScreen(navigation, settings, today, repository, scope, modifier)
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        NepalTimeBanner(settings = settings, repository = repository, scope = scope)
        NepdateContent(
            settings = settings,
            today = today,
            nepalTime = nepalTime,
            initialDate = navigation.openDate,
            initialDateKey = navigation.openKey,
            onSettings = { navigation.overlay = Overlay.SETTINGS },
            onSearch = { navigation.overlay = Overlay.SEARCH },
            onOpenYear = { navigation.overlay = Overlay.YEAR },
            onShowInPatro = navigation::showDate,
            jumpToMonth = navigation.jumpMonth,
            jumpKey = navigation.jumpKey,
            destination = navigation.destination,
            onDestination = { navigation.destination = it },
            onFirstGridDrawn = onFirstGridDrawn,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Offers to follow Nepal time when the device is set to another zone and the offer is still open. */
@Composable
private fun NepalTimeBanner(
    settings: Settings,
    repository: SettingsRepository,
    scope: CoroutineScope,
) {
    if (!shouldSuggestNepalTime(settings, ZoneId.systemDefault())) return
    NepalTimeSuggestion(
        onAccept = {
            scope.launch {
                repository.setDateFollows(DateFollows.NEPAL)
                repository.setDateFollowsSuggestionHandled(true)
            }
        },
        onDismiss = {
            scope.launch { repository.setDateFollowsSuggestionHandled(true) }
        },
    )
}

/**
 * The current Nepal time shown in the month header when the setting follows Nepal and the device is
 * somewhere else (MR-06 AC2). Null when the two zones agree.
 */
@Composable
internal fun rememberNepalTime(
    todayProvider: TodayProvider,
    settings: Settings,
): String? {
    if (settings.dateFollows != DateFollows.NEPAL || ZoneId.systemDefault() == KATHMANDU) return null
    val lang = languageFor(LocalConfiguration.current.locales[0])
    var now by remember { mutableStateOf(todayProvider.now(DateFollows.NEPAL)) }
    LaunchedEffect(todayProvider) {
        while (true) {
            now = todayProvider.now(DateFollows.NEPAL)
            val nextMinute = now.plusMinutes(1).withSecond(0).withNano(0)
            delay(Duration.between(now, nextMinute).toMillis().coerceAtLeast(1))
        }
    }
    return now.format(TIME_OF_DAY).inDigits(settings.digits.resolve(lang))
}

/** The three tabs, without the settings store, so tests and previews can supply their own values. */
@Composable
internal fun NepdateContent(
    settings: Settings,
    today: NepaliDate,
    modifier: Modifier = Modifier,
    nepalTime: String? = null,
    initialDate: NepaliDate? = null,
    initialDateKey: Int = 0,
    onSettings: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onOpenYear: (() -> Unit)? = null,
    onShowInPatro: ((NepaliDate) -> Unit)? = null,
    jumpToMonth: Pair<Int, Int>? = null,
    jumpKey: Int = 0,
    destination: NepdateDestination? = null,
    onDestination: ((NepdateDestination) -> Unit)? = null,
    onFirstGridDrawn: () -> Unit = {},
) {
    var ownDestination by rememberSaveable { mutableStateOf(NepdateDestination.MONTH) }
    val current = destination ?: ownDestination
    val select: (NepdateDestination) -> Unit = { onDestination?.invoke(it) ?: run { ownDestination = it } }

    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (current) {
                NepdateDestination.MONTH ->
                    MonthScreen(
                        initialYear = today.year,
                        initialMonth = today.monthValue,
                        today = today,
                        settings = settings,
                        nepalTime = nepalTime,
                        openDate = initialDate,
                        openDateKey = initialDateKey,
                        onSettings = onSettings,
                        onSearch = onSearch,
                        onOpenYear = onOpenYear,
                        jumpToMonth = jumpToMonth,
                        jumpKey = jumpKey,
                        onFirstGridDrawn = onFirstGridDrawn,
                    )
                NepdateDestination.UPCOMING ->
                    UpcomingScreen(
                        today = today,
                        settings = settings,
                        onSettings = onSettings,
                    )
                NepdateDestination.CONVERTER ->
                    ConverterScreen(
                        today = today,
                        settings = settings,
                        onSettings = onSettings,
                        onShowInPatro = onShowInPatro,
                    )
            }
        }
        ShortNavigationBar(
            selected = current,
            onSelect = select,
        )
    }
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

private val TIME_OF_DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
