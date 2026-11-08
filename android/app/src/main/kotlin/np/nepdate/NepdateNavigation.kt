package np.nepdate

import android.os.Build
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import np.nepdate.data.DateNotice
import np.nepdate.data.Settings
import np.nepdate.data.SettingsRepository
import np.nepdate.ui.NepdateDestination
import np.nepdate.ui.about.AboutScreen
import np.nepdate.ui.search.SearchScreen
import np.nepdate.ui.settings.SettingsScreen
import np.nepdate.ui.year.YearScreen

/** Full-screen destinations above the tabs. */
internal enum class Overlay { NONE, SETTINGS, ABOUT, SEARCH, YEAR }

/**
 * Where the app shell is: the open overlay, the selected tab, and the pending requests to open a
 * day or jump to a month. The overlay and the tab survive process death. A request is consumed
 * once the month tab has acted on it, so it never replays.
 */
@Stable
internal class AppNavigation(
    overlayState: MutableState<Overlay>,
    destinationState: MutableState<NepdateDestination>,
) {
    var overlay by overlayState
    var destination by destinationState
    var openDate by mutableStateOf<NepaliDate?>(null)
        private set
    var openKey by mutableIntStateOf(0)
        private set
    var jumpMonth by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    var jumpKey by mutableIntStateOf(0)
        private set

    /** Closes any overlay and opens [date] on the month tab. */
    fun showDate(date: NepaliDate) {
        overlay = Overlay.NONE
        destination = NepdateDestination.MONTH
        openDate = date
        openKey++
    }

    /** Closes any overlay and shows the given month on the month tab. */
    fun showMonth(
        year: Int,
        month: Int,
    ) {
        overlay = Overlay.NONE
        destination = NepdateDestination.MONTH
        jumpMonth = year to month
        jumpKey++
    }

    /**
     * Marks the pending open-day request as handled, so the day sheet does not open again when the
     * month tab comes back into composition (a swipe or tap back to Patro).
     */
    fun consumeOpenDate() {
        openDate = null
    }

    /** Marks the pending month jump as handled, for the same reason as [consumeOpenDate]. */
    fun consumeJump() {
        jumpMonth = null
    }

    /** The system back gesture: About returns to Settings, every other overlay closes. */
    fun back() {
        overlay = if (overlay == Overlay.ABOUT) Overlay.SETTINGS else Overlay.NONE
    }
}

@Composable
internal fun rememberAppNavigation(): AppNavigation {
    val overlay = rememberSaveable { mutableStateOf(Overlay.NONE) }
    val destination = rememberSaveable { mutableStateOf(NepdateDestination.MONTH) }
    return remember(overlay, destination) { AppNavigation(overlay, destination) }
}

/** Shows the open full-screen overlay. Draws nothing when no overlay is open. */
@Composable
internal fun OverlayScreen(
    navigation: AppNavigation,
    settings: Settings,
    today: NepaliDate,
    repository: SettingsRepository,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    when (navigation.overlay) {
        Overlay.SETTINGS ->
            SettingsScreen(
                settings = settings,
                appVersion = appVersion,
                libraryVersion = BuildConfig.NEPDATE_VERSION,
                osVersion = Build.VERSION.RELEASE,
                onClose = { navigation.overlay = Overlay.NONE },
                onDateFollows = { scope.launch { repository.setDateFollows(it) } },
                onDigits = { scope.launch { repository.setDigits(it) } },
                onShowTithi = { scope.launch { repository.setShowTithi(it) } },
                onLook = { scope.launch { repository.setLook(it) } },
                onWallpaperColors = { scope.launch { repository.setWallpaperColors(it) } },
                onShowAdDates = { scope.launch { repository.setShowAdDates(it) } },
                onHaptics = { scope.launch { repository.setHaptics(it) } },
                onStatusBarDate = { scope.launch { repository.setDateNotice(DateNotice.STATUS_BAR, it) } },
                onNotificationDate = { scope.launch { repository.setDateNotice(DateNotice.CARD, it) } },
                onStartTab = { scope.launch { repository.setStartTab(it) } },
                onOpenAbout = { navigation.overlay = Overlay.ABOUT },
            )
        Overlay.ABOUT ->
            AboutScreen(
                appVersion = appVersion,
                libraryVersion = BuildConfig.NEPDATE_VERSION,
                osVersion = Build.VERSION.RELEASE,
                onBack = { navigation.overlay = Overlay.SETTINGS },
                digits = settings.digits,
            )
        Overlay.SEARCH ->
            SearchScreen(
                today = today,
                settings = settings,
                onBack = { navigation.overlay = Overlay.NONE },
                onOpenDate = navigation::showDate,
                recentStore = repository.recentSearches,
                modifier = modifier,
            )
        Overlay.YEAR ->
            YearScreen(
                initialYear = today.year,
                today = today,
                settings = settings,
                onBack = { navigation.overlay = Overlay.NONE },
                onOpenMonth = navigation::showMonth,
                modifier = modifier.navigationBarsPadding(),
            )
        Overlay.NONE -> Unit
    }
}
