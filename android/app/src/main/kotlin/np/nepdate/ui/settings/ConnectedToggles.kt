package np.nepdate.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.ui.common.SymbolIcon

/**
 * A single-choice connected button group (Material 3 Expressive): equal-width toggle buttons whose
 * inner corners round out when pressed and that turn into a full pill when chosen. The chosen one
 * shows a check that grows in with the motion scheme's spring.
 *
 * @param haptics the settings' haptics, or null where the app's settings are not at hand
 * @param tags optional test tags, one per option
 */
@Composable
internal fun <T> ConnectedToggleGroup(
    options: List<Choice<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    haptics: Haptics?,
    modifier: Modifier = Modifier,
    label: String? = null,
    tags: List<String> = emptyList(),
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectableGroup()
                .then(if (label != null) Modifier.semantics { contentDescription = label } else Modifier),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            val checked = option.value == selected
            ToggleButton(
                checked = checked,
                onCheckedChange = {
                    if (!checked) {
                        haptics?.toggle(true)
                        onSelect(option.value)
                    }
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .semantics { role = Role.RadioButton }
                        .then(tags.getOrNull(index)?.let { Modifier.testTag(it) } ?: Modifier),
                shapes = connectedShapes(index, options.size),
            ) {
                val motion = MaterialTheme.motionScheme
                AnimatedVisibility(
                    visible = checked,
                    enter = expandHorizontally(motion.fastSpatialSpec()) + fadeIn(motion.fastEffectsSpec()),
                    exit = shrinkHorizontally(motion.fastSpatialSpec()) + fadeOut(motion.fastEffectsSpec()),
                ) {
                    Row {
                        SymbolIcon(R.drawable.ic_sym_check, size = 18.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                }
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun connectedShapes(
    index: Int,
    count: Int,
): ToggleButtonShapes =
    when (index) {
        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
        count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
    }
