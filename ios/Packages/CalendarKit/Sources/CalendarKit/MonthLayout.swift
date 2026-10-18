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

  /// The Gregorian year of the first day.
  public let adStartYear: Int

  /// The Gregorian month of the first day, 1 to 12.
  public let adStartMonth: Int

  /// The Gregorian year of the last day.
  public let adEndYear: Int

  /// The Gregorian month of the last day, 1 to 12.
  public let adEndMonth: Int

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
    self.adStartYear = first.gregorian.year
    self.adStartMonth = first.gregorian.month
    self.adEndYear = last.gregorian.year
    self.adEndMonth = last.gregorian.month
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

  /// The month name in the given language.
  /// - Parameter language: The language of the name.
  /// - Returns: For example `Asoj` or `असोज`.
  public func name(_ language: AppLanguage) -> String {
    MonthNames.bs(month, language)
  }

  /// The month title, for example `Asoj 2083` or `असोज २०८३`.
  /// - Parameters:
  ///   - language: The language of the month name.
  ///   - digits: The resolved Digits setting, for the year.
  /// - Returns: The month name and year.
  public func title(_ language: AppLanguage, digits: Digits) -> String {
    "\(name(language)) \(digits.format(year))"
  }

  /// The Gregorian span the month covers, for example `Sep – Oct 2026` (MR-01 AC2).
  /// - Parameters:
  ///   - language: The language of the Gregorian month names.
  ///   - digits: The resolved Digits setting, for the years.
  /// - Returns: One month and year, or the two months and their years.
  public func adSpan(_ language: AppLanguage, digits: Digits) -> String {
    let start = MonthNames.ad(adStartMonth, language, short: true)
    let end = MonthNames.ad(adEndMonth, language, short: true)
    if adStartYear == adEndYear && adStartMonth == adEndMonth {
      return "\(start) \(digits.format(adStartYear))"
    }
    if adStartYear == adEndYear {
      return "\(start) – \(end) \(digits.format(adStartYear))"
    }
    let startYear = digits.format(adStartYear)
    let endYear = digits.format(adEndYear)
    return "\(start) \(startYear) – \(end) \(endYear)"
  }

  /// The spoken label of one cell (MR-10 AC1), for example
  /// `Asoj 25, 2083, Sunday, 11 October 2026, public holiday, Ghatasthapana Brata`.
  ///
  /// A neighbouring month's cell has no holiday or event part, because those marks belong to the
  /// month that owns the day. Only the first event is spoken; the day sheet lists them all.
  /// - Parameters:
  ///   - cell: The cell to describe.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - publicHoliday: The localised words for a public holiday, without a trailing comma.
  /// - Returns: The label, or an empty string for a blank cell.
  public func accessibilityLabel(
    for cell: MonthCell,
    language: AppLanguage,
    digits: Digits,
    publicHoliday: String
  ) -> String {
    guard let date = cell.date, let weekday = cell.weekday else { return "" }
    let bs =
      "\(MonthNames.bs(date.month, language)) \(digits.format(date.day)), "
      + "\(digits.format(date.year))"
    let ad =
      "\(digits.format(cell.adDay)) \(MonthNames.ad(cell.adMonth, language)) "
      + "\(digits.format(cell.adYear))"
    var parts = [bs, weekday.name(language.lang), ad]
    if cell.isHoliday {
      parts.append(publicHoliday)
    }
    if let event = cell.events.first {
      parts.append(digits.applying(to: event.name(language)))
    }
    return parts.joined(separator: ", ")
  }

  /// The weekday column headings, Sunday first.
  /// - Parameter language: The language of the names.
  /// - Returns: Seven short weekday names.
  public static func weekdayNames(_ language: AppLanguage) -> [String] {
    Weekday.allCases.map { $0.shortName(language.lang) }
  }
}
