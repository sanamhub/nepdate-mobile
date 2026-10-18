import NepDate

/// The layout of one Bikram Sambat month: five or six weeks of cells, Sunday first (A1-02,
/// MR-01 AC7).
///
/// No calendar arithmetic happens here. The month length, the weekday and the neighbouring dates
/// all come from the library.
public struct MonthLayout: Sendable, Hashable {
  /// The Bikram Sambat year.
  public let year: Int

  /// The month, 1 to 12.
  public let month: Int

  /// The first day of the month.
  public let firstDay: MonthDate

  /// The last day of the month.
  public let lastDay: MonthDate

  /// The number of cells before the first day.
  public let leadingDays: Int

  /// The cells of the grid: 35 or 42, Sunday first.
  public let cells: [MonthCell]

  private let englishName: String
  private let nepaliName: String

  /// Lays out a month from the library's calendar and patro data.
  /// - Parameters:
  ///   - year: The Bikram Sambat year, 1901 to 2199.
  ///   - month: The month, 1 to 12.
  ///   - showTithi: Whether a cell carries its tithi (MR-01 AC8).
  ///   - today: The date that gets the today ring.
  public init?(year: Int, month: Int, showTithi: Bool, today: MonthDate) {
    guard let first = try? NepaliDate(year: year, month: month, day: 1) else { return nil }
    let last = first.lastDayOfMonth
    let leading = first.weekday.rawValue
    let cellCount = leading + last.day <= 35 ? 35 : 42

    var gridCells: [MonthCell] = []
    gridCells.reserveCapacity(cellCount)
    for index in 0..<cellCount {
      let offset = index - leading
      if offset < 0 {
        let date = try? first.adding(days: offset)
        gridCells.append(
          MonthCell(date: date, inMonth: false, showTithi: showTithi, today: today)
        )
      } else if offset < last.day {
        let date = try? first.adding(days: offset)
        gridCells.append(
          MonthCell(date: date, inMonth: true, showTithi: showTithi, today: today)
        )
      } else {
        let date = try? last.adding(days: offset - last.day + 1)
        gridCells.append(
          MonthCell(date: date, inMonth: false, showTithi: showTithi, today: today)
        )
      }
    }

    self.year = year
    self.month = month
    self.firstDay = MonthDate(year: first.year, month: first.month, day: first.day)
    self.lastDay = MonthDate(year: last.year, month: last.month, day: last.day)
    self.leadingDays = leading
    self.cells = gridCells
    self.englishName = first.bsMonth.name(.english)
    self.nepaliName = first.bsMonth.name(.nepali)
  }

  /// The number of week rows: 5 or 6.
  public var rowCount: Int {
    cells.count / 7
  }

  /// The number of days in the month, 29 to 32.
  public var dayCount: Int {
    lastDay.day
  }

  /// The BS day numbers of this month's public holidays, in order.
  public var holidayDays: [Int] {
    cells.filter { $0.isInMonth && $0.isHoliday }.map(\.bsDay)
  }

  /// The month title in the given language, for example `असोज २०८३`.
  /// - Parameter language: The language of the name and the digits.
  /// - Returns: The month name and year.
  public func title(_ language: AppLanguage) -> String {
    let name = language == .nepali ? nepaliName : englishName
    return language == .nepali ? "\(name) \(nepaliDigits(year))" : "\(name) \(year)"
  }

  /// The weekday column headings, Sunday first.
  /// - Parameter language: The language of the names.
  /// - Returns: Seven short weekday names.
  public static func weekdayNames(_ language: AppLanguage) -> [String] {
    Weekday.allCases.map { $0.shortName(language.lang) }
  }
}
