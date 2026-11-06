package np.nepdate.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.segmentShape
import np.nepdate.ui.theme.LocalNepdateColors

/** The tonal pair behind a row's leading symbol. */
internal enum class Tone { PRIMARY, SECONDARY, TERTIARY, HOLIDAY }

/** The container and content colours of [this] tone in the current theme. */
@Composable
internal fun Tone.colors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        Tone.PRIMARY -> scheme.primaryContainer to scheme.onPrimaryContainer
        Tone.SECONDARY -> scheme.secondaryContainer to scheme.onSecondaryContainer
        Tone.TERTIARY -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        Tone.HOLIDAY -> LocalNepdateColors.current.let { it.holidayContainer to it.onHolidayContainer }
    }
}

/** The back arrow at the top of Settings and About, with an optional large title under it. */
@Composable
internal fun BackHeader(
    onBack: () -> Unit,
    backTag: String,
    title: String? = null,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 4.dp, top = 8.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag(backTag)) {
            Icon(
                painter = painterResource(R.drawable.ic_sym_arrow_back),
                contentDescription = stringResource(R.string.settings_back),
            )
        }
        if (title != null) {
            Text(
                text = title,
                modifier =
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                        .semantics { heading() },
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
            )
        }
    }
}

/**
 * A titled group of segmented rows: 2 dp gaps, large outer corners and small inner ones. Each row
 * lambda receives the shape of its position in the group.
 */
@Composable
internal fun SettingsGroup(
    title: String,
    rows: List<@Composable (Shape) -> Unit>,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = title,
            modifier =
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
                    .semantics { heading() },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            rows.forEachIndexed { index, row -> row(segmentShape(index, rows.size)) }
        }
    }
}

/**
 * One segmented settings row: a 40 dp tonal circle with a filled symbol, the title and supporting
 * text, and an optional trailing control. [modifier] carries the click or toggle semantics; it is
 * applied inside the clip so the ripple follows the row shape.
 */
@Composable
internal fun SettingsRow(
    @DrawableRes icon: Int,
    tone: Tone,
    title: String,
    supporting: String?,
    shape: Shape,
    modifier: Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val (container, content) = tone.colors()
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .then(modifier)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(container),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SymbolIcon(icon, tint = content, size = 22.dp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        trailing?.invoke(this)
    }
}

/** A settings row with an M3 switch; the whole row toggles. */
@Composable
internal fun SwitchRow(
    @DrawableRes icon: Int,
    tone: Tone,
    title: String,
    supporting: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
) {
    SettingsRow(
        icon = icon,
        tone = tone,
        title = title,
        supporting = supporting,
        shape = shape,
        modifier = Modifier.toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch),
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
            thumbContent =
                if (checked) {
                    { SymbolIcon(R.drawable.ic_sym_check, size = SwitchDefaults.IconSize) }
                } else {
                    null
                },
        )
    }
}

/** One option of a [ChoiceRow]. */
internal data class Choice<T>(
    val value: T,
    val label: String,
)

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
    shape: Shape,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    SettingsRow(
        icon = icon,
        tone = tone,
        title = title,
        supporting = choices.firstOrNull { it.value == selected }?.label,
        shape = shape,
        modifier = Modifier.clickable(role = Role.Button) { open = true },
    )
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = { open = false }, modifier = Modifier.heightIn(min = 48.dp)) {
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
                                    .clip(MaterialTheme.shapes.medium)
                                    .selectable(
                                        selected = choice.value == selected,
                                        role = Role.RadioButton,
                                        onClick = {
                                            onSelect(choice.value)
                                            open = false
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
}
