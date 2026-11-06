package np.nepdate.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.ui.common.AdSpan
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adSpan
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.theme.LocalNepdateColors

/** The top bar: the month title opens the year view; search and settings on the right. */
@Composable
internal fun MonthTopBar(
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
internal fun MonthHeader(
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
                modifier = Modifier.testTag("month_today_button"),
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
    modifier: Modifier = Modifier,
    wide: Boolean = false,
    content: @Composable () -> Unit,
) {
    // The click area and its semantics include the 4 dp above and below the pill, so the touch
    // target is 48 dp high while the drawn pill and its ripple stay 40 dp.
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ).semantics { contentDescription = label }
                .padding(vertical = 4.dp)
                .then(if (wide) Modifier.heightIn(min = 40.dp) else Modifier.size(width = 48.dp, height = 40.dp))
                .clip(shape)
                .background(if (enabled) color else color.copy(alpha = DISABLED_ALPHA))
                .indication(interactionSource, ripple())
                .then(if (wide) Modifier.padding(horizontal = 14.dp, vertical = 10.dp) else Modifier),
        contentAlignment = Alignment.Center,
    ) { content() }
}

private const val DISABLED_ALPHA = 0.38f
