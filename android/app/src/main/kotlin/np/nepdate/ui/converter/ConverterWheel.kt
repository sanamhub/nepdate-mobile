package np.nepdate.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toLocalDate
import io.github.sanamhub.nepdate.toNepaliDate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.month.PICKER_FIRST_YEAR
import np.nepdate.ui.month.PICKER_LAST_YEAR
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The year, month and day wheel in the calendar [direction] reads from. It only offers dates the
 * library covers, and a day that a shorter month lacks is clamped to the month's last day.
 */
@Composable
internal fun DateWheel(
    date: NepaliDate,
    direction: ConvertDirection,
    digits: Digits,
    onDateChange: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
    onTick: () -> Unit = {},
) {
    val columns =
        when (direction) {
            ConvertDirection.BS_TO_AD -> bsColumns(date, digits, onDateChange)
            ConvertDirection.AD_TO_BS -> adColumns(date, digits, onDateChange)
        }
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(WHEEL_ROW * WHEEL_ROWS)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(SELECTION_BAND)
                    .clip(RoundedCornerShape(SELECTION_BAND / 2))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
        )
        Row(Modifier.fillMaxSize()) {
            columns.forEach { column ->
                WheelColumn(
                    column = column,
                    onTick = onTick,
                    modifier =
                        Modifier
                            .weight(column.weight)
                            .fillMaxHeight(),
                )
            }
        }
    }
}

/** The same wheel in a dialog, for one end of the "between two dates" card. */
@Composable
internal fun DatePickDialog(
    title: String,
    initial: NepaliDate,
    direction: ConvertDirection,
    digits: Digits,
    onDismiss: () -> Unit,
    onConfirm: (NepaliDate) -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            DateWheel(
                date = draft,
                direction = direction,
                digits = digits,
                onDateChange = { draft = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(draft) },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.testTag("converter_pick_done"),
            ) {
                Text(stringResource(R.string.converter_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.month_picker_cancel))
            }
        },
    )
}

/** One wheel: its labels, the selected index, and what picking an index does. */
private class WheelSpec(
    val labels: List<String>,
    val selectedIndex: Int,
    val description: String,
    val tag: String,
    val weight: Float,
    val onSelect: (Int) -> Unit,
)

@Composable
private fun bsColumns(
    date: NepaliDate,
    digits: Digits,
    onDateChange: (NepaliDate) -> Unit,
): List<WheelSpec> {
    val yearLabels = remember(digits) { (PICKER_FIRST_YEAR..PICKER_LAST_YEAR).map { it.toString().inDigits(digits) } }
    val monthLabels = stringArrayResource(R.array.bs_month_long).toList()
    val dayCount = date.lengthOfMonth()
    val dayLabels = remember(dayCount, digits) { (1..dayCount).map { it.toString().inDigits(digits) } }
    val change: (Int, Int, Int) -> Unit = { year, month, day ->
        val length = NepaliDate.of(year, month, 1).lengthOfMonth()
        onDateChange(NepaliDate.of(year, month, day.coerceAtMost(length)))
    }
    return listOf(
        WheelSpec(
            labels = yearLabels,
            selectedIndex = date.year - PICKER_FIRST_YEAR,
            description = stringResource(R.string.month_picker_year),
            tag = "converter_wheel_year",
            weight = YEAR_WEIGHT,
            onSelect = { change(PICKER_FIRST_YEAR + it, date.monthValue, date.dayOfMonth) },
        ),
        WheelSpec(
            labels = monthLabels,
            selectedIndex = date.monthValue - 1,
            description = stringResource(R.string.month_picker_month),
            tag = "converter_wheel_month",
            weight = MONTH_WEIGHT,
            onSelect = { change(date.year, it + 1, date.dayOfMonth) },
        ),
        WheelSpec(
            labels = dayLabels,
            selectedIndex = date.dayOfMonth - 1,
            description = stringResource(R.string.converter_wheel_day),
            tag = "converter_wheel_day",
            weight = DAY_WEIGHT,
            onSelect = { change(date.year, date.monthValue, it + 1) },
        ),
    )
}

