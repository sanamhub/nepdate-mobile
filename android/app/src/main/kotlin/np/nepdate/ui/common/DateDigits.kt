package np.nepdate.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * The digits of a typed date, at most eight (YYYYMMDD). Devanagari digits from a Nepali keyboard
 * count as their ASCII twins, and every other character is dropped, so a pasted "2083-07-22" works.
 */
internal fun dateDigits(input: String): String =
    input
        .map { if (it in '०'..'९') '0' + (it - '०') else it }
        .filter { it in '0'..'9' }
        .take(DATE_DIGITS)
        .joinToString("")

/** [digits] with the slashes of YYYY/MM/DD, added as soon as the part before them is complete. */
internal fun withDateSlashes(digits: String): String =
    buildString {
        digits.forEachIndexed { index, char ->
            if (index == YEAR_DIGITS || index == YEAR_DIGITS + MONTH_DIGITS) append('/')
            append(char)
        }
    }

/**
 * Shows the stored digits as YYYY/MM/DD. The field keeps the digits only, so backspace always
 * removes a digit and never gets stuck on a slash, and the cursor maps across the slashes.
 */
internal object DateSlashTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val shown = withDateSlashes(text.text)
        val mapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int =
                    offset +
                        (if (offset > YEAR_DIGITS) 1 else 0) +
                        (if (offset > YEAR_DIGITS + MONTH_DIGITS) 1 else 0)

                override fun transformedToOriginal(offset: Int): Int =
                    (
                        offset -
                            (if (offset > YEAR_DIGITS) 1 else 0) -
                            (if (offset > YEAR_DIGITS + MONTH_DIGITS + 1) 1 else 0)
                    ).coerceIn(0, text.length)
            }
        return TransformedText(AnnotatedString(shown), mapping)
    }
}

internal const val DATE_DIGITS = 8
private const val YEAR_DIGITS = 4
private const val MONTH_DIGITS = 2
