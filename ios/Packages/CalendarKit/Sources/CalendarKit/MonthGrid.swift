import Foundation
import NepDate
import NepDatePatro

/// The layout of one Bikram Sambat month, ready to draw.
public struct MonthGrid: Sendable, Hashable {
  /// The Bikram Sambat year.
  public let year: Int

  /// The Bikram Sambat month, 1 to 12.
  public let month: Int

  /// The number of days in the month, 29 to 32.
  public let dayCount: Int

  /// The month title, for example `असोज २०८३`.
  public let title: String

  /// The weekday column headings, Sunday first.
  public let weekdayNames: [String]

  /// The number of empty cells before the first day.
  public let leadingBlanks: Int

  /// The cells of the grid, padded with `nil` so that every row has seven cells.
  public let cells: [MonthDay?]

  /// Creates the grid for a Bikram Sambat month.
  ///
  /// The grid always ends with a complete week: `cells.count` is a multiple of seven.
  /// - Parameters:
  ///   - year: The Bikram Sambat year, 1901 to 2199.
  ///   - month: The month, 1 to 12.
  ///   - language: The language of names and digits.
  ///   - today: The date to ring, or `nil` to ring nothing.
  /// - Returns: `nil` when the year or month is outside the library's range.
  public init?(
    year: Int,
    month: Int,
    language: AppLanguage,
    today: (year: Int, month: Int, day: Int)? = nil
  ) {
    guard let range = try? NepaliDateRange.month(year: year, month: month) else { return nil }
    let first = range.lowerBound
    self.year = year
    self.month = month
    self.dayCount = range.count
    self.leadingBlanks = first.weekday.rawValue
    self.title = Self.title(for: first, language: language)
    self.weekdayNames = Weekday.allCases.map { $0.shortName(language.lang) }

    var gridCells: [MonthDay?] = Array(repeating: nil, count: leadingBlanks)
    for date in range {
      gridCells.append(MonthDay(date: date, today: today))
    }
    while gridCells.count % 7 != 0 {
      gridCells.append(nil)
    }
    self.cells = gridCells
  }

  private static func title(for first: NepaliDate, language: AppLanguage) -> String {
    let name = first.bsMonth.name(language.lang)
    return language == .nepali ? "\(name) \(nepaliDigits(first.year))" : "\(name) \(first.year)"
  }
}