@Composable
private fun adColumns(
    date: NepaliDate,
    digits: Digits,
    onDateChange: (NepaliDate) -> Unit,
): List<WheelSpec> {
    val first = remember { NepaliDate.MIN.toLocalDate() }
    val last = remember { NepaliDate.MAX.toLocalDate() }
    val ad = date.toLocalDate()
    val years = first.year..last.year
    val months =
        (if (ad.year == first.year) first.monthValue else 1)..(if (ad.year == last.year) last.monthValue else 12)
    val month = YearMonth.of(ad.year, ad.monthValue)
    val days =
        (
            if (month ==
                YearMonth.from(
                    first,
                )
            ) {
                first.dayOfMonth
            } else {
                1
            }
        )..(if (month == YearMonth.from(last)) last.dayOfMonth else month.lengthOfMonth())
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val yearLabels = remember(digits) { years.map { it.toString().inDigits(digits) } }
    val monthLabels = months.map { adMonthNames[it - 1] }
    val dayLabels = remember(days, digits) { days.map { it.toString().inDigits(digits) } }
    val change: (Int, Int, Int) -> Unit = { year, monthValue, day ->
        val target = YearMonth.of(year, monthValue)
        val picked = LocalDate.of(year, monthValue, day.coerceAtMost(target.lengthOfMonth()))
        onDateChange(picked.coerceIn(first, last).toNepaliDate())
    }
    return listOf(
        WheelSpec(
            labels = yearLabels,
            selectedIndex = ad.year - years.first,
            description = stringResource(R.string.month_picker_year),
            tag = "converter_wheel_year",
            weight = YEAR_WEIGHT,
            onSelect = { change(years.first + it, ad.monthValue, ad.dayOfMonth) },
        ),
        WheelSpec(
            labels = monthLabels,
            selectedIndex = ad.monthValue - months.first,
            description = stringResource(R.string.month_picker_month),
            tag = "converter_wheel_month",
            weight = MONTH_WEIGHT,
            onSelect = { change(ad.year, months.first + it, ad.dayOfMonth) },
        ),
        WheelSpec(
            labels = dayLabels,
            selectedIndex = ad.dayOfMonth - days.first,
            description = stringResource(R.string.converter_wheel_day),
            tag = "converter_wheel_day",
            weight = DAY_WEIGHT,
            onSelect = { change(ad.year, ad.monthValue, days.first + it) },
        ),
    )
}

/**
 * A snapping wheel of five visible rows. The centred row is the value; it is reported while the
 * user scrolls, so the result follows the finger. TalkBack reads it as an adjustable value: swipe
 * up or down (or the custom actions) steps it, and its state is the selected label.
 */
@Composable
private fun WheelColumn(
    column: WheelSpec,
    onTick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = column.selectedIndex.coerceIn(0, column.labels.lastIndex)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val fling = rememberSnapFlingBehavior(listState, SnapPosition.Center)
    val scope = rememberCoroutineScope()
    val onSelect by rememberUpdatedState(column.onSelect)
    val tick by rememberUpdatedState(onTick)
    val currentSelected by rememberUpdatedState(selected)
    val centred by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val middle = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - middle) }?.index
                ?: listState.firstVisibleItemIndex
        }
    }
    // The finger moves the value: report each new centred row while the list is being scrolled.
    LaunchedEffect(listState) {
        snapshotFlow { centred }
            .distinctUntilChanged()
            .filter { listState.isScrollInProgress && it != currentSelected }
            .collect {
                tick()
                onSelect(it)
            }
    }
    // The value moves the wheel: follow a change from outside (clamped day, TalkBack, a new year).
    LaunchedEffect(selected, column.labels) {
        if (!listState.isScrollInProgress && centred != selected) listState.scrollToItem(selected)
    }
    val nextLabel = stringResource(R.string.converter_wheel_next)
    val previousLabel = stringResource(R.string.converter_wheel_previous)
    LazyColumn(
        state = listState,
        flingBehavior = fling,
        contentPadding = PaddingValues(vertical = WHEEL_ROW * (WHEEL_ROWS / 2)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.wheelSemantics(column, selected, nextLabel, previousLabel),
    ) {
        items(count = column.labels.size) { index ->
            WheelRow(
                label = column.labels[index],
                distance = abs(index - centred),
                onClick = {
                    column.onSelect(index)
                    scope.launch { listState.animateScrollToItem(index) }
                },
            )
        }
    }
}

/** Presents a wheel to TalkBack as one adjustable value with next and previous actions. */
private fun Modifier.wheelSemantics(
    column: WheelSpec,
    selected: Int,
    nextLabel: String,
    previousLabel: String,
): Modifier {
    val lastIndex = column.labels.lastIndex
    return clearAndSetSemantics {
        testTag = column.tag
        contentDescription = column.description
        stateDescription = column.labels[selected]
        progressBarRangeInfo =
            ProgressBarRangeInfo(
                current = selected.toFloat(),
                range = 0f..lastIndex.coerceAtLeast(1).toFloat(),
                steps = (lastIndex - 1).coerceAtLeast(0),
            )
        setProgress { target ->
            val index = target.roundToInt().coerceIn(0, lastIndex)
            if (index != selected) column.onSelect(index)
            true
        }
        customActions =
            listOf(
                CustomAccessibilityAction(nextLabel) {
                    if (selected < lastIndex) column.onSelect(selected + 1)
                    selected < lastIndex
                },
                CustomAccessibilityAction(previousLabel) {
                    if (selected > 0) column.onSelect(selected - 1)
                    selected > 0
                },
            )
    }
}

@Composable
private fun WheelRow(
    label: String,
    distance: Int,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(WHEEL_ROW)
                .clickable(interactionSource = null, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color =
                when (distance) {
                    0 -> scheme.onSecondaryContainer
                    1 -> scheme.onSurfaceVariant
                    else -> scheme.onSurfaceVariant.copy(alpha = FAR_ROW_ALPHA)
                },
            fontSize =
                when (distance) {
                    0 -> 20.sp
                    1 -> 16.sp
                    else -> 14.sp
                },
            fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val WHEEL_ROW: Dp = 42.dp
private const val WHEEL_ROWS = 5
private val SELECTION_BAND: Dp = 44.dp
private const val FAR_ROW_ALPHA = 0.55f
private const val YEAR_WEIGHT = 1.1f
private const val MONTH_WEIGHT = 1.3f
private const val DAY_WEIGHT = 0.8f
