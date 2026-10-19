import CalendarKit

/// A Bikram Sambat year and month, used as one identity while paging (MR-01 AC5, AC6).
public struct MonthKey: Hashable, Sendable, Comparable, Identifiable, CustomStringConvertible {
  /// The first year the picker covers, BS 1901.
  public static let firstYear = 1901

  /// The last year the picker covers, BS 2199.
  public static let lastYear = 2199

  /// The number of months from BS 1901-01 to 2199-12: 3588.
  public static let count = (lastYear - firstYear + 1) * 12

  /// The Bikram Sambat year.
  public let year: Int

  /// The month, 1 to 12.
  public let month: Int

  /// The position in the pager, 0 for 1901-01 and 3587 for 2199-12.
  public var id: Int {
    (year - Self.firstYear) * 12 + month - 1
  }

  /// Creates a key.
  /// - Parameters:
  ///   - year: The year, 1901 to 2199.
  ///   - month: The month, 1 to 12.
  public init?(year: Int, month: Int) {
    guard (Self.firstYear...Self.lastYear).contains(year), (1...12).contains(month) else {
      return nil
    }
    self.year = year
    self.month = month
  }

  /// Creates a key from its position in the pager.
  /// - Parameter id: The position, 0 to 3587.
  public init?(id: Int) {
    guard (0..<Self.count).contains(id) else { return nil }
    self.year = Self.firstYear + id / 12
    self.month = id % 12 + 1
  }

  /// Creates the key of the month that contains a date.
  /// - Parameter date: The date.
  public init?(monthOf date: MonthDate) {
    self.init(year: date.year, month: date.month)
  }

  /// The first day of the month.
  public var firstDay: MonthDate {
    MonthDate(year: year, month: month, day: 1)
  }

  /// Every key the pager shows, in order.
  public static let all: [MonthKey] = (0..<count).compactMap { MonthKey(id: $0) }

  /// Orders two keys by their pager position, earliest first.
  /// - Parameters:
  ///   - lhs: The left key.
  ///   - rhs: The right key.
  /// - Returns: `true` when `lhs` is the earlier month.
  public static func < (lhs: MonthKey, rhs: MonthKey) -> Bool {
    lhs.id < rhs.id
  }

  /// The text form, for example `2083-6`.
  public var description: String {
    "\(year)-\(month)"
  }
}
