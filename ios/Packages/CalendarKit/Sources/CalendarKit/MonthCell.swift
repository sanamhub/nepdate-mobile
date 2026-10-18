import NepDate
import NepDatePatro

/// One cell of a month grid: an in-month day, a dimmed neighbouring day, or a blank cell at the
/// edge of the library's range.
public struct MonthCell: Sendable, Hashable {
  /// The date, or `nil` at the edge of the library's range.
  public let date: MonthDate?

  /// The Bikram Sambat day of the month; 0 when ``date`` is `nil`.
  public let bsDay: Int

  /// The Gregorian day of the month; 0 when ``date`` is `nil`.
  public let adDay: Int

  /// The Gregorian year; 0 when ``date`` is `nil`.
  public let adYear: Int

  /// The Gregorian month, 1 to 12; 0 when ``date`` is `nil`.
  public let adMonth: Int

  /// The weekday; `nil` when ``date`` is `nil`.
  public let weekday: Weekday?

  /// Whether the date is a Saturday, the holiday column.
  public let isSaturday: Bool

  /// Whether the date is a public holiday. Always false outside the shown month.
  public let isHoliday: Bool

  /// Whether the date is today, including when it belongs to a neighbouring month.
  public let isToday: Bool

  /// Whether the date belongs to the month the grid was built for.
  public let isInMonth: Bool

  /// The tithi, when the grid was built with tithi on and the data has one.
  public let tithi: NamedText?

  /// The events of the day in file order. Empty outside the shown month.
  public let events: [NamedText]

  /// Whether the event dot shows.
  public var hasEvents: Bool {
    !events.isEmpty
  }

  /// Whether tapping the cell selects its date (MR-01 AC7: neighbouring days are not tappable).
  public var isSelectable: Bool {
    isInMonth && date != nil
  }

  init(date: NepaliDate?, inMonth: Bool, showTithi: Bool, today: MonthDate) {
    guard let date else {
      self.date = nil
      self.bsDay = 0
      self.adDay = 0
      self.adYear = 0
      self.adMonth = 0
      self.weekday = nil
      self.isSaturday = false
      self.isHoliday = false
      self.isToday = false
      self.isInMonth = false
      self.tithi = nil
      self.events = []
      return
    }
    let info = inMonth ? date.calendarInfo : nil
    let monthDate = MonthDate(year: date.year, month: date.month, day: date.day)
    self.date = monthDate
    self.bsDay = date.day
    self.adDay = date.gregorian.day
    self.adYear = date.gregorian.year
    self.adMonth = date.gregorian.month
    self.weekday = date.weekday
    self.isSaturday = date.weekday == .saturday
    self.isHoliday = info?.isPublicHoliday ?? false
    self.isToday = monthDate == today
    self.isInMonth = inMonth
    self.tithi = showTithi ? info?.tithi.map(NamedText.init) : nil
    self.events = info.map { $0.events.map(NamedText.init) } ?? []
  }

  /// The BS day number.
  /// - Parameter digits: The resolved Digits setting.
  /// - Returns: The day in those digits.
  public func bsNumber(_ digits: Digits) -> String {
    digits.format(bsDay)
  }

  /// The AD day number.
  /// - Parameter digits: The resolved Digits setting.
  /// - Returns: The day in those digits.
  public func adNumber(_ digits: Digits) -> String {
    digits.format(adDay)
  }
}
