package np.nepdate.ui.day

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.CalendarInfo
import io.github.sanamhub.nepdate.patro.Patro
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.CookieShape
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SunnyShape
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.bsLongNepali
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.settings.rememberHaptics
import np.nepdate.ui.theme.LocalNepdateColors

/**
 * The modal day sheet from MR-02. It is as tall as its content. When [today] is known the sheet
 * marks today and shows how far the date is from it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(
    date: NepaliDate,
    settings: Settings,
    onDismiss: () -> Unit,
    today: NepaliDate? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        DayDetailContent(date = date, settings = settings, onClose = onDismiss, today = today)
    }
}

/**
 * The content of the day sheet. It is separate from [DayDetailSheet] so screenshots and UI tests
 * can render it without a dialog window. The previous and next day buttons move within the sheet;
 * [onClose] is offered to accessibility services as the dismiss action.
 */
@Composable
fun DayDetailContent(
    date: NepaliDate,
    settings: Settings,
    onClose: () -> Unit,
    today: NepaliDate? = null,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val displaySettings = settings.copy(digits = settings.digits.resolve(lang))
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    var shown by remember(date) { mutableStateOf(date) }
    val info = remember(shown) { if (shown in Patro.coverage) Patro.info(shown) else null }
    val haptics = rememberHaptics(settings.haptics)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .semantics {
                    dismiss {
                        onClose()
                        true
                    }
                }.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp)
                .testTag("day_detail"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DayDetailHeader(
            date = shown,
            today = today,
            lang = lang,
            digits = displaySettings.digits,
            bsMonthNames = bsMonthNames,
            onPrevious = {
                haptics.tick()
                shown = shown.minusDays(1)
            },
            onNext = {
                haptics.tick()
                shown = shown.plusDays(1)
            },
        )
        DayDetailChips(date = shown, info = info, today = today, digits = displaySettings.digits)
        if (info == null) {
            Text(
                text = stringResource(R.string.day_no_data),
                modifier = Modifier.padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            DayDetailFacts(info = info, lang = lang, digits = displaySettings.digits)
        }
        DayDetailActions(
            date = shown,
            info = info,
            lang = lang,
            settings = displaySettings,
            bsMonthNames = bsMonthNames,
            onCopied = haptics::confirm,
        )
    }
}

/** The date, which slides on the motion scheme's spring as it steps, and the previous and next buttons. */
@Composable
private fun DayDetailHeader(
    date: NepaliDate,
    today: NepaliDate?,
    lang: Lang,
    digits: Digits,
    bsMonthNames: List<String>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val motion = MaterialTheme.motionScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = date,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                // Next slides up, previous slides down: the way the arrow points.
                val direction = if (targetState > initialState) 1 else -1
                val enter =
                    slideInVertically(motion.defaultSpatialSpec()) { it / SLIDE_FRACTION * direction } +
                        fadeIn(motion.defaultEffectsSpec())
                val exit =
                    slideOutVertically(motion.defaultSpatialSpec()) { -it / SLIDE_FRACTION * direction } +
                        fadeOut(motion.fastEffectsSpec())
                enter.togetherWith(exit)
            },
            label = "day",
        ) { shownDate ->
            DayHeading(shownDate, today, lang, digits, bsMonthNames)
        }
        Spacer(Modifier.width(8.dp))
        DayStepper(
            canGoBack = date > NepaliDate.MIN,
            canGoForward = date < NepaliDate.MAX,
            onPrevious = onPrevious,
            onNext = onNext,
            modifier = Modifier.align(Alignment.Top),
        )
    }
}

