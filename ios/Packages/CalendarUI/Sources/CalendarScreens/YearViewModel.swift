import CalendarKit
import Foundation

/// One of the twelve mini months of the year view (MR-14).
public struct YearMonthSummary: Sendable, Hashable, Identifiable {
  /// The month.
  public let key: MonthKey

  /// The month name, for example `Asoj`.
  public let name: String

  /// The month's grid, Sunday first; neighbouring days are left blank in the view.
  public let cells: [MonthCell]

  /// The number of public holidays in the month.
  public let holidayCount: Int

  /// The whole spoken label, for example `Asoj 2083, 3 public holidays` (MR-14 AC4).
  public let spokenLabel: String

  /// The identity of the month.
  public var id: Int {
    key.id
  }
}

/// The year view's state (MR-14): twelve mini months, the holiday count of each, the year title
/// and the two year buttons with their limits.
public struct YearViewModel: Sendable {
  /// The Bikram Sambat year.
  public let year: Int

  /// The year in the resolved Digits setting.
  public let title: String

  /// The twelve months, Baisakh first.
  public let months: [YearMonthSummary]

  /// The date that gets the today mark.
  public let today: MonthDate

  /// The language of the names.
  public let language: AppLanguage

  /// The resolved Digits setting.
  public let digits: Digits

  /// Creates the year.
  /// - Parameters:
  ///   - year: The Bikram Sambat year, 1901 to 2199.
  ///   - today: The date that gets the today mark.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  public init?(
    year: Int,
    today: MonthDate,
    language: AppLanguage,
    digits: Digits
  ) {
    guard (MonthKey.firstYear...MonthKey.lastYear).contains(year) else { return nil }
    var months: [YearMonthSummary] = []
    months.reserveCapacity(12)
    for month in 1...12 {
      guard let key = MonthKey(year: year, month: month) else { return nil }
      guard let layout = MonthLayout(year: year, month: month, showTithi: false, today: today)
      else { return nil }
      let count = layout.holidayDays.count
      let name = MonthNames.bs(month, language)
      let countText =
        count == 1
        ? AppText.string("public holiday", language)
        : AppText.string("public holidays", language)
      let spoken = "\(name) \(digits.format(year)), \(digits.format(count)) \(countText)"
      months.append(
        YearMonthSummary(
          key: key,
          name: name,
          cells: layout.cells,
          holidayCount: count,
          spokenLabel: spoken
        )
      )
    }
    self.year = year
    self.title = digits.format(year)
    self.months = months
    self.today = today
    self.language = language
    self.digits = digits
  }

  /// Whether the year before this one is in range (MR-14 AC2).
  public var canGoPrevious: Bool {
    year > MonthKey.firstYear
  }

  /// Whether the year after this one is in range (MR-14 AC2).
  public var canGoNext: Bool {
    year < MonthKey.lastYear
  }

  /// The year before this one, or `nil` at the range's start.
  public var previous: YearViewModel? {
    guard canGoPrevious else { return nil }
    return YearViewModel(year: year - 1, today: today, language: language, digits: digits)
  }

  /// The year after this one, or `nil` at the range's end.
  public var next: YearViewModel? {
    guard canGoNext else { return nil }
    return YearViewModel(year: year + 1, today: today, language: language, digits: digits)
  }
}
