import CalendarKit
import Foundation
import NepDate

/// A converted BS date and how far it is from today (MR-04 AC2).
public struct Conversion: Sendable, Hashable {
  /// The BS date.
  public let bs: MonthDate

  /// The AD year.
  public let adYear: Int

  /// The AD month, 1 to 12.
  public let adMonth: Int

  /// The AD day.
  public let adDay: Int

  /// Whole days from today; negative for a past date.
  public let daysFromToday: Int
}

/// A second BS date and its distance from the first (MR-04 AC3).
public struct Comparison: Sendable, Hashable {
  /// The first date.
  public let from: MonthDate

  /// The second date.
  public let to: MonthDate

  /// Whole years after the whole months.
  public let years: Int

  /// Whole months after the years.
  public let months: Int

  /// Days after the months.
  public let days: Int
}

/// Why a conversion failed, before the view maps it to a localised line.
public enum ConverterError: Sendable, Hashable {
  /// The day does not exist in that month.
  case invalidDay(year: Int, month: Int, monthLength: Int)

  /// The month is not 1 to 12.
  case invalidMonth

  /// The date is outside the library's range.
  case outOfRange

  /// The text could not be read as a date.
  case unreadable

  /// There is no first result to compare from.
  case noFirstDate
}

/// The converter's state (MR-04). A failed conversion keeps the last ``result``.
public struct ConverterViewModel: Sendable {
  /// The converted date, if one has succeeded.
  public private(set) var result: Conversion?

  /// The last conversion error, if any.
  public private(set) var error: ConverterError?

  /// The last successful comparison.
  public private(set) var comparison: Comparison?

  /// The last comparison error, if any.
  public private(set) var comparisonError: ConverterError?

  /// The date "N days from today" counts from.
  public let today: MonthDate

  /// The language of the names and messages.
  public let language: AppLanguage

  /// The resolved Digits setting.
  public let digits: Digits

  /// Creates an empty converter.
  /// - Parameters:
  ///   - today: The date distances count from.
  ///   - language: The language of the names and messages.
  ///   - digits: The resolved Digits setting.
  public init(today: MonthDate, language: AppLanguage, digits: Digits) {
    self.today = today
    self.language = language
    self.digits = digits
  }

  /// Converts BS text with the library's lenient parser (MR-04 AC1 and AC2).
  /// - Parameter text: Nepali or English digits, with a month name or numeric fields.
  public mutating func fromBs(_ text: String) {
    do {
      let date = try NepaliDate.parseLenient(text.trimmingCharacters(in: .whitespacesAndNewlines))
      result = conversion(date)
      error = nil
    } catch {
      self.error = Self.converterError(error, text: text)
    }
  }

  /// Converts an AD date from the platform date picker (MR-04 AC1 and AC2).
  /// - Parameters:
  ///   - year: The AD year.
  ///   - month: The AD month, 1 to 12.
  ///   - day: The AD day.
  public mutating func fromAd(year: Int, month: Int, day: Int) {
    do {
      let date = try NepaliDate(gregorianYear: year, month: month, day: day)
      result = conversion(date)
      error = nil
    } catch {
      self.error = Self.converterError(error, text: nil)
    }
  }

  /// Compares a second BS date with the current result (MR-04 AC3).
  /// - Parameter text: The second date, in the same forms as ``fromBs(_:)``.
  public mutating func compareWithBs(_ text: String) {
    guard let first = result else {
      comparisonError = .noFirstDate
      return
    }
    do {
      let second = try NepaliDate.parseLenient(
        text.trimmingCharacters(in: .whitespacesAndNewlines)
      )
      let firstDate = try NepaliDate(
        year: first.bs.year,
        month: first.bs.month,
        day: first.bs.day
      )
      let diff = firstDate.diff(to: second)
      comparison = Comparison(
        from: first.bs,
        to: MonthDate(year: second.year, month: second.month, day: second.day),
        years: diff.years,
        months: diff.months,
        days: diff.days
      )
      comparisonError = nil
    } catch {
      comparisonError = Self.converterError(error, text: text)
    }
  }

  /// The long BS result, for example `Asoj 20, 2083`.
  public var resultBsDate: String? {
    guard let result else { return nil }
    return "\(MonthNames.bs(result.bs.month, language)) \(digits.format(result.bs.day)), "
      + "\(digits.format(result.bs.year))"
  }

  /// The weekday and AD result, for example `Tuesday, 6 October 2026`.
  public var resultWeekdayAndDate: String? {
    guard let result else { return nil }
    guard
      let date = try? NepaliDate(
        year: result.bs.year,
        month: result.bs.month,
        day: result.bs.day
      )
    else { return nil }
    let day = digits.format(result.adDay)
    let month = MonthNames.ad(result.adMonth, language)
    let year = digits.format(result.adYear)
    return "\(date.weekday.name(language.nepDateLang)), \(day) \(month) \(year)"
  }

