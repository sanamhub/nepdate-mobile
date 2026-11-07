package np.nepdate.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import np.nepdate.R
import np.nepdate.ui.common.ShapeBadge
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.theme.LocalNepdateColors

/** The tonal pair behind a row's leading symbol. */
internal enum class Tone { PRIMARY, SECONDARY, TERTIARY, HOLIDAY }

/** One option of a [ChoiceRow] or a [ConnectedToggleGroup]. */
internal data class Choice<T>(
    val value: T,
    val label: String,
)

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

/** The haptics of the settings screens; silent unless a screen provides one. */
internal val LocalSettingsHaptics =
    staticCompositionLocalOf {
        Haptics(
            feedback =
                object : HapticFeedback {
                    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
                },
            enabled = false,
        )
    }

/**
 * The back arrow of Settings and About. Its tint comes from the app bar's navigation icon colour,
 * so it follows light, dark and wallpaper colours; pressing it morphs the shape.
 */
@Composable
internal fun BackButton(
    onBack: () -> Unit,
    tag: String,
) {
    val label = stringResource(R.string.settings_back)
    IconButton(
        onClick = onBack,
        shapes = IconButtonDefaults.shapes(),
        modifier = Modifier.testTag(tag).semantics { contentDescription = label },
    ) {
        SymbolIcon(R.drawable.ic_sym_arrow_back)
    }
}

/**
 * A titled group of segmented rows (Android 16 Settings style): 2 dp gaps, large outer corners and
 * small inner ones that round out while a row is pressed. Each row receives the shapes of its place.
 */
@Composable
internal fun SettingsGroup(
    title: String,
    rows: List<@Composable (ListItemShapes) -> Unit>,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(
            text = title,
            modifier =
                Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                    .semantics { heading() },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall,
        )
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            rows.forEachIndexed { index, row -> row(ListItemDefaults.segmentedShapes(index, rows.size)) }
        }
    }
}

/** The 40 dp tonal circle with a filled symbol at the start of a settings row. */
@Composable
internal fun ToneIcon(
    @DrawableRes icon: Int,
    tone: Tone,
) {
    val (container, content) = tone.colors()
    ShapeBadge(shape = CircleShape, color = container, size = 40.dp) {
        SymbolIcon(icon, tint = content, size = 22.dp)
    }
}

/** The colours every settings row shares: the row sits on the container colour of the page. */
@Composable
internal fun settingsRowColors() =
    ListItemDefaults.segmentedColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        supportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

/** One tappable settings row: the tonal symbol, title and supporting text, and a trailing slot. */
@Composable
internal fun SettingsRow(
    @DrawableRes icon: Int,
    tone: Tone,
    title: String,
    supporting: String?,
    shapes: ListItemShapes,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = Modifier.heightIn(min = 64.dp),
        colors = settingsRowColors(),
        leadingContent = { ToneIcon(icon, tone) },
        supportingContent = supporting?.let { text -> { Text(text) } },
        trailingContent = trailing,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}

/** A settings row with a switch; the whole row toggles and gives a light haptic tick. */
@Composable
internal fun SwitchRow(
    @DrawableRes icon: Int,
    tone: Tone,
    title: String,
    supporting: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shapes: ListItemShapes,
) {
    val haptics = LocalSettingsHaptics.current
    SegmentedListItem(
        onClick = {
            haptics.toggle(!checked)
            onCheckedChange(!checked)
        },
        shapes = shapes,
        // A switch row, not a selected item: it keeps its colours and reads as a switch.
        modifier =
            Modifier.heightIn(min = 64.dp).semantics {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
            },
        colors = settingsRowColors(),
        leadingContent = { ToneIcon(icon, tone) },
        supportingContent = supporting?.let { text -> { Text(text) } },
        trailingContent = {
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
        },
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}
