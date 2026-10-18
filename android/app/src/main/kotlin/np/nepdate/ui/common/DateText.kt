package np.nepdate.ui.common

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import io.github.sanamhub.nepdate.toLocalDate
import np.nepdate.data.Digits
import java.time.LocalDate
import java.util.Locale

/** The library language for an Android locale; Nepali is the only translated language in v1. */
internal fun languageFor(locale: Locale): Lang = if (locale.language == "ne") Lang.NEPALI else Lang.ENGLISH

/** Rewrites the ASCII or Devanagari digits in [this] to the chosen setting. */
internal fun String.inDigits(digits: Digits): String =
    when (digits) {
        Digits.ENGLISH -> map { if (it in '०'..'९') '0' + (it - '०') else it }.joinToString("")
        Digits.NEPALI -> map { if (it in '0'..'9') '०' + (it - '0') else it }.joinToString("")
    }

/**
 * The long BS date with the product's month name, for example `Asoj 25, 2083`.
 *
 * The English month names come from resources because the requirement and design system write
 * `Asoj`, `Asar` and `Baisakh`, while the library writes `Ashoj`, `Ashad` and `Baishakh`. The date
 * itself, including its digits, still comes from [date].
 */
internal fun bsLong(
    date: NepaliDate,
    monthName: String,
    digits: Digits,
): String {
    val day = date.dayOfMonth.toString().inDigits(digits)
    val year = date.year.toString().inDigits(digits)
    return "$monthName $day, $year"
}

/**
 * The long BS date in Devanagari, for the second line when the app language is English (MR-02 AC1).
 * The line exists to show the Nepali script, so it keeps Devanagari digits whatever the Digits
 * setting is: `असोज ९, २०८३`, never `असोज 9, 2083`.
 */
internal fun bsLongNepali(date: NepaliDate): String =
    date
        .toLongString(weekday = false, year = true, pad = false, lang = Lang.NEPALI)
        .inDigits(Digits.NEPALI)

/** The BS month and year with the chosen digits, for example `Asoj 2083`. */
internal fun monthTitle(
    monthName: String,
    year: Int,
    digits: Digits,
): String = "$monthName ${year.toString().inDigits(digits)}"

/** The AD date in [lang]'s month names, with the chosen digits, for example `11 October 2026`. */
internal fun adDate(
    date: NepaliDate,
    monthNames: List<String>,
    digits: Digits,
): String = adDate(date.toLocalDate(), monthNames, digits)

/** The AD date in [lang]'s month names, with the chosen digits, for example `11 October 2026`. */
internal fun adDate(
    date: LocalDate,
    monthNames: List<String>,
    digits: Digits,
): String {
    val day = date.dayOfMonth.toString().inDigits(digits)
    val year = date.year.toString().inDigits(digits)
    return "$day ${monthNames[date.monthValue - 1]} $year"
}

/** The two AD months a BS month spans, already formatted for the header. */
internal data class AdSpan(
    val startMonth: String,
    val startYear: String,
    val endMonth: String,
    val endYear: String,
    val isSameMonth: Boolean,
    val isSameYear: Boolean,
)

/** Formats the AD span of a BS month from its first and last day (MR-01 AC2). */
internal fun adSpan(
    first: NepaliDate,
    last: NepaliDate,
    monthNames: List<String>,
    digits: Digits,
): AdSpan =
    AdSpan(
        startMonth = monthNames[first.gregorianMonth - 1],
        startYear = first.gregorianYear.toString().inDigits(digits),
        endMonth = monthNames[last.gregorianMonth - 1],
        endYear = last.gregorianYear.toString().inDigits(digits),
        isSameMonth = first.gregorianYear == last.gregorianYear && first.gregorianMonth == last.gregorianMonth,
        isSameYear = first.gregorianYear == last.gregorianYear,
    )

/** The weekday name in [lang], for example `Sunday` or `आइतवार`. */
internal fun weekdayName(
    date: NepaliDate,
    lang: Lang,
): String = date.dayOfWeek.displayName(lang)

/** The seven weekday headers, Sunday first; Saturday is the weekly holiday in Nepal. */
internal fun weekdayHeaders(lang: Lang): List<String> = Weekday.entries.map { it.shortName(lang) }