  /// "Today", "Tomorrow", "5 days from today" or "2 days before today".
  public var daysFromTodayText: String? {
    guard let result else { return nil }
    return Self.distanceText(result.daysFromToday, language: language, digits: digits)
  }

  /// The localised conversion error.
  public var errorText: String? {
    guard let error else { return nil }
    return text(for: error)
  }

  /// The two date ends of the comparison.
  public var comparisonRange: String? {
    guard let comparison else { return nil }
    let from =
      "\(MonthNames.bs(comparison.from.month, language)) "
      + "\(digits.format(comparison.from.day)), \(digits.format(comparison.from.year))"
    let to =
      "\(MonthNames.bs(comparison.to.month, language)) "
      + "\(digits.format(comparison.to.day)), \(digits.format(comparison.to.year))"
    return AppText.string("\(from) to \(to)", language)
  }

  /// The difference, for example `1 year, 0 months, 0 days`.
  public var differenceText: String? {
    guard let comparison else { return nil }
    let years = Self.unitText(
      comparison.years,
      one: "year",
      many: "years",
      language: language,
      digits: digits
    )
    let months = Self.unitText(
      comparison.months,
      one: "month",
      many: "months",
      language: language,
      digits: digits
    )
    let days = Self.unitText(
      comparison.days,
      one: "day",
      many: "days",
      language: language,
      digits: digits
    )
    return [years, months, days].joined(separator: ", ")
  }

  /// The localised comparison error.
  public var comparisonErrorText: String? {
    guard let comparisonError else { return nil }
    return text(for: comparisonError)
  }

  private func conversion(_ date: NepaliDate) -> Conversion {
    let gregorian = date.gregorian
    let todayDate = try? NepaliDate(
      year: today.year,
      month: today.month,
      day: today.day
    )
    return Conversion(
      bs: MonthDate(year: date.year, month: date.month, day: date.day),
      adYear: gregorian.year,
      adMonth: gregorian.month,
      adDay: gregorian.day,
      daysFromToday: todayDate?.days(until: date) ?? 0
    )
  }

  private func text(for error: ConverterError) -> String {
    switch error {
    case .invalidDay(let year, let month, let monthLength):
      let monthName = MonthNames.bs(month, language)
      return AppText.string(
        "\(monthName) has \(digits.format(monthLength)) days in \(digits.format(year))",
        language
      )
    case .invalidMonth:
      return AppText.string("Month must be 1 to 12", language)
    case .outOfRange:
      return AppText.string("That date is outside the BS range", language)
    case .unreadable:
      return AppText.string("Enter a BS date like 2083-06-20", language)
    case .noFirstDate:
      return AppText.string("Convert a date before comparing", language)
    }
  }

  private static func converterError(_ error: NepDateError, text: String?) -> ConverterError {
    switch error.kind {
    case .invalidDay(let monthLength):
      guard let text, let date = invalidDayDate(in: text) else { return .unreadable }
      return .invalidDay(year: date.year, month: date.month, monthLength: monthLength)
    case .invalidMonth:
      return .invalidMonth
    case .outOfRange:
      return .outOfRange
    default:
      return .unreadable
    }
  }

  /// Finds the year and month of a date whose day failed validation. A month name gives no month
  /// number, so each number is replaced by a valid day in turn and the parser reads the fields.
  private static func invalidDayDate(in text: String) -> NepaliDate? {
    let normalized = Digits.english.applying(to: text)
    guard let regex = try? NSRegularExpression(pattern: #"\d+"#) else { return nil }
    let range = NSRange(normalized.startIndex..<normalized.endIndex, in: normalized)
    for match in regex.matches(in: normalized, range: range) {
      guard let numberRange = Range(match.range, in: normalized) else { continue }
      var candidate = normalized
      candidate.replaceSubrange(numberRange, with: "1")
      if let date = try? NepaliDate.parseLenient(candidate) {
        return date
      }
    }
    return nil
  }

  private static func distanceText(
    _ days: Int,
    language: AppLanguage,
    digits: Digits
  ) -> String {
    switch days {
    case 0:
      return AppText.string("Today", language)
    case 1:
      return AppText.string("Tomorrow", language)
    case 2...:
      return AppText.string("\(digits.format(days)) days from today", language)
    case -1:
      return AppText.string("\(digits.format(1)) day before today", language)
    default:
      return AppText.string("\(digits.format(-days)) days before today", language)
    }
  }

  private static func unitText(
    _ count: Int,
    one: String.LocalizationValue,
    many: String.LocalizationValue,
    language: AppLanguage,
    digits: Digits
  ) -> String {
    let word = AppText.string(count == 1 ? one : many, language)
    return "\(digits.format(count)) \(word)"
  }
}
