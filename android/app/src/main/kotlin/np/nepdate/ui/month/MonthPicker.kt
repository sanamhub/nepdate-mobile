package np.nepdate.ui.month

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.Month
import np.nepdate.R

/** The month picker covers the whole BS range the library supports (MR-01 AC6). */
internal const val PICKER_FIRST_YEAR = 1901
internal const val PICKER_LAST_YEAR = 2199
internal const val MONTHS_PER_YEAR = 12
internal const val MONTH_PAGE_COUNT = (PICKER_LAST_YEAR - PICKER_FIRST_YEAR + 1) * MONTHS_PER_YEAR

private const val MONTH_COLUMNS = 3
private const val MAX_YEAR_DIGITS = 4
private const val SCRIM_ALPHA = 0.40f

/** The month and year picker over the month screen (MR-01 AC6). */
@Composable
internal fun MonthYearPicker(
    initialYear: Int,
    initialMonth: Int,
    lang: Lang,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit,
) {
    var yearText by remember { mutableStateOf(initialYear.toString()) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth) }
    val year = yearText.toIntOrNull()
    val validYear = year != null && year in PICKER_FIRST_YEAR..PICKER_LAST_YEAR

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag("month_picker"),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA))
                    .clickable(onClick = onDismiss),
        )
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.month_picker_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                YearField(yearText = yearText, isValid = validYear, onYearChange = { yearText = it })
                Text(
                    text = stringResource(R.string.month_picker_month),
                    style = MaterialTheme.typography.labelLarge,
                )
                MonthChips(selectedMonth = selectedMonth, lang = lang, onSelect = { selectedMonth = it })
                Spacer(Modifier.height(4.dp))
                PickerButtons(
                    enabled = validYear,
                    onDismiss = onDismiss,
                    onGo = { year?.let { onConfirm(it, selectedMonth) } },
                )
            }
        }
    }
}

@Composable
private fun PickerButtons(
    enabled: Boolean,
    onDismiss: () -> Unit,
    onGo: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onDismiss) {
            Text(stringResource(R.string.month_picker_cancel))
        }
        TextButton(onClick = onGo, enabled = enabled) {
            Text(stringResource(R.string.month_picker_go))
        }
    }
}

@Composable
private fun YearField(
    yearText: String,
    isValid: Boolean,
    onYearChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = yearText,
        onValueChange = { value ->
            if (value.length <= MAX_YEAR_DIGITS && value.all { it in '0'..'9' }) {
                onYearChange(value)
            }
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("month_picker_year"),
        label = { Text(stringResource(R.string.month_picker_year)) },
        isError = yearText.isNotEmpty() && !isValid,
        supportingText =
            if (yearText.isNotEmpty() && !isValid) {
                { Text(stringResource(R.string.month_picker_year_error)) }
            } else {
                null
            },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
private fun MonthChips(
    selectedMonth: Int,
    lang: Lang,
    onSelect: (Int) -> Unit,
) {
    Month.entries.chunked(MONTH_COLUMNS).forEach { months ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            months.forEach { month ->
                FilterChip(
                    selected = month.value == selectedMonth,
                    onClick = { onSelect(month.value) },
                    label = {
                        Text(
                            text = month.displayName(lang),
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                        )
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("month_option_${month.value}"),
                )
            }
            repeat(MONTH_COLUMNS - months.size) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
