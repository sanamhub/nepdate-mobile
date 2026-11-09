package np.nepdate.ui.converter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.NepaliDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.LocalBottomBarPadding
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.settings.Choice
import np.nepdate.ui.settings.ConnectedToggleGroup
import np.nepdate.ui.settings.Haptics
import np.nepdate.ui.settings.rememberHaptics

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
    val inputs = rememberConverterInputs(seeds.picked, seeds.from, seeds.to)
    val picked = NepaliDate.ofEpochDay(inputs.pickedDay)
    val conversion = remember(picked, today) { conversionOf(picked, today) }
    val comparison = remember(inputs.fromDay, inputs.toDay) { inputs.comparison() }
    val haptics = rememberHaptics(settings.haptics)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = LocalBottomBarPadding.current.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ConverterTopBar(
            onSettings = onSettings,
            onReset =
                {
                    inputs.reset(today)
                    haptics.confirm()
                }.takeIf { inputs.isChanged(today) },
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DirectionGroup(direction = inputs.direction, haptics = haptics, onDirection = { inputs.direction = it })
            DateEntry(inputs, picked, digits, haptics)
            ResultCard(
                conversion = conversion,
                direction = inputs.direction,
                lang = lang,
                digits = digits,
                onShowInPatro = onShowInPatro,
                onCopied = haptics::confirm,
            )
            BetweenCard(
                comparison = comparison,
                today = today,
                direction = inputs.direction,
                digits = digits,
                onFrom = { inputs.fromDay = it.toEpochDay() },
                onTo = { inputs.toDay = it.toEpochDay() },
                onSwapped = haptics::tick,
            )
        }
    }
}

/**
 * One card with the two ways to set the date: the typing row on top and the wheel under it. The
 * row shows the wheel's date as its hint and a typed date moves the wheel, so they read as one
 * control rather than two inputs.
 */
@Composable
private fun DateEntry(
    inputs: ConverterInputs,
    picked: NepaliDate,
    digits: Digits,
    haptics: Haptics,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(top = 10.dp, start = 10.dp, end = 10.dp),
    ) {
        TypedDateField(
            direction = inputs.direction,
            current = picked,
            onDate = {
                inputs.jumpTo(it)
                haptics.confirm()
            },
        )
        key(inputs.direction, inputs.wheelKey) {
            DateWheel(
                date = picked,
                direction = inputs.direction,
                digits = digits,
                onDateChange = { inputs.pickedDay = it.toEpochDay() },
                onTick = haptics::tick,
                framed = false,
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
                    ?: ConverterInputs.yearStart(today)
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
private fun ConverterTopBar(
    onSettings: (() -> Unit)?,
    onReset: (() -> Unit)?,
) {
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
        // Shown only once something differs from today, so an untouched converter stays quiet.
        AnimatedVisibility(visible = onReset != null, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
            TextButton(onClick = { onReset?.invoke() }, shapes = ButtonDefaults.shapes()) {
                SymbolIcon(icon = R.drawable.ic_sym_history, size = 18.dp)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.converter_reset))
            }
        }
        if (onSettings != null) {
            val settingsLabel = stringResource(R.string.settings_title)
            IconButton(
                onClick = onSettings,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.semantics { contentDescription = settingsLabel },
            ) {
                SymbolIcon(icon = R.drawable.ic_sym_settings_filled, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** BS to AD and AD to BS as a connected toggle group; the chosen one turns into a full pill. */
@Composable
private fun DirectionGroup(
    direction: ConvertDirection,
    haptics: Haptics,
    onDirection: (ConvertDirection) -> Unit,
) {
    ConnectedToggleGroup(
        options =
            listOf(
                Choice(ConvertDirection.BS_TO_AD, stringResource(R.string.converter_bs_to_ad)),
                Choice(ConvertDirection.AD_TO_BS, stringResource(R.string.converter_ad_to_bs)),
            ),
        selected = direction,
        onSelect = onDirection,
        haptics = haptics,
        label = stringResource(R.string.converter_direction),
        tags = listOf("converter_bs_to_ad", "converter_ad_to_bs"),
    )
}
