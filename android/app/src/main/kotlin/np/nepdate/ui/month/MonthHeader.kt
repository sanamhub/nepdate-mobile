package np.nepdate.ui.month

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupMenuState
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.adSpan
import np.nepdate.ui.common.monthTitle
import np.nepdate.ui.theme.LocalNepdateColors

/** The top bar: the month title opens the year view; search and settings on the right. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            IconButton(
                onClick = onSearch,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.semantics { contentDescription = searchLabel },
            ) {
                SymbolIcon(icon = R.drawable.ic_sym_search, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (onSettings != null) {
            val settingsLabel = stringResource(R.string.settings_title)
            IconButton(
                onClick = onSettings,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.semantics { contentDescription = settingsLabel },
            ) {
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
                style = MaterialTheme.typography.titleLarge,
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

/**
 * Previous, Today (only off the current month) and Next as one Material 3 Expressive connected
 * button group: a pressed button widens and squares its inner corners while its neighbours give way.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val previous = NavButton(previousLabel, R.drawable.ic_sym_chevron_left, canGoPrevious, onPrevious)
    val next = NavButton(nextLabel, R.drawable.ic_sym_chevron_right, canGoNext, onNext)
    val todayLabel = stringResource(R.string.month_today)
    val leading =
        ButtonShapes(
            shape = ButtonGroupDefaults.connectedLeadingButtonShape,
            pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
        )
    val middle = ButtonShapes(MaterialTheme.shapes.small, ButtonGroupDefaults.connectedMiddleButtonPressShape)
    val trailing =
        ButtonShapes(
            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
            pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
        )
    ButtonGroup(
        overflowIndicator = { ButtonGroupDefaults.OverflowIndicator(it) },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navItem(previous, leading)
        if (showToday) {
            customItem(
                buttonGroupContent = {
                    val source = remember { MutableInteractionSource() }
                    Button(
                        onClick = onToday,
                        shapes = middle,
                        modifier =
                            Modifier
                                .animateWidth(source)
                                .testTag("month_today_button")
                                .heightIn(min = NAV_BUTTON_HEIGHT),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        interactionSource = source,
                    ) { Text(text = todayLabel, style = MaterialTheme.typography.labelLarge) }
                },
                menuContent = { menu -> MenuEntry(todayLabel, onToday, menu) },
            )
        }
        navItem(next, trailing)
    }
}

/** One chevron of the group: an icon-only tonal button with its spoken label. */
private class NavButton(
    val label: String,
    @DrawableRes val icon: Int,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ButtonGroupScope.navItem(
    button: NavButton,
    shapes: ButtonShapes,
) {
    customItem(
        buttonGroupContent = {
            val source = remember { MutableInteractionSource() }
            FilledTonalButton(
                onClick = button.onClick,
                shapes = shapes,
                enabled = button.enabled,
                modifier =
                    Modifier
                        .animateWidth(source)
                        .semantics { contentDescription = button.label }
                        .size(NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT),
                contentPadding = PaddingValues(0.dp),
                interactionSource = source,
            ) { SymbolIcon(icon = button.icon, size = 22.dp) }
        },
        menuContent = { menu -> MenuEntry(button.label, button.onClick, menu) },
    )
}

/** The entry a button becomes if the group ever overflows into its menu. */
@Composable
private fun MenuEntry(
    label: String,
    onClick: () -> Unit,
    menu: ButtonGroupMenuState,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = {
            onClick()
            menu.dismiss()
        },
    )
}

private val NAV_BUTTON_WIDTH = 52.dp

// The group's buttons are drawn at the 48 dp touch height, so their target is the button itself.
private val NAV_BUTTON_HEIGHT = 48.dp
