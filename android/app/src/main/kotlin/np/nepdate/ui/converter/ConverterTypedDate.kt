package np.nepdate.ui.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toLocalDate
import io.github.sanamhub.nepdate.toNepaliDate
import np.nepdate.R
import np.nepdate.ui.common.DATE_DIGITS
import np.nepdate.ui.common.DateSlashTransformation
import np.nepdate.ui.common.SymbolIcon
import np.nepdate.ui.common.dateDigits
import np.nepdate.ui.common.withDateSlashes
import java.time.DateTimeException
import java.time.LocalDate

/**
 * The typing row at the top of the date card: the other way to set the wheel under it. It shows the
 * wheel's date as a hint, takes digits only and adds the slashes itself (20830722 shows as
 * 2083/07/22); once the eight digits form a real date in the direction's calendar, the wheel
 * jumps to it through [onDate].
 */
@Composable
internal fun TypedDateField(
    direction: ConvertDirection,
    current: NepaliDate,
    onDate: (NepaliDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var digits by rememberSaveable(direction) { mutableStateOf("") }
    val complete = digits.length == DATE_DIGITS
    val invalid = complete && typedDate(digits, direction) == null
    val label =
        stringResource(
            if (direction ==
                ConvertDirection.BS_TO_AD
            ) {
                R.string.converter_type_bs
            } else {
                R.string.converter_type_ad
            },
        )
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.titleMedium.copy(color = if (invalid) colors.error else colors.onSurface)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FIELD_HEIGHT)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SymbolIcon(icon = R.drawable.ic_sym_event, tint = colors.onSurfaceVariant, size = 20.dp)
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (digits.isEmpty()) {
                // The wheel's own date as the hint: typing replaces it.
                Text(
                    text = withDateSlashes(currentDigits(current, direction)),
                    style = textStyle.copy(color = colors.onSurfaceVariant.copy(alpha = HINT_ALPHA)),
                )
            }
            BasicTextField(
                value = digits,
                onValueChange = { typed ->
                    val next = dateDigits(typed)
                    if (next != digits) {
                        digits = next
                        if (next.length == DATE_DIGITS) typedDate(next, direction)?.let(onDate)
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = label }
                        .testTag("converter_typed_date"),
                textStyle = textStyle,
                singleLine = true,
                cursorBrush = SolidColor(colors.primary),
                visualTransformation = DateSlashTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
        }
        FieldHint(invalid)
    }
}

/** "Type or turn" at the end of the row, or "Not a date" in the error colour. */
@Composable
private fun FieldHint(invalid: Boolean) {
    Text(
        text = stringResource(if (invalid) R.string.converter_type_invalid else R.string.converter_type_hint),
        color = if (invalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
    )
}

/** [date] as YYYYMMDD digits in the calendar the direction reads from. */
private fun currentDigits(
    date: NepaliDate,
    direction: ConvertDirection,
): String =
    when (direction) {
        ConvertDirection.BS_TO_AD -> "%04d%02d%02d".format(date.year, date.monthValue, date.dayOfMonth)
        ConvertDirection.AD_TO_BS ->
            date.toLocalDate().let { "%04d%02d%02d".format(it.year, it.monthValue, it.dayOfMonth) }
    }

/** Eight digits read as a BS or an AD date, or null when they name no date the data covers. */
internal fun typedDate(
    digits: String,
    direction: ConvertDirection,
): NepaliDate? {
    val year = digits.substring(0, YEAR_END).toInt()
    val month = digits.substring(YEAR_END, MONTH_END).toInt()
    val day = digits.substring(MONTH_END).toInt()
    return when (direction) {
        ConvertDirection.BS_TO_AD -> NepaliDate.ofOrNull(year, month, day)
        ConvertDirection.AD_TO_BS ->
            try {
                LocalDate.of(year, month, day).toNepaliDate()
            } catch (_: DateTimeException) {
                null
            } catch (_: NepDateException) {
                null
            }
    }
}

private val FIELD_HEIGHT = 52.dp
private const val HINT_ALPHA = 0.6f
private const val YEAR_END = 4
private const val MONTH_END = 6
