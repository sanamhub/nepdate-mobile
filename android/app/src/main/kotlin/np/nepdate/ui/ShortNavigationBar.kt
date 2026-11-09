package np.nepdate.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import np.nepdate.ui.common.SymbolIcon
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The floating navigation bar (Material 3 Expressive, 2025): a fully rounded pill on
 * surfaceContainerHighest, inset from the screen edges and lifted above the gesture bar. The pages
 * draw their cards on surfaceContainer, so the bar takes the highest container tone, a hairline
 * outline and a deeper shadow to stay distinct from whatever scrolls under it, in dark theme too
 * where a shadow alone barely shows. The selected
 * indicator follows the tab pager's position, so it glides with a swipe and springs with a tap;
 * mid-way it stretches a little, like a drop of ink moving between tabs.
 *
 * @param selected the tab the pager is on or heading to; read here so only the bar recomposes
 * @param position the tab pager's position: the current page plus its offset fraction
 */
@Composable
internal fun FloatingNavigationBar(
    selected: () -> NepdateDestination,
    position: () -> Float,
    onSelect: (NepdateDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.navigationBarsPadding().padding(bottom = BAR_MARGIN),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(BAR_PADDING)) {
            SelectedIndicator(position)
            Row(modifier = Modifier.selectableGroup()) {
                val target = selected()
                NepdateDestination.entries.forEach { item ->
                    NavigationItem(item = item, isSelected = item == target, onSelect = onSelect)
                }
            }
        }
    }
}

/** The bottom content padding a tab needs so its last row can scroll clear of the bar. */
@Composable
internal fun floatingBarPadding(): PaddingValues {
    val gesture = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return PaddingValues(bottom = gesture + FLOATING_BAR_HEIGHT + BAR_MARGIN + CONTENT_GAP)
}

/** The secondary-container pill under the selected tab. It reads the position only while drawing. */
@Composable
private fun SelectedIndicator(position: () -> Float) {
    Box(
        modifier =
            Modifier
                .offset { IntOffset((position() * ITEM_WIDTH.toPx()).roundToInt(), 0) }
                .graphicsLayer {
                    val travel = position().let { abs(it - it.roundToInt()) }
                    scaleX = 1f + travel * STRETCH
                    scaleY = 1f - travel * SQUASH
                }.size(ITEM_WIDTH, ITEM_HEIGHT)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
    )
}

/** One tab: the symbol (filled when selected) over its label, a 56 dp high target. */
@Composable
private fun NavigationItem(
    item: NepdateDestination,
    isSelected: Boolean,
    onSelect: (NepdateDestination) -> Unit,
) {
    val label = stringResource(item.label)
    val colors = MaterialTheme.colorScheme
    val tint by animateColorAsState(
        targetValue = if (isSelected) colors.onSecondaryContainer else colors.onSurfaceVariant,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "nav tint",
    )
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier =
            Modifier
                .size(ITEM_WIDTH, ITEM_HEIGHT)
                .clip(CircleShape)
                .selectable(
                    selected = isSelected,
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.Tab,
                    onClick = { onSelect(item) },
                ).testTag("nav_${item.name.lowercase()}")
                .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SymbolIcon(icon = if (isSelected) item.selectedIcon else item.icon, tint = tint)
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = tint,
            maxLines = 1,
        )
    }
}

private val ITEM_WIDTH: Dp = 88.dp
private val ITEM_HEIGHT: Dp = 56.dp
private val BAR_PADDING: Dp = 6.dp
private val BAR_MARGIN: Dp = 12.dp
private val CONTENT_GAP: Dp = 8.dp

/** The drawn height of the pill: one item plus its padding above and below. */
internal val FLOATING_BAR_HEIGHT: Dp = ITEM_HEIGHT + BAR_PADDING * 2

private const val STRETCH = 0.5f
private const val SQUASH = 0.12f
