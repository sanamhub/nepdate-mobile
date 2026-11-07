package np.nepdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sanamhub.nepdate.KATHMANDU
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.data.SettingsRepository
import np.nepdate.data.StartTab
import np.nepdate.data.TodayProvider
import np.nepdate.di.AppContainer
import np.nepdate.ui.FloatingNavigationBar
import np.nepdate.ui.NepdateDestination
import np.nepdate.ui.common.LocalBottomBarPadding
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.rememberReduceMotion
import np.nepdate.ui.common.resolve
import np.nepdate.ui.converter.ConverterScreen
import np.nepdate.ui.firstrun.FirstRunScreen
import np.nepdate.ui.floatingBarPadding
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
    // Keeps each tab's saved state (the converter's dates, the shown month) while an overlay such
    // as Settings or Search replaces the tabs, so coming back shows them as they were.
    val tabsState = rememberSaveableStateHolder()

    LaunchedEffect(initialDateKey) {
        if (initialDate != null) navigation.showDate(initialDate)
    }
    StartTabEffect(repository, navigation, opensToDate = initialDate != null)
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
        tabsState.SaveableStateProvider(TABS_STATE_KEY) {
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
                onShowMonth = navigation::showMonth,
                jumpToMonth = navigation.jumpMonth,
                jumpKey = navigation.jumpKey,
                destination = navigation.destination,
                onDestination = { navigation.destination = it },
                onFirstGridDrawn = onFirstGridDrawn,
                onOpenDateHandled = navigation::consumeOpenDate,
                onJumpHandled = navigation::consumeJump,
                modifier = Modifier.weight(1f),
            )
        }
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
    onShowMonth: ((year: Int, month: Int) -> Unit)? = null,
    jumpToMonth: Pair<Int, Int>? = null,
    jumpKey: Int = 0,
    destination: NepdateDestination? = null,
    onDestination: ((NepdateDestination) -> Unit)? = null,
    onFirstGridDrawn: () -> Unit = {},
    onOpenDateHandled: () -> Unit = {},
    onJumpHandled: () -> Unit = {},
) {
    var ownDestination by rememberSaveable { mutableStateOf(NepdateDestination.MONTH) }
    val current = destination ?: ownDestination
    val select: (NepdateDestination) -> Unit = { onDestination?.invoke(it) ?: run { ownDestination = it } }
    val pager = rememberTabPager(current, select)
    val tabs = pager.state
    val args =
        TabArgs(
            settings = settings,
            today = today,
            nepalTime = nepalTime,
            openDate = initialDate,
            openDateKey = initialDateKey,
            onSettings = onSettings,
            onSearch = onSearch,
            onOpenYear = onOpenYear,
            onShowInPatro = onShowInPatro,
            onShowMonth = onShowMonth,
            jumpToMonth = jumpToMonth,
            jumpKey = jumpKey,
            onFirstGridDrawn = onFirstGridDrawn,
            onOpenDateHandled = onOpenDateHandled,
            onJumpHandled = onJumpHandled,
        )
    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBottomBarPadding provides floatingBarPadding()) {
            HorizontalPager(
                state = tabs,
                modifier = Modifier.fillMaxSize().testTag("tab_pager"),
                // Only the visible tab is composed; a neighbour composes when a swipe reveals it, and
                // each tab's saveable state (month page, list scroll) is kept by the pager.
                beyondViewportPageCount = 0,
                // No page connection: a horizontal child (the month pager, a converter row) hands
                // the drag it cannot use to the tabs, so a swipe at its end moves to the next tab.
                pageNestedScrollConnection = remember { object : NestedScrollConnection {} },
                key = { NepdateDestination.entries[it].name },
            ) { page -> TabPage(NepdateDestination.entries[page], args) }
        }
        FloatingNavigationBar(
            selected = { NepdateDestination.entries[tabs.targetPage] },
            position = { tabs.currentPage + tabs.currentPageOffsetFraction },
            onSelect = pager.show,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** What the tabs need from the shell, bundled so the pager page stays one small call. */
private class TabArgs(
    val settings: Settings,
    val today: NepaliDate,
    val nepalTime: String?,
    val openDate: NepaliDate?,
    val openDateKey: Int,
    val onSettings: (() -> Unit)?,
    val onSearch: (() -> Unit)?,
    val onOpenYear: (() -> Unit)?,
    val onShowInPatro: ((NepaliDate) -> Unit)?,
    val onShowMonth: ((year: Int, month: Int) -> Unit)?,
    val jumpToMonth: Pair<Int, Int>?,
    val jumpKey: Int,
    val onFirstGridDrawn: () -> Unit,
    val onOpenDateHandled: () -> Unit,
    val onJumpHandled: () -> Unit,
)

@Composable
private fun TabPage(
    tab: NepdateDestination,
    args: TabArgs,
) {
    when (tab) {
        NepdateDestination.MONTH ->
            MonthScreen(
                initialYear = args.today.year,
                initialMonth = args.today.monthValue,
                today = args.today,
                settings = args.settings,
                nepalTime = args.nepalTime,
                openDate = args.openDate,
                openDateKey = args.openDateKey,
                onSettings = args.onSettings,
                onSearch = args.onSearch,
                onOpenYear = args.onOpenYear,
                jumpToMonth = args.jumpToMonth,
                jumpKey = args.jumpKey,
                onFirstGridDrawn = args.onFirstGridDrawn,
                onOpenDateHandled = args.onOpenDateHandled,
                onJumpHandled = args.onJumpHandled,
            )
        NepdateDestination.UPCOMING ->
            UpcomingScreen(
                today = args.today,
                settings = args.settings,
                onSettings = args.onSettings,
                onOpenMonth = args.onShowMonth,
            )
        NepdateDestination.CONVERTER ->
            ConverterScreen(
                today = args.today,
                settings = args.settings,
                onSettings = args.onSettings,
                onShowInPatro = args.onShowInPatro,
            )
    }
}

/** The tab pager and the way a tap moves it. */
private class TabPager(
    val state: PagerState,
    val show: (NepdateDestination) -> Unit,
)

/**
 * The tab pager, kept in step with the selected tab both ways: a tap (or a deep link) scrolls the
 * pager with the expressive spatial spring, and the page it settles on, after a tap or a swipe,
 * becomes the selected tab.
 */
@Composable
private fun rememberTabPager(
    current: NepdateDestination,
    select: (NepdateDestination) -> Unit,
): TabPager {
    val state = rememberPagerState(initialPage = current.ordinal) { NepdateDestination.entries.size }
    val spring = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val reduceMotion = rememberReduceMotion()
    val scope = rememberCoroutineScope()
    val latestSelect by rememberUpdatedState(select)
    LaunchedEffect(current) {
        if (state.targetPage != current.ordinal) state.goTo(current.ordinal, spring, reduceMotion)
    }
    // The first settled page is only the initial page. Reporting it would overwrite a tab chosen
    // in the same frame, such as the "Opens on" setting read just after launch.
    LaunchedEffect(state) {
        snapshotFlow { state.settledPage }.drop(1).collect { latestSelect(NepdateDestination.entries[it]) }
    }
    return remember(state, scope, spring, reduceMotion) {
        TabPager(state) { tab -> scope.launch { state.goTo(tab.ordinal, spring, reduceMotion) } }
    }
}

private suspend fun PagerState.goTo(
    page: Int,
    spring: FiniteAnimationSpec<Float>,
    reduceMotion: Boolean,
) {
    if (reduceMotion) scrollToPage(page) else animateScrollToPage(page, animationSpec = spring)
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

/**
 * Opens the tab chosen in settings, once per cold start. It waits for the stored settings (the first
 * collected value is only the default) and stays out of the way when a widget opened a date.
 */
@Composable
private fun StartTabEffect(
    repository: SettingsRepository,
    navigation: AppNavigation,
    opensToDate: Boolean,
) {
    var applied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(repository) {
        if (applied) return@LaunchedEffect
        val startTab = repository.settings.first().startTab
        if (!opensToDate) navigation.destination = startTab.destination()
        applied = true
    }
}

private const val TABS_STATE_KEY = "tabs"

private fun StartTab.destination(): NepdateDestination =
    when (this) {
        StartTab.PATRO -> NepdateDestination.MONTH
        StartTab.FESTIVALS -> NepdateDestination.UPCOMING
        StartTab.CONVERTER -> NepdateDestination.CONVERTER
    }
