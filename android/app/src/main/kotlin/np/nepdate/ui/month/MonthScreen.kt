package np.nepdate.ui.month

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.LocalBottomBarPadding
import np.nepdate.ui.common.languageFor
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
    jumpToMonth: Pair<Int, Int>? = null,
    jumpKey: Int = 0,
    onFirstGridDrawn: () -> Unit = {},
    onOpenDateHandled: () -> Unit = {},
    onJumpHandled: () -> Unit = {},
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val displaySettings = settings.copy(digits = settings.digits.resolve(lang))
    val pagerState = rememberPagerState(initialPage = monthPage(initialYear, initialMonth)) { MONTH_PAGE_COUNT }
    val currentPage = pagerState.settledPage
    val currentLayout =
        remember(currentPage, displaySettings, today) {
            monthLayout(yearForPage(currentPage), monthForPage(currentPage), displaySettings, today)
        }
    val goTo = rememberMonthGoTo(pagerState)
    var showPicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<NepaliDate?>(null) }
    OpenDateEffect(openDate, openDateKey, pagerState, onHandled = onOpenDateHandled) { selectedDate = it }
    JumpEffect(jumpToMonth, jumpKey, pagerState, onHandled = onJumpHandled)
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
        MonthBody(
            pagerState = pagerState,
            layout = currentLayout,
            settings = displaySettings,
            today = today,
            lang = lang,
            nepalTime = nepalTime,
            selectedDate = selectedDate,
            onDayClick = { selectedDate = it },
            onFirstGridDrawn = onFirstGridDrawn,
            goTo = goTo,
        )
    }

    MonthOverlays(
        today = today,
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

/**
 * Everything below the top bar: today's hero (on the current month, when the grid fits) and the
 * month card with its header, weekday row and pager.
 */
@Composable
private fun ColumnScope.MonthBody(
    pagerState: PagerState,
    layout: MonthLayout,
    settings: Settings,
    today: NepaliDate,
    lang: Lang,
    nepalTime: String?,
    selectedDate: NepaliDate?,
    onDayClick: (NepaliDate) -> Unit,
    onFirstGridDrawn: () -> Unit,
    goTo: (Int) -> Unit,
) {
    val currentPage = pagerState.settledPage
    val todayPage = monthPage(today.year, today.monthValue)
    val bigFont = LocalDensity.current.fontScale >= LIST_FONT_SCALE
    val density = LocalDensity.current
    val bottomBar = LocalBottomBarPadding.current.calculateBottomPadding()
    // A week row is as tall as its numeral, marks, AD day and tithi need, and no taller: stretching
    // rows to fill the screen left the numbers floating in tall empty cells.
    val rowHeight = (if (settings.showTithi) ROW_TITHI else ROW).plus(if (settings.showAdDates) AD_ROW else 0.dp)
    var heroHeight by remember { mutableStateOf(0.dp) }
    BoxWithConstraints(modifier = Modifier.weight(1f)) {
        // The card reaches the bottom of the screen, behind the floating bar, even when its content
        // is shorter; the large-text list keeps the fixed height it scrolls inside.
        val fill = maxHeight - heroHeight
        val cardHeight = if (bigFont) maxOf(fill, LIST_CARD_MIN + bottomBar) else null
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.onSizeChanged { heroHeight = with(density) { it.height.toDp() } }) {
                TodayHeroSlot(
                    visible = currentPage == todayPage && !bigFont,
                    today = today,
                    lang = lang,
                    digits = settings.digits,
                    onOpenDay = onDayClick,
                )
            }
            MonthCard(
                pagerState,
                layout,
                settings,
                today,
                lang,
                nepalTime,
                selectedDate,
                onDayClick,
                onFirstGridDrawn,
                goTo,
                rowHeight = rowHeight,
                height = cardHeight,
                minHeight = fill,
            )
        }
    }
}

/** The month card: its header, the weekday row and the month pager, at [height]. */
@Composable
private fun MonthCard(
    pagerState: PagerState,
    layout: MonthLayout,
    settings: Settings,
    today: NepaliDate,
    lang: Lang,
    nepalTime: String?,
    selectedDate: NepaliDate?,
    onDayClick: (NepaliDate) -> Unit,
    onFirstGridDrawn: () -> Unit,
    goTo: (Int) -> Unit,
    rowHeight: Dp,
    height: Dp?,
    minHeight: Dp,
) {
    val currentPage = pagerState.settledPage
    val todayPage = monthPage(today.year, today.monthValue)
    Column(
        modifier =
            Modifier
                .then(if (height != null) Modifier.height(height) else Modifier.heightIn(min = minHeight))
                .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(top = 6.dp),
    ) {
        MonthHeader(
            layout = layout,
            digits = settings.digits,
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
            settings = settings,
            today = today,
            lang = lang,
            onDayClick = onDayClick,
            onFirstGridDrawn = onFirstGridDrawn,
            rowHeight = rowHeight,
            modifier =
                if (height != null) {
                    Modifier.weight(1f)
                } else {
                    Modifier.height(gridHeight(pagerState, layout, settings, today, rowHeight))
                },
        )
        if (height == null) {
            Crossfade(
                targetState = layout,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "month highlights",
            ) { shown ->
                MonthHighlights(
                    layout = shown,
                    lang = lang,
                    digits = settings.digits,
                    onDayClick = onDayClick,
                    modifier =
                        Modifier.padding(
                            bottom = LocalBottomBarPadding.current.calculateBottomPadding() + 16.dp,
                        ),
                )
            }
        }
    }
}

/**
 * The pager's height: the rows of the settled month, or of the month being swiped to when it has
 * more, so a six-row month never shows its last row cut off mid-swipe. It animates between the two.
 */
@Composable
private fun gridHeight(
    pagerState: PagerState,
    layout: MonthLayout,
    settings: Settings,
    today: NepaliDate,
    rowHeight: Dp,
): Dp {
    val target = pagerState.targetPage
    val targetRows =
        remember(target, settings, today) {
            monthLayout(yearForPage(target), monthForPage(target), settings, today).rowCount
        }
    val rows = maxOf(layout.rowCount, targetRows)
    val height by animateDpAsState(
        targetValue = rowHeight * rows + ROW_GAP * (rows - 1),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "grid height",
    )
    return height
}

@Composable
private fun MonthOverlays(
    today: NepaliDate,
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
            today = today,
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
    rowHeight: Dp,
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
                rowHeight = rowHeight,
                onFirstGridDrawn = if (page == pagerState.currentPage) onFirstGridDrawn else ({}),
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

private val LIST_CARD_MIN = 320.dp
private val ROW = 52.dp
private val ROW_TITHI = 76.dp

private const val SATURDAY_INDEX = 6