/** The date tile, the weekday, the BS date in both scripts and the AD date. */
@Composable
private fun DayHeading(
    date: NepaliDate,
    today: NepaliDate?,
    lang: Lang,
    digits: Digits,
    bsMonthNames: List<String>,
) {
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    val isHoliday = remember(date) { date in Patro.coverage && Patro.info(date).isPublicHoliday }
    Row(verticalAlignment = Alignment.CenterVertically) {
        DayTile(date = date, isHoliday = isHoliday, isToday = date == today, digits = digits)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = weekdayName(date, lang),
                modifier = Modifier.semantics { heading() },
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = bsLong(date, bsMonthNames[date.monthValue - 1], digits),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (lang == Lang.ENGLISH) {
                Text(
                    text = bsLongNepali(date),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text =
                    stringResource(
                        R.string.day_weekday_ad,
                        weekdayName(date, lang),
                        adDate(date, adMonthNames, digits),
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** The BS day on the Cookie shape, or on the marigold Sunny shape when the date is today. */
@Composable
private fun DayTile(
    date: NepaliDate,
    isHoliday: Boolean,
    isToday: Boolean,
    digits: Digits,
) {
    val colors = LocalNepdateColors.current
    val (shape, container, content) =
        when {
            isToday -> Triple(SunnyShape, colors.today, colors.onToday)
            isHoliday -> Triple(CookieShape, colors.holidayContainer, colors.onHolidayContainer)
            else ->
                Triple(
                    CookieShape,
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
        }
    ShapeBadge(shape = shape, color = container, size = 88.dp) {
        Text(
            text = date.dayOfMonth.toString().inDigits(digits),
            color = content,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Previous day above next day, joined into one connected group. */
@Composable
private fun DayStepper(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        StepButton(
            icon = R.drawable.ic_sym_keyboard_arrow_up,
            label = stringResource(R.string.day_previous),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
            enabled = canGoBack,
            onClick = onPrevious,
        )
        StepButton(
            icon = R.drawable.ic_sym_keyboard_arrow_down,
            label = stringResource(R.string.day_next),
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            enabled = canGoForward,
            onClick = onNext,
        )
    }
}

@Composable
private fun StepButton(
    @DrawableRes icon: Int,
    label: String,
    shape: RoundedCornerShape,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        // Pressing rounds the connected corners out, the Expressive press feedback.
        shapes = IconButtonDefaults.shapes(shape = shape, pressedShape = RoundedCornerShape(16.dp)),
        colors =
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        modifier =
            Modifier
                .size(width = 48.dp, height = 48.dp)
                .semantics { contentDescription = label },
    ) {
        SymbolIcon(icon = icon, size = 22.dp)
    }
}

/** Tithi and events as one segmented list group. */
@Composable
private fun DayDetailFacts(
    info: CalendarInfo,
    lang: Lang,
    digits: Digits,
) {
    val colors = LocalNepdateColors.current
    val scheme = MaterialTheme.colorScheme
    val rows =
        buildList {
            info.tithi?.let { tithi ->
                add(
                    FactRow(
                        label = R.string.day_tithi,
                        value = tithi.name(lang),
                        icon = R.drawable.ic_sym_dark_mode_filled,
                        iconContainer = scheme.tertiaryContainer,
                        iconContent = scheme.onTertiaryContainer,
                        emphasised = false,
                        tag = null,
                    ),
                )
            }
            info.events.forEachIndexed { index, event ->
                // The first event of a public holiday is the holiday itself, so it gets the holiday tint.
                val isHolidayName = info.isPublicHoliday && index == 0
                add(
                    FactRow(
                        label = R.string.day_event_label,
                        value = event.name(lang),
                        icon = R.drawable.ic_sym_temple_hindu_filled,
                        iconContainer = if (isHolidayName) colors.holidayContainer else scheme.secondaryContainer,
                        iconContent = if (isHolidayName) colors.onHolidayContainer else scheme.onSecondaryContainer,
                        emphasised = isHolidayName,
                        tag = "day_event_$index",
                    ),
                )
            }
        }
    if (rows.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        rows.forEachIndexed { index, row ->
            FactListItem(row = row, digits = digits, shapes = ListItemDefaults.segmentedShapes(index, rows.size))
        }
    }
}

private data class FactRow(
    val label: Int,
    val value: String,
    @param:DrawableRes val icon: Int,
    val iconContainer: Color,
    val iconContent: Color,
    val emphasised: Boolean,
    val tag: String?,
)

@Composable
private fun FactListItem(
    row: FactRow,
    digits: Digits,
    shapes: ListItemShapes,
) {
    SegmentedListItem(
        shapes = shapes,
        modifier = Modifier.heightIn(min = 64.dp),
        colors =
            ListItemDefaults.segmentedColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                overlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        leadingContent = {
            ShapeBadge(shape = CircleShape, color = row.iconContainer, size = 40.dp) {
                SymbolIcon(icon = row.icon, tint = row.iconContent, size = 22.dp)
            }
        },
        overlineContent = { Text(stringResource(row.label), style = MaterialTheme.typography.labelMedium) },
    ) {
        Text(
            text = row.value.inDigits(digits),
            modifier = if (row.tag != null) Modifier.testTag(row.tag) else Modifier,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (row.emphasised) FontWeight.Bold else null,
        )
    }
}

private const val SLIDE_FRACTION = 3
