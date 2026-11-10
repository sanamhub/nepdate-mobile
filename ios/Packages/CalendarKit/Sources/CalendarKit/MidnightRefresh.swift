import Foundation

/// When the app should read the clock again (MR-01, MR-05 AC1).
///
/// A screen that shows today has to change at local midnight even when nobody touches the app.
/// The seconds are computed with a Gregorian `Calendar` in the chosen zone, by adding one day to
/// the start of the day, never by adding 86,400 seconds, so a daylight-saving change cannot make
/// the wait an hour wrong.
public enum MidnightRefresh {
  /// The number of seconds from an instant to the next local midnight.
  /// - Parameters:
  ///   - now: The instant to measure from.
  ///   - zone: The zone whose midnight matters: the device's, or `Asia/Kathmandu`.
  /// - Returns: A positive number of seconds, at most one day.
  public static func secondsUntilNextMidnight(now: Date, zone: TimeZone) -> TimeInterval {
    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = zone
    let start = calendar.startOfDay(for: now)
    guard let next = calendar.date(byAdding: .day, value: 1, to: start) else {
      return 24 * 60 * 60
    }
    return max(1, next.timeIntervalSince(now))
  }

  /// The zone the settings choose for today (MR-06 AC1).
  /// - Parameter dateFollows: The Date follows setting.
  /// - Returns: Nepal time or the device's zone.
  public static func zone(for dateFollows: DateFollows) -> TimeZone {
    dateFollows == .nepal ? CalendarDates.nepal : .current
  }
}
