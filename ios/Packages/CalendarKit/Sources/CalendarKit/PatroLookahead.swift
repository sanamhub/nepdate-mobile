import NepDate
import NepDatePatro

/// The next public holiday after a day, inside a look-ahead window (MR-22 AC2).
public struct HolidayCountdown: Sendable, Hashable {
  /// The holiday's date.
  public let date: MonthDate

  /// The holiday's name, when the data names an event on it.
  public let name: NamedText?

  /// Whole days from the day the countdown started on; always at least 1.
  public let days: Int
}

/// Looks forward for the next public holiday.
public enum PatroLookahead {
  /// The next public holiday after a date.
  ///
  /// The scan starts the day after `date` and stops at the end of the window or the end of the
  /// patro coverage, whichever comes first.
  /// - Parameters:
  ///   - date: The day to look after.
  ///   - withinDays: The size of the window in days; 60 by default, as on Android.
  /// - Returns: The holiday and its distance, or `nil` when the window has none.
  public static func nextHoliday(
    after date: MonthDate,
    withinDays: Int = 60
  ) -> HolidayCountdown? {
    guard withinDays > 0 else { return nil }
    guard let start = try? NepaliDate(year: date.year, month: date.month, day: date.day)
    else { return nil }
    guard let windowEnd = try? start.adding(days: withinDays) else { return nil }
    let end = min(windowEnd, Patro.coverage.upperBound)
    var cursor = start
    while cursor < end {
      guard let next = try? cursor.adding(days: 1) else { return nil }
      cursor = next
      let info = cursor.calendarInfo
      if info.isPublicHoliday {
        return HolidayCountdown(
          date: MonthDate(year: cursor.year, month: cursor.month, day: cursor.day),
          name: info.events.first.map(NamedText.init),
          days: start.days(until: cursor)
        )
      }
    }
    return nil
  }

  /// The public holiday on a day, if the day is one.
  /// - Parameter date: The day to read.
  /// - Returns: The holiday with a day count of 0, or `nil` when the day is not a public holiday.
  public static func holiday(on date: MonthDate) -> HolidayCountdown? {
    guard let day = try? NepaliDate(year: date.year, month: date.month, day: date.day)
    else { return nil }
    let info = day.calendarInfo
    guard info.isPublicHoliday else { return nil }
    return HolidayCountdown(
      date: date,
      name: info.events.first.map(NamedText.init),
      days: 0
    )
  }
}
