/// A Bikram Sambat date as the app's own value, so a view or a test can hold and compare one
/// without importing the library.
public struct MonthDate: Sendable, Hashable, Comparable, CustomStringConvertible {
  /// The Bikram Sambat year, 1901 to 2199.
  public let year: Int

  /// The month, 1 to 12.
  public let month: Int

  /// The day of the month.
  public let day: Int

  /// Creates a date.
  /// - Parameters:
  ///   - year: The Bikram Sambat year.
  ///   - month: The month, 1 to 12.
  ///   - day: The day of the month.
  public init(year: Int, month: Int, day: Int) {
    self.year = year
    self.month = month
    self.day = day
  }

  /// The text form, for example `2083-6-20`.
  public var description: String {
    "\(year)-\(month)-\(day)"
  }

  /// Orders two dates by year, then month, then day.
  /// - Parameters:
  ///   - lhs: The left date.
  ///   - rhs: The right date.
  /// - Returns: `true` when `lhs` is earlier.
  public static func < (lhs: MonthDate, rhs: MonthDate) -> Bool {
    (lhs.year, lhs.month, lhs.day) < (rhs.year, rhs.month, rhs.day)
  }
}
