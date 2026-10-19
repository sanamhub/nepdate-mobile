import CalendarKit
import EventKit
import Foundation

/// Builds the all-day event handed to the system event editor (MR-02 AC2).
///
/// The builder is separate from the editor so a unit test can inspect the event without presenting
/// the editor or asking for calendar access. It never reads a calendar, a default calendar or an
/// authorization status.
public enum DayEvent {
  /// Builds the event for a day detail sheet.
  ///
  /// The title is the first event or the holiday name, so a holiday with a named event keeps that
  /// name and its scope. The event is all-day on the AD date in the current time zone and has no
  /// alarm. EventKit keeps an all-day event's dates in the current time zone and ends it at
  /// 23:59:59 of its last day.
  /// - Parameters:
  ///   - model: The day's values.
  ///   - publicHoliday: The localised words for a public holiday.
  ///   - store: The event store the editor uses.
  /// - Returns: The event, or `nil` when the AD date cannot be made.
  public static func event(
    for model: DayDetailViewModel,
    publicHoliday: String,
    store: EKEventStore
  ) -> EKEvent? {
    let gregorian = model.gregorianDate
    guard
      let start = startOfDay(
        year: gregorian.year,
        month: gregorian.month,
        day: gregorian.day,
        timeZone: .current
      )
    else { return nil }

    let event = EKEvent(eventStore: store)
    event.title = model.calendarTitle(publicHoliday: publicHoliday)
    event.isAllDay = true
    event.startDate = start
    // A one-day all-day event ends on the day it starts; the next midnight can make it two days.
    event.endDate = start
    event.alarms = nil
    return event
  }

  /// Midnight of a Gregorian date in a time zone.
  ///
  /// The AD year, month and day are Gregorian. `Calendar.current` follows the user's calendar
  /// setting (Buddhist, Japanese and so on), which would read 2026 as a year of another era, so the
  /// calendar here is always Gregorian.
  /// - Parameters:
  ///   - year: The AD year.
  ///   - month: The AD month, 1 to 12.
  ///   - day: The AD day of the month.
  ///   - timeZone: The time zone of the midnight.
  /// - Returns: The start of that day, or `nil` when the date cannot be made.
  public static func startOfDay(year: Int, month: Int, day: Int, timeZone: TimeZone) -> Date? {
    var calendar = Calendar(identifier: .gregorian)
    calendar.timeZone = timeZone
    let components = DateComponents(year: year, month: month, day: day)
    guard let date = calendar.date(from: components) else { return nil }
    return calendar.startOfDay(for: date)
  }
}
