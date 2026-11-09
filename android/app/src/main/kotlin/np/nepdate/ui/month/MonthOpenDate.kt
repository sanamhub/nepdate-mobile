package np.nepdate.ui.month

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.launch
import np.nepdate.ui.common.rememberReduceMotion

/**
 * Opens the day sheet and month page for a pending request: a widget deep link (W-02 AC1, W-03
 * AC2), a search result or "Show in Patro". The request is reported handled once acted on, so the
 * sheet does not open again when the month tab leaves composition and comes back (a swipe or tap
 * back to Patro). Only an explicit action or a link opens it.
 */
@Composable
internal fun OpenDateEffect(
    openDate: NepaliDate?,
    openDateKey: Int,
    pagerState: PagerState,
    onHandled: () -> Unit = {},
    onOpen: (NepaliDate) -> Unit,
) {
    val handled by rememberUpdatedState(onHandled)
    val open by rememberUpdatedState(onOpen)
    LaunchedEffect(openDate, openDateKey) {
        val date = openDate ?: return@LaunchedEffect
        val page = monthPage(date.year, date.monthValue)
        if (pagerState.currentPage != page) pagerState.scrollToPage(page)
        open(date)
        // Last, because clearing the request changes this effect's keys and cancels it.
        handled()
    }
}

/** Shows a requested month (from the year view) once, then reports the request handled. */
@Composable
internal fun JumpEffect(
    jumpToMonth: Pair<Int, Int>?,
    jumpKey: Int,
    pagerState: PagerState,
    onHandled: () -> Unit,
) {
    val handled by rememberUpdatedState(onHandled)
    LaunchedEffect(jumpToMonth, jumpKey) {
        val (year, month) = jumpToMonth ?: return@LaunchedEffect
        pagerState.scrollToPage(monthPage(year, month))
        handled()
    }
}

/**
 * Moves the month pager to a page with the expressive spatial spring: Previous, Next, Today and the
 * picker. With animations off it jumps.
 */
@Composable
internal fun rememberMonthGoTo(pagerState: PagerState): (Int) -> Unit {
    val scope = rememberCoroutineScope()
    val spring = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val reduceMotion = rememberReduceMotion()
    return remember(pagerState, scope, spring, reduceMotion) {
        { page ->
            scope.launch {
                if (reduceMotion) {
                    pagerState.scrollToPage(page)
                } else {
                    pagerState.animateScrollToPage(page, animationSpec = spring)
                }
            }
        }
    }
}
