package np.nepdate.ui.converter

import io.github.sanamhub.nepdate.DateDiff
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.toLocalDate
import io.github.sanamhub.nepdate.toNepaliDate
import np.nepdate.data.Digits
import np.nepdate.ui.common.inDigits
import java.time.LocalDate

/** A converted date and how far it is from today (MR-04 AC2). */
internal data class Conversion(
    val bs: NepaliDate,
    val ad: LocalDate,
    val daysFromToday: Int,
)

/** A second date and the calendar distance from the first (MR-04 AC3). */
internal data class Comparison(
    val from: NepaliDate,
    val to: NepaliDate,
    val diff: DateDiff,
)

/** The converter's state. A failed conversion keeps the last [result]. */
internal data class ConverterState(
    val result: Conversion? = null,
    val error: ConverterError? = null,
    val comparison: Comparison? = null,
    val comparisonError: ConverterError? = null,
)

/** Why a conversion failed, before the UI maps it to a localised line. */
internal sealed interface ConverterError {
    data class InvalidDay(
        val year: Int,
        val month: Int,
        val monthLength: Int,
    ) : ConverterError

    data object InvalidMonth : ConverterError

    data object OutOfRange : ConverterError

    data object Unreadable : ConverterError

    data object NoFirstDate : ConverterError
}

/** BS to AD with the library's lenient parser (MR-04 AC1, AC2). */
internal fun ConverterState.fromBs(
    text: String,
    today: NepaliDate,
): ConverterState =
    try {
        val bs = NepaliDate.parseLenient(text.trim())
        copy(result = conversionOf(bs, today), error = null)
    } catch (exception: NepDateException) {
        copy(error = exception.toConverterError(text))
    }

/** AD to BS through the platform date picker's value (MR-04 AC1, AC2). */
internal fun ConverterState.fromAd(
    date: LocalDate,
    today: NepaliDate,
): ConverterState =
    try {
        val bs = date.toNepaliDate()
        copy(result = conversionOf(bs, today), error = null)
    } catch (exception: NepDateException) {
        copy(error = exception.toConverterError(null))
    }

/**
 * The distance from the current result to a second BS date (MR-04 AC3).
 *
 * @return a state with [ConverterState.comparison] set, or [ConverterError.NoFirstDate] when there
 *   is no result to compare from
 */
internal fun ConverterState.compareWithBs(text: String): ConverterState {
    val first = result ?: return copy(comparisonError = ConverterError.NoFirstDate)
    return try {
        val second = NepaliDate.parseLenient(text.trim())
        copy(
            comparison = Comparison(first.bs, second, first.bs.until(second)),
            comparisonError = null,
        )
    } catch (exception: NepDateException) {
        copy(comparisonError = exception.toConverterError(text))
    }
}

private fun conversionOf(
    bs: NepaliDate,
    today: NepaliDate,
): Conversion =
    Conversion(
        bs = bs,
        ad = bs.toLocalDate(),
        daysFromToday = today.daysUntil(bs).toInt(),
    )

private fun NepDateException.toConverterError(text: String?): ConverterError =
    when (kind) {
        NepDateException.Kind.INVALID_DAY -> {
            val fields = text?.let(::numericFields)
            if (fields == null) {
                ConverterError.Unreadable
            } else {
                ConverterError.InvalidDay(fields.first, fields.second, monthLength)
            }
        }
        NepDateException.Kind.INVALID_MONTH -> ConverterError.InvalidMonth
        NepDateException.Kind.OUT_OF_RANGE -> ConverterError.OutOfRange
        else -> ConverterError.Unreadable
    }

/** The year and month of a numeric BS input, for the "Asoj has 31 days in 2083" line. */
private fun numericFields(text: String): Pair<Int, Int>? =
    NUMERIC_BS.find(text.inDigits(Digits.ENGLISH))?.let { match ->
        val year = match.groupValues[1].toIntOrNull()
        val month = match.groupValues[2].toIntOrNull()
        if (year == null || month == null) null else year to month
    }

private val NUMERIC_BS = Regex("""(\d{4})\D+(\d{1,2})\D+\d{1,2}""")
