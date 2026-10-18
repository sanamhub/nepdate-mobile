package np.nepdate.ui.month

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.coroutines.launch
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.AdSpan
import np.nepdate.ui.common.adSpan
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.common.weekdayHeaders
import np.nepdate.ui.day.DayDetailSheet
import np.nepdate.ui.theme.LocalNepdateColors

/** Above this font scale seven columns no longer fit, so the grid becomes a list (MR-10 AC2). */
internal const val LIST_FONT_SCALE = 1.5f

/**
 * The month view (MR-01): a swipeable grid with a month picker, previous/next buttons and a Today
 * button that appears only off the current month.
 *
 * @param initialYear the BS year to open on
 * @param initialMonth the BS month to open on, 1..12
 * @param today the day that gets the today ring
 * @param settings the digits and tithi choices
 */
@Composable
fun MonthScreen(
    initialYear: Int,
    initialMonth: Int,
    today: NepaliDate,
    settings: Settings,
    modifier: Modifier = Modifier,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val pagerState = rememberPagerState(initialPage = monthPage(initialYear, initialMonth)) { MONTH_PAGE_COUNT }
    val currentPage = pagerState.settledPage
    val currentLayout =
        remember(currentPage, settings, today) {
            monthLayout(yearForPage(currentPage), monthForPage(currentPage), settings, today)
        }
    val todayPage = monthPage(today.year, today.monthValue)
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<NepaliDate?>(null) }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        MonthHeader(
            layout = currentLayout,
            digits = settings.digits,
            showToday = currentPage != todayPage,
            canGoPrevious = currentPage > 0,
            canGoNext = currentPage < MONTH_PAGE_COUNT - 1,
            onChoose = { showPicker = true },
            onPrevious = {
                scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
            },
            onNext = {
                scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
            },
            onToday = {
                scope.launch { pagerState.animateScrollToPage(todayPage) }
            },
        )
        WeekdayHeader(lang = lang)
        MonthPager(
            pagerState = pagerState,
            selectedDate = selectedDate,
            settings = settings,
            today = today,
            lang = lang,
            onDayClick = { selectedDate = it },
            modifier = Modifier.weight(1f),
        )
    }

    MonthOverlays(
        selectedDate = selectedDate,
        settings = settings,
        showPicker = showPicker,
        currentLayout = currentLayout,
        lang = lang,
        onSheetDismiss = { selectedDate = null },
        onPickerDismiss = { showPicker = false },
        onPickerConfirm = { year, month ->
            showPicker = false
            scope.launch { pagerState.animateScrollToPage(monthPage(year, month)) }
        },
    )
}

@Composable
private fun MonthOverlays(
    selectedDate: NepaliDate?,
    settings: Settings,
    showPicker: Boolean,
    currentLayout: MonthLayout,
    lang: Lang,
    onSheetDismiss: () -> Unit,
    onPickerDismiss: () -> Unit,
    onPickerConfirm: (year: Int, month: Int) -> Unit,
) {
    BackHandler(enabled = showPicker) { onPickerDismiss() }
    selectedDate?.let { date ->
        DayDetailSheet(
            date = date,
            settings = settings,
            onDismiss = onSheetDismiss,
        )
    }
    if (showPicker) {
        MonthYearPicker(
            initialYear = currentLayout.year,
            initialMonth = currentLayout.month.value,
            lang = lang,
            onDismiss = onPickerDismiss,
            onConfirm = onPickerConfirm,
        )
    }
}

