package np.nepdate.ui.month

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.groupShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.weekdayHeaders
import np.nepdate.ui.day.DayDetailSheet
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The font scale at which the month switches from the grid to the list. At 360x640 dp with a
 * six-row month and tithi on, 1.15 fits the grid; 1.3 clips the second tithi line, so the list
 * starts there (MR-10 AC2).
 */
internal const val LIST_FONT_SCALE = 1.3f

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
    nepalTime: String? = null,
    openDate: NepaliDate? = null,
    openDateKey: Int = 0,
    onSettings: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onOpenYear: (() -> Unit)? = null,
    onFirstGridDrawn: () -> Unit = {},
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val displaySettings = settings.copy(digits = settings.digits.resolve(lang))
    val pagerState = rememberPagerState(initialPage = monthPage(initialYear, initialMonth)) { MONTH_PAGE_COUNT }
    val currentPage = pagerState.settledPage
    val currentLayout =
        remember(currentPage, displaySettings, today) {
            monthLayout(yearForPage(currentPage), monthForPage(currentPage), displaySettings, today)
        }
    val todayPage = monthPage(today.year, today.monthValue)
    val scope = rememberCoroutineScope()
    val goTo: (Int) -> Unit = { page -> scope.launch { pagerState.animateScrollToPage(page) } }
    var showPicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<NepaliDate?>(null) }
    OpenDateEffect(openDate, openDateKey, pagerState) { selectedDate = it }
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                // The picker draws over this screen in the same window, so TalkBack and Switch
                // Access must not reach the month behind it while it is open.
                .then(if (showPicker) Modifier.clearAndSetSemantics {} else Modifier),
    ) {
        MonthTopBar(
            layout = currentLayout,
            digits = displaySettings.digits,
            onChoose = onOpenYear ?: { showPicker = true },
            onSearch = onSearch,
            onSettings = onSettings,
        )
        val bigFont = LocalDensity.current.fontScale >= LIST_FONT_SCALE
        if (currentPage == todayPage && !bigFont) {
            TodayHero(
                today = today,
                lang = lang,
                digits = displaySettings.digits,
                onOpenDay = { selectedDate = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(top = 4.dp),
        ) {
            MonthHeader(
                layout = currentLayout,
                digits = displaySettings.digits,
                nepalTime = nepalTime,
                showToday = currentPage != todayPage,
                canGoPrevious = currentPage > 0,
                canGoNext = currentPage < MONTH_PAGE_COUNT - 1,
                onPrevious = { goTo(currentPage - 1) },
                onNext = { goTo(currentPage + 1) },
                onToday = { goTo(todayPage) },
            )
            WeekdayHeader(lang = lang)
            MonthPager(
                pagerState = pagerState,
                selectedDate = selectedDate,
                settings = displaySettings,
                today = today,
                lang = lang,
                onDayClick = { selectedDate = it },
                onFirstGridDrawn = onFirstGridDrawn,
                modifier = Modifier.weight(1f),
            )
        }
    }

    MonthOverlays(
        selectedDate = selectedDate,
        settings = displaySettings,
        showPicker = showPicker,
        currentLayout = currentLayout,
        onSheetDismiss = { selectedDate = null },
        onPickerDismiss = { showPicker = false },
        onPickerConfirm = { year, month ->
            showPicker = false
            goTo(monthPage(year, month))
        },
    )
}

@Composable
private fun MonthOverlays(
    selectedDate: NepaliDate?,
    settings: Settings,
    showPicker: Boolean,
    currentLayout: MonthLayout,
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
            monthNames = stringArrayResource(R.array.bs_month_long).toList(),
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
    onFirstGridDrawn: () -> Unit,
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
                onFirstGridDrawn = if (page == pagerState.currentPage) onFirstGridDrawn else ({}),
            )
        }
    }
}

/** The top bar: the month title opens the year view; search and settings on the right. */
@Composable
private fun MonthTopBar(
    layout: MonthLayout,
    digits: Digits,
    onChoose: () -> Unit,
    onSearch: (() -> Unit)?,
    onSettings: (() -> Unit)?,
) {
    val chooseLabel = stringResource(R.string.month_choose)
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onChoose)
                    .semantics { contentDescription = chooseLabel }
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = monthTitle(bsMonthNames[layout.month.value - 1], layout.year, digits),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            SymbolIcon(icon = R.drawable.ic_sym_arrow_drop_down, tint = MaterialTheme.colorScheme.onSurface)
        }
        if (onSearch != null) {
            val searchLabel = stringResource(R.string.home_search)
            IconButton(onClick = onSearch, modifier = Modifier.semantics { contentDescription = searchLabel }) {
                SymbolIcon(icon = R.drawable.ic_sym_search, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onSettings != null) {
            val settingsLabel = stringResource(R.string.settings_title)
            IconButton(onClick = onSettings, modifier = Modifier.semantics { contentDescription = settingsLabel }) {
                SymbolIcon(icon = R.drawable.ic_sym_settings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** The month card header: BS month, AD span, and the connected previous, today and next group. */
@Composable
private fun MonthHeader(
    layout: MonthLayout,
    digits: Digits,
    nepalTime: String?,
    showToday: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    val colors = LocalNepdateColors.current
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val spanText =
        adSpanText(
            adSpan(
                layout.firstDay,
                layout.lastDay,
                stringArrayResource(R.array.ad_month_short).toList(),
                digits,
            ),
        )
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bsMonthNames[layout.month.value - 1],
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            Text(
                text = spanText,
                color = colors.dimmed,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (nepalTime != null) {
                Text(
                    text = stringResource(R.string.month_nepal_time, nepalTime),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
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

/** Previous, Today (only off the current month) and Next as one connected button group. */
@Composable
private fun MonthNavigation(
    showToday: Boolean,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    val previousLabel = stringResource(R.string.month_previous)
    val nextLabel = stringResource(R.string.month_next)
    val todayLabel = stringResource(R.string.month_today)
    val tonal = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
        GroupButton(
            shape = groupShape(GroupPosition.START),
            color = tonal,
            enabled = canGoPrevious,
            label = previousLabel,
            onClick = onPrevious,
        ) { SymbolIcon(icon = R.drawable.ic_sym_chevron_left, size = 22.dp) }
        if (showToday) {
            GroupButton(
                shape = groupShape(GroupPosition.MIDDLE),
                color = MaterialTheme.colorScheme.primary,
                enabled = true,
                label = todayLabel,
                onClick = onToday,
                wide = true,
            ) {
                Text(
                    text = todayLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        GroupButton(
            shape = groupShape(GroupPosition.END),
            color = tonal,
            enabled = canGoNext,
            label = nextLabel,
            onClick = onNext,
        ) { SymbolIcon(icon = R.drawable.ic_sym_chevron_right, size = 22.dp) }
    }
}

@Composable
private fun GroupButton(
    shape: Shape,
    color: Color,
    enabled: Boolean,
    label: String,
    onClick: () -> Unit,
    wide: Boolean = false,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .then(if (wide) Modifier.heightIn(min = 40.dp) else Modifier.size(width = 48.dp, height = 40.dp))
                .clip(shape)
                .background(if (enabled) color else color.copy(alpha = DISABLED_ALPHA))
                .clickable(enabled = enabled, onClick = onClick)
                .semantics { contentDescription = label }
                .then(if (wide) Modifier.padding(horizontal = 14.dp, vertical = 10.dp) else Modifier),
        contentAlignment = Alignment.Center,
    ) { content() }
}

private const val DISABLED_ALPHA = 0.38f

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
