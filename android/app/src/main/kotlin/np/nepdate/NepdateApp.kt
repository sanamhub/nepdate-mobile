package np.nepdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.sanamhub.nepdate.KATHMANDU
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.nepdate.data.DateFollows
import np.nepdate.data.Settings
import np.nepdate.data.TodayProvider
import np.nepdate.di.AppContainer
import np.nepdate.ui.NepdateDestination
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.converter.ConverterScreen
import np.nepdate.ui.month.MonthScreen
import np.nepdate.ui.settings.NepalTimeSuggestion
import np.nepdate.ui.settings.SettingsScreen
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
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = Settings())
    val today = rememberToday(container.todayProvider, settings.dateFollows)
    val nepalTime = rememberNepalTime(container.todayProvider, settings)
    val scope = rememberCoroutineScope()
    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialDateKey) {
        if (initialDate != null) settingsOpen = false
    }

    if (settingsOpen) {
        SettingsScreen(
            settings = settings,
            appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            libraryVersion = BuildConfig.NEPDATE_VERSION,
            osVersion = Build.VERSION.RELEASE,
            onClose = { settingsOpen = false },
            onDateFollows = { scope.launch { container.settingsRepository.setDateFollows(it) } },
            onDigits = { scope.launch { container.settingsRepository.setDigits(it) } },
            onShowTithi = { scope.launch { container.settingsRepository.setShowTithi(it) } },
        )
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (shouldSuggestNepalTime(settings, ZoneId.systemDefault())) {
            NepalTimeSuggestion(
                onAccept = {
                    scope.launch {
                        container.settingsRepository.setDateFollows(DateFollows.NEPAL)
                        container.settingsRepository.setDateFollowsSuggestionHandled(true)
                    }
                },
                onDismiss = {
                    scope.launch { container.settingsRepository.setDateFollowsSuggestionHandled(true) }
                },
            )
        }
        NepdateContent(
            settings = settings,
            today = today,
            nepalTime = nepalTime,
            initialDate = initialDate,
            initialDateKey = initialDateKey,
            onSettings = { settingsOpen = true },
            onFirstGridDrawn = onFirstGridDrawn,
            modifier = Modifier.weight(1f),
        )
    }
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
    onFirstGridDrawn: () -> Unit = {},
) {
    var destination by rememberSaveable { mutableStateOf(NepdateDestination.MONTH) }

    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            when (destination) {
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
                    )
            }
        }
        ShortNavigationBar(
            selected = destination,
            onSelect = { destination = it },
        )
    }
}

/**
 * The Material 3 Expressive short navigation bar (64 dp): the selected tab shows its filled symbol
 * in a secondary-container pill and a bold label; the others show outlined symbols. One composable
 * for every tab, so the bar never drifts between screens.
 */
@Composable
internal fun ShortNavigationBar(
    selected: NepdateDestination,
    onSelect: (NepdateDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .height(64.dp)
                    .selectableGroup(),
        ) {
            NepdateDestination.entries.forEach { item ->
                val isSelected = item == selected
                val label = stringResource(item.label)
                val pill by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    label = "nav pill",
                )
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(
                                selected = isSelected,
                                onClick = { onSelect(item) },
                                role = Role.Tab,
                            ).testTag("nav_${item.name.lowercase()}")
                            .semantics { contentDescription = label },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(width = 56.dp, height = 32.dp)
                                .clip(CircleShape)
                                .background(pill),
                        contentAlignment = Alignment.Center,
                    ) {
                        SymbolIcon(
                            icon = if (isSelected) item.selectedIcon else item.icon,
                            tint =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        maxLines = 1,
                    )
                }
            }
        }
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
