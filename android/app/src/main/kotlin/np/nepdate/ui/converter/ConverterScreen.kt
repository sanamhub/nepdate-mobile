package np.nepdate.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Settings
import np.nepdate.ui.common.GroupPosition
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.groupShape
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve

/**
 * The Converter tab (MR-04): a direction toggle, a year, month and day wheel whose result updates
 * live, and the distance between two dates. The wheel only offers valid days, so there is no error
 * state.
 *
 * @param initialBs a BS date in any form the lenient parser reads; seeds the wheel (tests, screenshots)
 * @param initialCompare a BS date that seeds the first date of the "between two dates" card
 * @param initialState seeds the wheel and the comparison when [initialBs] and [initialCompare] are blank
 * @param onShowInPatro opens the converted date in the month view; the button hides when null
 */
@Composable
internal fun ConverterScreen(
    today: NepaliDate,
    settings: Settings,
    modifier: Modifier = Modifier,
    initialBs: String = "",
    initialCompare: String = "",
    initialState: ConverterState = ConverterState(),
    onSettings: (() -> Unit)? = null,
    onShowInPatro: ((NepaliDate) -> Unit)? = null,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val digits = settings.digits.resolve(lang)
    val seeds = remember { ConverterSeeds.of(initialBs, initialCompare, initialState, today) }
    var direction by rememberSaveable { mutableStateOf(ConvertDirection.BS_TO_AD) }
    var pickedDay by rememberSaveable { mutableLongStateOf(seeds.picked.toEpochDay()) }
    var fromDay by rememberSaveable { mutableLongStateOf(seeds.from.toEpochDay()) }
    var toDay by rememberSaveable { mutableLongStateOf(seeds.to.toEpochDay()) }
    val picked = NepaliDate.ofEpochDay(pickedDay)
    val conversion = remember(picked, today) { conversionOf(picked, today) }
    val comparison =
        remember(fromDay, toDay) { comparisonOf(NepaliDate.ofEpochDay(fromDay), NepaliDate.ofEpochDay(toDay)) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConverterTopBar(onSettings = onSettings)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DirectionGroup(direction = direction, onDirection = { direction = it })
            key(direction) {
                DateWheel(
                    date = picked,
                    direction = direction,
                    digits = digits,
                    onDateChange = { pickedDay = it.toEpochDay() },
                )
            }
            ResultCard(
                conversion = conversion,
                direction = direction,
                lang = lang,
                digits = digits,
                onShowInPatro = onShowInPatro,
            )
            BetweenCard(
                comparison = comparison,
                today = today,
                direction = direction,
                digits = digits,
                onFrom = { fromDay = it.toEpochDay() },
                onTo = { toDay = it.toEpochDay() },
            )
        }
    }
}

/** The starting dates of the screen, read once from the test and screenshot seeds. */
private data class ConverterSeeds(
    val picked: NepaliDate,
    val from: NepaliDate,
    val to: NepaliDate,
) {
    companion object {
        fun of(
            initialBs: String,
            initialCompare: String,
            initialState: ConverterState,
            today: NepaliDate,
        ): ConverterSeeds {
            val picked = parsed(initialBs, today) ?: initialState.result?.bs ?: today
            val from =
                initialState.comparison?.from
                    ?: parsed(initialCompare, today)
                    ?: NepaliDate.of(today.year, 1, 1)
            val to = initialState.comparison?.to ?: today
            return ConverterSeeds(picked, from, to)
        }

        private fun parsed(
            text: String,
            today: NepaliDate,
        ): NepaliDate? = if (text.isBlank()) null else ConverterState().fromBs(text, today).result?.bs
    }
}

@Composable
private fun ConverterTopBar(onSettings: (() -> Unit)?) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 8.dp, top = 8.dp)
                .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.converter_title),
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { heading() },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onSettings != null) {
            val settingsLabel = stringResource(R.string.settings_title)
            IconButton(onClick = onSettings, modifier = Modifier.semantics { contentDescription = settingsLabel }) {
                SymbolIcon(icon = R.drawable.ic_sym_settings, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** BS to AD and AD to BS as a connected two-button group; the selected one is fully rounded. */
@Composable
private fun DirectionGroup(
    direction: ConvertDirection,
    onDirection: (ConvertDirection) -> Unit,
) {
    val groupLabel = stringResource(R.string.converter_direction)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectableGroup()
                .semantics { contentDescription = groupLabel },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        DirectionButton(
            label = stringResource(R.string.converter_bs_to_ad),
            selected = direction == ConvertDirection.BS_TO_AD,
            position = GroupPosition.START,
            tag = "converter_bs_to_ad",
            onClick = { onDirection(ConvertDirection.BS_TO_AD) },
            modifier = Modifier.weight(1f),
        )
        DirectionButton(
            label = stringResource(R.string.converter_ad_to_bs),
            selected = direction == ConvertDirection.AD_TO_BS,
            position = GroupPosition.END,
            tag = "converter_ad_to_bs",
            onClick = { onDirection(ConvertDirection.AD_TO_BS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DirectionButton(
    label: String,
    selected: Boolean,
    position: GroupPosition,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = if (selected) RoundedCornerShape(24.dp) else groupShape(position, outer = 24.dp, inner = 8.dp)
    val container = if (selected) scheme.primary else scheme.surfaceContainerHighest
    val content = if (selected) scheme.onPrimary else scheme.onSurface
    Row(
        modifier =
            modifier
                .height(48.dp)
                .clip(shape)
                .background(container)
                .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
                .testTag(tag)
                .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            SymbolIcon(icon = R.drawable.ic_sym_check, tint = content, size = 18.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