@Composable
private fun MonthPager(
    pagerState: PagerState,
    selectedDate: NepaliDate?,
    settings: Settings,
    today: NepaliDate,
    lang: Lang,
    onDayClick: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(R.array.ad_month_long).toList()
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val publicHoliday = stringResource(R.string.public_holiday_label)
    val listLayout = LocalDensity.current.fontScale >= LIST_FONT_SCALE
    HorizontalPager(
        state = pagerState,
        modifier =
            modifier
                .testTag("month_pager"),
        beyondViewportPageCount = 1,
    ) { page ->
        val pageLayout =
            remember(page, settings, today) {
                monthLayout(yearForPage(page), monthForPage(page), settings, today)
            }
        if (listLayout) {
            MonthList(
                layout = pageLayout,
                selectedDate = selectedDate,
                settings = settings,
                lang = lang,
                bsMonthNames = bsMonthNames,
                monthNames = monthNames,
                publicHoliday = publicHoliday,
                onDayClick = onDayClick,
            )
        } else {
            MonthGrid(
                layout = pageLayout,
                selectedDate = selectedDate,
                settings = settings,
                lang = lang,
                bsMonthNames = bsMonthNames,
                monthNames = monthNames,
                publicHoliday = publicHoliday,
                onDayClick = onDayClick,
            )
        }
    }
}

@Composable
private fun MonthHeader(
    layout: MonthLayout,
    digits: Digits,
    showToday: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onChoose: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    val colors = LocalNepdateColors.current
    val monthShortNames = stringArrayResource(R.array.ad_month_short).toList()
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val chooseLabel = stringResource(R.string.month_choose)
    val span = adSpan(layout.firstDay, layout.lastDay, monthShortNames, digits)
    val spanText = adSpanText(span)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clickable(onClick = onChoose)
                    .semantics { contentDescription = chooseLabel }
                    .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = monthTitle(bsMonthNames[layout.month.value - 1], layout.year, digits),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = spanText,
                color = colors.dimmed,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        MonthNavigation(
            showToday = showToday,
            canGoPrevious = canGoPrevious,
            canGoNext = canGoNext,
            onPrevious = onPrevious,
            onNext = onNext,
            onToday = onToday,
        )
    }
}

@Composable
private fun adSpanText(span: AdSpan): String =
    when {
        span.isSameMonth ->
            stringResource(R.string.month_ad_span_same_month, span.startMonth, span.startYear)
        span.isSameYear ->
            stringResource(
                R.string.month_ad_span_same_year,
                span.startMonth,
                span.endMonth,
                span.startYear,
            )
        else ->
            stringResource(
                R.string.month_ad_span_cross_year,
                span.startMonth,
                span.startYear,
                span.endMonth,
                span.endYear,
            )
    }

@Composable
private fun MonthNavigation(
    showToday: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    val colors = LocalNepdateColors.current
    val previousLabel = stringResource(R.string.month_previous)
    val nextLabel = stringResource(R.string.month_next)
    val todayLabel = stringResource(R.string.month_today)
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onPrevious,
            enabled = canGoPrevious,
            modifier =
                Modifier
                    .size(48.dp)
                    .semantics { contentDescription = previousLabel },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (showToday) {
            TextButton(
                onClick = onToday,
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text(text = todayLabel, color = colors.today)
            }
        }
        IconButton(
            onClick = onNext,
            enabled = canGoNext,
            modifier =
                Modifier
                    .size(48.dp)
                    .semantics { contentDescription = nextLabel },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun WeekdayHeader(lang: Lang) {
    if (LocalDensity.current.fontScale >= LIST_FONT_SCALE) return
    val colors = LocalNepdateColors.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        weekdayHeaders(lang).forEachIndexed { index, name ->
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                color = if (index == SATURDAY_INDEX) colors.holiday else colors.dimmed,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/** The pager page for a BS year and month. */
internal fun monthPage(
    year: Int,
    month: Int,
): Int {
    val page = (year - PICKER_FIRST_YEAR) * MONTHS_PER_YEAR + month - 1
    return page.coerceIn(0, MONTH_PAGE_COUNT - 1)
}

/** The BS year of a pager page. */
internal fun yearForPage(page: Int): Int = PICKER_FIRST_YEAR + page / MONTHS_PER_YEAR

/** The BS month of a pager page, 1..12. */
internal fun monthForPage(page: Int): Int = page % MONTHS_PER_YEAR + 1

private const val SATURDAY_INDEX = 6
