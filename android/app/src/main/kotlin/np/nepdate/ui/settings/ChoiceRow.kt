package np.nepdate.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import np.nepdate.R

/**
 * A settings row that shows the current choice and opens a dialog of radio options. Picking an
 * option applies it at once and closes the dialog.
 */
@Composable
internal fun <T> ChoiceRow(
    @DrawableRes icon: Int,
    tone: Tone,
    title: String,
    choices: List<Choice<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    shapes: ListItemShapes,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    SettingsRow(
        icon = icon,
        tone = tone,
        title = title,
        supporting = choices.firstOrNull { it.value == selected }?.label,
        shapes = shapes,
        onClick = { open = true },
    )
    if (open) {
        ChoiceDialog(
            title = title,
            choices = choices,
            selected = selected,
            onSelect = { value ->
                onSelect(value)
                open = false
            },
            onDismiss = { open = false },
        )
    }
}

@Composable
private fun <T> ChoiceDialog(
    title: String,
    choices: List<Choice<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val haptics = LocalSettingsHaptics.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.day_close))
            }
        },
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                choices.forEach { choice ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clip(MaterialTheme.shapes.large)
                                .selectable(
                                    selected = choice.value == selected,
                                    role = Role.RadioButton,
                                    onClick = {
                                        haptics.tick()
                                        onSelect(choice.value)
                                    },
                                ).padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = choice.value == selected, onClick = null)
                        Text(text = choice.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}
