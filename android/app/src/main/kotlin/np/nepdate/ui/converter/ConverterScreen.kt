package np.nepdate.ui.converter

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.DateDiff
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toLocalDate
import np.nepdate.R
import np.nepdate.data.Digits
import np.nepdate.data.Settings
import np.nepdate.ui.common.ScreenHeader
import np.nepdate.ui.common.adDate
import np.nepdate.ui.common.bsLong
import np.nepdate.ui.common.inDigits
import np.nepdate.ui.common.languageFor
import np.nepdate.ui.common.resolve
import np.nepdate.ui.common.weekdayName
import np.nepdate.ui.theme.LocalNepdateColors
import java.time.LocalDate

/** The Converter tab (MR-04): BS to AD, AD to BS, and a difference between two BS dates. */
@Composable
internal fun ConverterScreen(
    today: NepaliDate,
    settings: Settings,
    modifier: Modifier = Modifier,
    initialBs: String = "",
    initialCompare: String = "",
    initialState: ConverterState = ConverterState(),
    onSettings: (() -> Unit)? = null,
) {
    val lang = languageFor(LocalConfiguration.current.locales[0])
    val displaySettings = settings.copy(digits = settings.digits.resolve(lang))
    val bsMonthNames = stringArrayResource(R.array.bs_month_long).toList()
    val adMonthNames = stringArrayResource(R.array.ad_month_long).toList()
    var bsInput by remember { mutableStateOf(initialBs) }
    var compareInput by remember { mutableStateOf(initialCompare) }
    var state by remember { mutableStateOf(initialState) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ScreenHeader(title = stringResource(R.string.converter_title), onSettings = onSettings)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BsConverter(
                bsInput = bsInput,
                onBsInput = { bsInput = it },
                onConvert = { state = state.fromBs(bsInput, today) },
                onAdPicked = { date -> state = state.fromAd(date, today) },
                initialAd = state.result?.ad ?: today.toLocalDate(),
                error = state.error,
                bsMonthNames = bsMonthNames,
                digits = displaySettings.digits,
            )
            state.result?.let { result ->
                ResultBlock(
                    result = result,
                    settings = displaySettings,
                    lang = lang,
                    monthNames = adMonthNames,
                    bsMonthNames = bsMonthNames,
                )
            }
            CompareBlock(
                compareInput = compareInput,
                onCompareInput = { compareInput = it },
                onCompare = { state = state.compareWithBs(compareInput) },
                state = state,
                settings = displaySettings,
                bsMonthNames = bsMonthNames,
            )
        }
    }
}

