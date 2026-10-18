package np.nepdate.ui.month

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.github.sanamhub.nepdate.NepaliDate

/** Opens the day sheet and month page from a widget deep link (W-02 AC1, W-03 AC2). */
@Composable
internal fun OpenDateEffect(
    openDate: NepaliDate?,
    openDateKey: Int,
    pagerState: PagerState,
    onOpen: (NepaliDate) -> Unit,
) {
    LaunchedEffect(openDate, openDateKey) {
        openDate?.let { date ->
            val page = monthPage(date.year, date.monthValue)
            if (pagerState.currentPage != page) pagerState.scrollToPage(page)
            onOpen(date)
        }
    }
}
