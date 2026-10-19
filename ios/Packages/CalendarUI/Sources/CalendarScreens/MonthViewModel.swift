import CalendarKit

/// The state of the month screen (MR-01): the month being shown, the selected day and the month
/// range the picker and the pager cover.
///
/// The layout and every label are computed here, so the SwiftUI views only draw values and the
/// next/previous, Today-button, picker-range and selection rules are tested without a simulator.
public struct MonthViewModel: Sendable {
  /// The date that gets the today ring and the month the Today button returns to.
  public let today: MonthDate

  /// The month being shown.
  public private(set) var month: MonthKey

  /// The day selected in the shown month, if any.
  public private(set) var selected: MonthDate?

  /// The language of the names.
  public let language: AppLanguage

  /// The resolved Digits setting.
  public let digits: Digits

  /// Whether cells carry their tithi line (MR-01 AC8).
  public let showTithi: Bool

  /// The shown month's layout.
  public private(set) var layout: MonthLayout

  private let todayMonth: MonthKey

  /// Creates the state, opening on today's month unless another month is given.
  /// - Parameters:
  ///   - today: The date that gets the today ring.
  ///   - language: The language of the names.
  ///   - digits: The resolved Digits setting.
  ///   - showTithi: Whether cells carry their tithi line.
  ///   - month: The month to show; today's month by default.
  public init?(
    today: MonthDate,
    language: AppLanguage,
    digits: Digits,
    showTithi: Bool,
    month: MonthKey? = nil
  ) {
    guard let todayMonth = MonthKey(monthOf: today) else { return nil }
    let start = month ?? todayMonth
    guard
      let layout = MonthLayout(
        year: start.year,
        month: start.month,
        showTithi: showTithi,
        today: today
      )
    else { return nil }

    self.today = today
    self.month = start
    self.selected = nil
    self.language = language
    self.digits = digits
    self.showTithi = showTithi
    self.layout = layout
    self.todayMonth = todayMonth
  }

  /// Whether the shown month is not today's month, which is when the Today button shows
  /// (MR-01 AC5).
  public var showsTodayButton: Bool {
    month != todayMonth
  }

  /// Whether a previous month exists inside the picker range.
  public var canGoPrevious: Bool {
    month.id > 0
  }

  /// Whether a next month exists inside the picker range.
  public var canGoNext: Bool {
    month.id < MonthKey.count - 1
  }

  /// The years the picker covers: BS 1901 to 2199 (MR-01 AC6).
  public var pickerYears: ClosedRange<Int> {
    MonthKey.firstYear...MonthKey.lastYear
  }

  /// The month title, for example `Asoj 2083`.
  public var monthTitle: String {
    layout.title(language, digits: digits)
  }

  /// The Gregorian span, for example `Sep – Oct 2026` (MR-01 AC2).
  public var adSpan: String {
    layout.adSpan(language, digits: digits)
  }

  /// The weekday headings, Sunday first.
  public var weekdayNames: [String] {
    MonthLayout.weekdayNames(language)
  }

  /// The cells of the shown month that belong to it.
  public var dayCells: [MonthCell] {
    layout.cells.filter { $0.isInMonth }
  }

  /// Shows another month and drops the selection.
  /// - Parameter key: The month to show.
  public mutating func go(to key: MonthKey) {
    guard key != month else { return }
    guard
      let layout = MonthLayout(
        year: key.year,
        month: key.month,
        showTithi: showTithi,
        today: today
      )
    else { return }
    month = key
    self.layout = layout
    selected = nil
  }

  /// Shows the previous month (MR-01 AC5).
  public mutating func goToPrevious() {
    if let previous = MonthKey(id: month.id - 1) { go(to: previous) }
  }

  /// Shows the next month (MR-01 AC5).
  public mutating func goToNext() {
    if let next = MonthKey(id: month.id + 1) { go(to: next) }
  }

  /// Returns to today's month (MR-01 AC5).
  public mutating func goToToday() {
    go(to: todayMonth)
  }

  /// Whether a cell can be selected. Neighbouring months' cells cannot (MR-01 AC7).
  /// - Parameter cell: The cell.
  /// - Returns: `true` for an in-month cell.
  public func isSelectable(_ cell: MonthCell) -> Bool {
    cell.isSelectable
  }

  /// Selects a cell, unless it belongs to a neighbouring month.
  /// - Parameter cell: The tapped cell.
  public mutating func select(_ cell: MonthCell) {
    guard cell.isSelectable, let date = cell.date else { return }
    selected = date
  }

  /// Selects a date, unless it is not in the shown month.
  /// - Parameter date: The tapped date.
  public mutating func select(_ date: MonthDate) {
    guard layout.cells.contains(where: { $0.isInMonth && $0.date == date }) else { return }
    selected = date
  }

  /// The spoken label of a cell (MR-10 AC1).
  /// - Parameters:
  ///   - cell: The cell.
  ///   - publicHoliday: The localised words for a public holiday.
  /// - Returns: The label.
  public func accessibilityLabel(for cell: MonthCell, publicHoliday: String) -> String {
    layout.accessibilityLabel(
      for: cell,
      language: language,
      digits: digits,
      publicHoliday: publicHoliday
    )
  }

  /// The spoken label of a date in the shown month.
  /// - Parameters:
  ///   - date: The date.
  ///   - publicHoliday: The localised words for a public holiday.
  /// - Returns: The label, or an empty string when the date is not in the shown month.
  public func accessibilityLabel(for date: MonthDate, publicHoliday: String) -> String {
    guard let cell = layout.cells.first(where: { $0.date == date }) else { return "" }
    return accessibilityLabel(for: cell, publicHoliday: publicHoliday)
  }
}
