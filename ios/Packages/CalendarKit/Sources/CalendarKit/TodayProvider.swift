import Foundation
import NepDate

/// Turns an injected clock into today's Bikram Sambat date in the zone the settings choose
/// (MR-06, I1-01).
public struct TodayProvider: Sendable {
  private let now: @Sendable () -> Date
  private let deviceZone: TimeZone

  /// Creates a provider.
  /// - Parameters:
  ///   - now: The clock; defaults to the current instant.
  ///   - deviceZone: The device's zone, injected so a test can put the device elsewhere.
  public init(now: @escaping @Sendable () -> Date = { Date() }, deviceZone: TimeZone = .current) {
    self.now = now
    self.deviceZone = deviceZone
  }

  /// Today's date in the chosen zone.
  /// - Parameter dateFollows: Nepal time or this device (MR-06 AC1).
  /// - Returns: The Bikram Sambat date at the clock's instant.
  public func today(dateFollows: DateFollows) -> MonthDate {
    let zone = dateFollows == .nepal ? CalendarDates.nepal : deviceZone
    let instant = now()
    if let date = try? NepaliDate(instant, in: zone) {
      return MonthDate(year: date.year, month: date.month, day: date.day)
    }
    // The clock is outside the library's AD 1844 to 2143 range. `NepaliDate.today` returns the
    // nearer bound, so do the same (Foundation.swift in nepdate-swift).
    let bound = instant.timeIntervalSince1970 < 0 ? NepaliDate.min : NepaliDate.max
    return MonthDate(year: bound.year, month: bound.month, day: bound.day)
  }
}