@Composable
private fun BsConverter(
    bsInput: String,
    onBsInput: (String) -> Unit,
    onConvert: () -> Unit,
    onAdPicked: (LocalDate) -> Unit,
    initialAd: LocalDate,
    error: ConverterError?,
    bsMonthNames: List<String>,
    digits: Digits,
) {
    OutlinedTextField(
        value = bsInput,
        onValueChange = onBsInput,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("converter_bs_input"),
        label = { Text(stringResource(R.string.converter_bs_label)) },
        placeholder = { Text(stringResource(R.string.converter_bs_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onConvert() }),
    )
    Button(
        onClick = onConvert,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("converter_convert"),
    ) {
        Text(stringResource(R.string.converter_convert))
    }
    AdDateButton(initial = initialAd, onPicked = onAdPicked)
    error?.let {
        Text(
            text = errorText(it, bsMonthNames, digits),
            modifier = Modifier.testTag("converter_error"),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AdDateButton(
    initial: LocalDate,
    onPicked: (LocalDate) -> Unit,
) {
    val context = LocalContext.current
    val pickerTitle = stringResource(R.string.converter_ad_picker_title)
    OutlinedButton(
        onClick = {
            val picker =
                DatePickerDialog(
                    context,
                    { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
                    initial.year,
                    initial.monthValue - 1,
                    initial.dayOfMonth,
                )
            picker.setTitle(pickerTitle)
            picker.show()
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("converter_ad_button"),
    ) {
        Text(stringResource(R.string.converter_choose_ad))
    }
}

@Composable
private fun ResultBlock(
    result: Conversion,
    settings: Settings,
    lang: Lang,
    monthNames: List<String>,
    bsMonthNames: List<String>,
) {
    HorizontalDivider(Modifier.padding(top = 8.dp))
    Text(
        text = stringResource(R.string.converter_result),
        color = LocalNepdateColors.current.dimmed,
        style = MaterialTheme.typography.labelLarge,
    )
    Text(
        text = bsLong(result.bs, bsMonthNames[result.bs.monthValue - 1], settings.digits),
        modifier = Modifier.testTag("converter_result"),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        text =
            stringResource(
                R.string.converter_weekday_ad,
                weekdayName(result.bs, lang),
                adDate(result.ad, monthNames, settings.digits),
            ),
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = daysFromToday(result.daysFromToday, settings.digits),
        color = LocalNepdateColors.current.dimmed,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun CompareBlock(
    compareInput: String,
    onCompareInput: (String) -> Unit,
    onCompare: () -> Unit,
    state: ConverterState,
    settings: Settings,
    bsMonthNames: List<String>,
) {
    HorizontalDivider(Modifier.padding(top = 8.dp))
    OutlinedTextField(
        value = compareInput,
        onValueChange = onCompareInput,
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("converter_compare_input"),
        label = { Text(stringResource(R.string.converter_compare_label)) },
        placeholder = { Text(stringResource(R.string.converter_compare_hint)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onCompare() }),
    )
    Button(
        onClick = onCompare,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("converter_compare"),
    ) {
        Text(stringResource(R.string.converter_compare))
    }
    state.comparisonError?.let {
        Text(
            text = errorText(it, bsMonthNames, settings.digits),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    state.comparison?.let { comparison ->
        Text(
            text =
                stringResource(
                    R.string.converter_compare_range,
                    bsLong(
                        comparison.from,
                        bsMonthNames[comparison.from.monthValue - 1],
                        settings.digits,
                    ),
                    bsLong(
                        comparison.to,
                        bsMonthNames[comparison.to.monthValue - 1],
                        settings.digits,
                    ),
                ),
            modifier = Modifier.testTag("converter_comparison"),
            color = LocalNepdateColors.current.dimmed,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = differenceText(comparison.diff, settings.digits),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun errorText(
    error: ConverterError,
    bsMonthNames: List<String>,
    digits: Digits,
): String =
    when (error) {
        is ConverterError.InvalidDay ->
            pluralStringResource(
                R.plurals.converter_error_invalid_day,
                error.monthLength,
                bsMonthNames[error.month - 1],
                error.monthLength,
                error.year,
            )
        ConverterError.InvalidMonth -> stringResource(R.string.converter_error_invalid_month)
        ConverterError.OutOfRange -> stringResource(R.string.converter_error_out_of_range)
        ConverterError.Unreadable -> stringResource(R.string.converter_error_unreadable)
        ConverterError.NoFirstDate -> stringResource(R.string.converter_error_no_first_date)
    }.inDigits(digits)

@Composable
private fun daysFromToday(
    days: Int,
    digits: Digits,
): String =
    when {
        days == 0 -> stringResource(R.string.converter_today)
        days == 1 -> stringResource(R.string.converter_tomorrow)
        days > 0 -> pluralStringResource(R.plurals.converter_days_from_today, days, days)
        else -> pluralStringResource(R.plurals.converter_days_before_today, -days, -days)
    }.inDigits(digits)

@Composable
private fun differenceText(
    diff: DateDiff,
    digits: Digits,
): String =
    listOf(
        pluralStringResource(R.plurals.converter_years, diff.years, diff.years),
        pluralStringResource(R.plurals.converter_months, diff.months, diff.months),
        pluralStringResource(R.plurals.converter_days, diff.days, diff.days),
    ).joinToString(", ").inDigits(digits)
