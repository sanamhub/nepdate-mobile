package np.nepdate.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import np.nepdate.ui.common.SymbolIcon

/**
 * The Material 3 Expressive short navigation bar (64 dp): the selected tab shows its filled symbol
 * in a secondary-container pill and a bold label; the others show outlined symbols. One composable
 * for every tab, so the bar never drifts between screens.
 */
@Composable
internal fun ShortNavigationBar(
    selected: NepdateDestination,
    onSelect: (NepdateDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .navigationBarsPadding()
                    .height(64.dp)
                    .selectableGroup(),
        ) {
            NepdateDestination.entries.forEach { item ->
                NavigationItem(
                    item = item,
                    isSelected = item == selected,
                    onSelect = onSelect,
                )
            }
        }
    }
}

/** One tab of the bar: the symbol in its pill and the label below it. */
@Composable
private fun RowScope.NavigationItem(
    item: NepdateDestination,
    isSelected: Boolean,
    onSelect: (NepdateDestination) -> Unit,
) {
    val label = stringResource(item.label)
    Column(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .selectable(
                    selected = isSelected,
                    onClick = { onSelect(item) },
                    role = Role.Tab,
                ).testTag("nav_${item.name.lowercase()}")
                .semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        NavigationPill(item = item, isSelected = isSelected)
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color =
                if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            maxLines = 1,
        )
    }
}

/** The pill behind the symbol; it fades in when the tab becomes selected. */
@Composable
private fun NavigationPill(
    item: NepdateDestination,
    isSelected: Boolean,
) {
    val pill by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "nav pill",
    )
    Box(
        modifier =
            Modifier
                .size(width = 56.dp, height = 32.dp)
                .clip(CircleShape)
                .background(pill),
        contentAlignment = Alignment.Center,
    ) {
        SymbolIcon(
            icon = if (isSelected) item.selectedIcon else item.icon,
            tint =
                if (isSelected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
