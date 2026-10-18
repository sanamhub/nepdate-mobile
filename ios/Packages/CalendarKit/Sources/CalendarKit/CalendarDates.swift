import Foundation
import NepDate

/// The dates the app starts from.
public enum CalendarDates {
  /// The time zone the app opens in. MR-06 adds a setting to change it later.
  public static let nepal = TimeZone(identifier: "Asia/Kathmandu") ?? .current

  /// Today's Bikram Sambat date in `timeZone`.
  /// - Parameter timeZone: The zone whose calendar date to return; defaults to Nepal time.
  /// - Returns: The year, month and day of today.
  public static func today(
    in timeZone: TimeZone = CalendarDates.nepal
  ) -> (year: Int, month: Int, day: Int) {
    let date = NepaliDate.today(in: timeZone)
    return (date.year, date.month, date.day)
  }
}
